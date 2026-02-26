package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.WidgetGroup
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import java.io.File

private val CPOINTER = ClassName("kotlinx.cinterop", "CPointer")
private const val BASE_PACKAGE = "com.hyeonslab.katatui"
private const val CINTEROP_PACKAGE = "$BASE_PACKAGE.cinterop"

// C types that require complex FFI handling (e.g. memScoped) — excluded from codegen setters
private val COMPLEX_C_TYPES = setOf("KatatuiStyle", "KatatuiRect", "KatatuiConstraint")

/**
 * Emits one Kotlin file per widget group using KotlinPoet.
 *
 * Generated classes:
 * - `class Foo internal constructor(internal val ptr: CPointer<KatatuiFoo>) : AutoCloseable`
 * - `companion object { operator fun invoke(init: Foo.() -> Unit = {}): Foo }`
 * - Properties for simple `katatui_foo_set_yyy` setters (String, Bool, UInt)
 * - Adder methods for `katatui_foo_add_yyy` functions
 *
 * Complex setter types (KatatuiStyle, KatatuiRect) are excluded — add hand-written overrides.
 */
class WrapperEmitter(private val outputDir: File) {
  fun emit(groups: List<WidgetGroup>) {
    for (group in groups) {
      emitGroup(group)
    }
  }

  @Suppress("LongMethod", "CyclomaticComplexMethod")
  private fun emitGroup(group: WidgetGroup) {
    // Opaque C structs (forward declarations) live in cnames.structs, NOT the cinterop package
    val cPointerType = ClassName("cnames.structs", group.cName)
    val selfType = ClassName(BASE_PACKAGE, group.kotlinName)
    val constructorFn = group.constructor ?: return
    val destructorFn = group.destructor
    val ptrType = CPOINTER.parameterizedBy(cPointerType)
    val classBuilder =
      TypeSpec.classBuilder(group.kotlinName)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addModifiers(KModifier.INTERNAL)
            .addParameter(ParameterSpec.builder("ptr", ptrType).build())
            .build()
        )
        .addProperty(
          PropertySpec.builder("ptr", ptrType)
            .addModifiers(KModifier.INTERNAL)
            .initializer("ptr")
            .build()
        )
        .addSuperinterface(ClassName(BASE_PACKAGE, "Katatui"))

    // close() delegates to katatui_xxx_free
    val closeFun =
      FunSpec.builder("close")
        .addModifiers(KModifier.OVERRIDE)
        .apply {
          if (destructorFn != null) {
            addStatement("${destructorFn.name}(ptr)")
          }
        }
        .build()
    classBuilder.addFunction(closeFun)

    // Setter properties — skip complex C types that need hand-written wrappers
    data class SetterInfo(
      val propName: String,
      val setter: com.hyeonslab.katatui.codegen.model.CFunction,
      val paramType: String,
    )
    group.setters
      .mapNotNull { s ->
        val name = s.setterProperty?.let { snakeToCamel(it) } ?: return@mapNotNull null
        val param = s.params.getOrNull(1) ?: return@mapNotNull null
        if (isComplexType(param.type)) return@mapNotNull null
        SetterInfo(name, s, param.type)
      }
      .forEach { (propName, setter, paramType) ->
        val kotlinType = cTypeToKotlin(paramType)
        val propBuilder =
          PropertySpec.builder(propName, kotlinType)
            .mutable(true)
            .initializer(defaultValueFor(kotlinType))
            .setter(
              FunSpec.setterBuilder()
                .addParameter("value", kotlinType)
                .addStatement("field = value")
                .addStatement("${setter.name}(ptr, value)")
                .build()
            )
        classBuilder.addProperty(propBuilder.build())
      }

    // Adder methods
    data class AdderInfo(
      val methodName: String,
      val adder: com.hyeonslab.katatui.codegen.model.CFunction,
      val paramType: String,
    )
    val adderPrefix = "katatui_${group.kotlinName.lowercase()}_add_"
    group.adders
      .mapNotNull { a ->
        val suffix = a.name.removePrefix(adderPrefix).ifEmpty { a.name }
        val methodName = "add${snakeToCamel(suffix).replaceFirstChar(Char::uppercase)}"
        val param = a.params.getOrNull(1) ?: return@mapNotNull null
        if (isComplexType(param.type)) return@mapNotNull null
        AdderInfo(methodName, a, param.type)
      }
      .forEach { (methodName, adder, paramType) ->
        val kotlinType = cTypeToKotlin(paramType)
        val adderFun =
          FunSpec.builder(methodName)
            .addParameter("value", kotlinType)
            .addStatement("${adder.name}(ptr, value)")
            .build()
        classBuilder.addFunction(adderFun)
      }

    // Companion object DSL factory
    // Constructor params (e.g. katatui_paragraph_new(const char *text))
    val ctorParams = constructorFn.params
    val ctorCallArgs = ctorParams.joinToString(", ") { it.name }

    val initLambdaType = LambdaTypeName.get(receiver = selfType, returnType = UNIT)
    val invokeFunBuilder =
      FunSpec.builder("invoke").addModifiers(KModifier.OPERATOR).returns(selfType)
    for (param in ctorParams) {
      invokeFunBuilder.addParameter(param.name, cTypeToKotlin(param.type))
    }
    invokeFunBuilder
      .addParameter(ParameterSpec.builder("init", initLambdaType).defaultValue("{}").build())
      .addStatement(
        "return %T(checkNotNull(${constructorFn.name}($ctorCallArgs)) " +
          "{ \"${constructorFn.name}() returned null\" }).apply(init)",
        selfType,
      )
    val invokeFun = invokeFunBuilder.build()
    classBuilder.addType(TypeSpec.companionObjectBuilder().addFunction(invokeFun).build())

    val fileBuilder =
      FileSpec.builder(BASE_PACKAGE, group.kotlinName)
        .addAnnotation(
          AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
            .addMember("%T::class", ClassName("kotlinx.cinterop", "ExperimentalForeignApi"))
            .build()
        )

    // Import all cinterop functions used in the generated code
    constructorFn.let { fileBuilder.addImport(CINTEROP_PACKAGE, it.name) }
    destructorFn?.let { fileBuilder.addImport(CINTEROP_PACKAGE, it.name) }
    group.setters
      .filter { !isComplexType(it.params.getOrNull(1)?.type ?: "") }
      .forEach { fileBuilder.addImport(CINTEROP_PACKAGE, it.name) }
    group.adders
      .filter { !isComplexType(it.params.getOrNull(1)?.type ?: "") }
      .forEach { fileBuilder.addImport(CINTEROP_PACKAGE, it.name) }

    fileBuilder.addType(classBuilder.build())
    outputDir.mkdirs()
    fileBuilder.build().writeTo(outputDir)
  }

  private fun snakeToCamel(snake: String): String =
    snake
      .split("_")
      .mapIndexed { i, part -> if (i == 0) part else part.replaceFirstChar(Char::uppercase) }
      .joinToString("")

  private fun isComplexType(cType: String): Boolean = COMPLEX_C_TYPES.any { cType.contains(it) }

  private fun cTypeToKotlin(cType: String): ClassName =
    when {
      cType.contains("char") -> ClassName("kotlin", "String")
      cType == "bool" -> ClassName("kotlin", "Boolean")
      cType.contains("uint32_t") -> ClassName("kotlin", "UInt")
      cType.contains("uint16_t") -> ClassName("kotlin", "UShort")
      cType.contains("uint8_t") -> ClassName("kotlin", "UByte")
      cType.contains("int32_t") -> ClassName("kotlin", "Int")
      else -> ClassName("kotlin", "Any")
    }

  private fun defaultValueFor(type: ClassName): String =
    when (type.simpleName) {
      "String" -> "\"\""
      "Boolean" -> "false"
      "UInt" -> "0u"
      "UShort" -> "0u"
      "UByte" -> "0u"
      "Int" -> "0"
      else -> "null"
    }
}
