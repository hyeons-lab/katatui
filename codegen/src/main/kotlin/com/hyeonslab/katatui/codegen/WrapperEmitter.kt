package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.FunctionRole
import com.hyeonslab.katatui.codegen.model.WidgetGroup
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

private val EXPERIMENTAL_FOREIGN_API = ClassName("kotlin", "OptIn")
private val EXPERIMENTAL_ANNOTATION = ClassName("kotlinx.cinterop", "ExperimentalForeignApi")
private val CPOINTER = ClassName("kotlinx.cinterop", "CPointer")
private val BASE_PACKAGE = "com.hyeonslab.katatui"
private val CINTEROP_PACKAGE = "$BASE_PACKAGE.cinterop"

/**
 * Emits one Kotlin file per widget group, using KotlinPoet.
 *
 * Generated classes follow the same pattern as the hand-written Block/Paragraph/List:
 * - `internal constructor(ptr: CPointer<KatatuiXxx>)` — pointer-backed
 * - `AutoCloseable` — via `katatui_xxx_free`
 * - `companion object { operator fun invoke(...) }` — DSL factory
 * - Properties for each `katatui_xxx_set_yyy` function
 */
class WrapperEmitter(private val outputDir: File) {
    fun emit(groups: List<WidgetGroup>) {
        for (group in groups) {
            emitGroup(group)
        }
    }

    private fun emitGroup(group: WidgetGroup) {
        val cPointerType = ClassName(CINTEROP_PACKAGE, group.cName)
        val constructorFn = group.constructor ?: return // can't generate without a constructor
        val destructorFn = group.destructor

        val classBuilder =
            TypeSpec.classBuilder(group.kotlinName)
                .addAnnotation(
                    AnnotationSpec.builder(EXPERIMENTAL_FOREIGN_API).build()
                )
                .addModifiers(KModifier.INTERNAL) // generated; hand-written wrappers expose API
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addModifiers(KModifier.INTERNAL)
                        .addParameter(
                            ParameterSpec.builder("ptr", CPOINTER.parameterizedBy(cPointerType))
                                .build()
                        )
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("ptr", CPOINTER.parameterizedBy(cPointerType))
                        .addModifiers(KModifier.INTERNAL)
                        .initializer("ptr")
                        .build()
                )
                .addSuperinterface(ClassName("kotlin", "AutoCloseable"))

        // close() — delegates to katatui_xxx_free
        val closeFun =
            FunSpec.builder("close")
                .addModifiers(KModifier.OVERRIDE)
                .apply {
                    if (destructorFn != null) {
                        addStatement(
                            "%T.%L(ptr)",
                            ClassName(CINTEROP_PACKAGE, destructorFn.name),
                            destructorFn.name,
                        )
                        // Use direct cinterop import instead
                        addStatement("${destructorFn.name}(ptr)")
                    }
                }
                .build()
        classBuilder.addFunction(closeFun)

        // Companion object with invoke factory
        val companionBuilder = TypeSpec.companionObjectBuilder()
        val invokeFun =
            FunSpec.builder("invoke")
                .addModifiers(KModifier.OPERATOR)
                .returns(ClassName(BASE_PACKAGE, group.kotlinName))
                .addParameter(
                    ParameterSpec.builder(
                        "init",
                        ClassName(BASE_PACKAGE, group.kotlinName).copy(
                            annotations = emptyList()
                        ),
                    )
                        .defaultValue("{}")
                        .build()
                )
                .addStatement(
                    "return %T(checkNotNull(${constructorFn.name}()) { \"${constructorFn.name}() returned null\" }).apply(init)",
                    ClassName(BASE_PACKAGE, group.kotlinName),
                )
                .build()
        companionBuilder.addFunction(invokeFun)
        classBuilder.addType(companionBuilder.build())

        // Setter properties
        for (setter in group.setters) {
            val propName = setter.setterProperty?.let { snakeToCamel(it) } ?: continue
            val setterParam = setter.params.getOrNull(1) ?: continue
            val kotlinType = cTypeToKotlin(setterParam.type)

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
        for (adder in group.adders) {
            val methodName = snakeToCamel(adder.name.removePrefix("katatui_${group.group}_add_"))
            val addParam = adder.params.getOrNull(1) ?: continue
            val kotlinType = cTypeToKotlin(addParam.type)
            val fun_ =
                FunSpec.builder("add${methodName.replaceFirstChar(Char::uppercase)}")
                    .addParameter("value", kotlinType)
                    .addStatement("${adder.name}(ptr, value)")
                    .build()
            classBuilder.addFunction(fun_)
        }

        val file =
            FileSpec.builder(BASE_PACKAGE, group.kotlinName)
                .addAnnotation(
                    AnnotationSpec.builder(
                        ClassName("kotlin", "Suppress")
                    )
                        .addMember("%S", "NOTHING_TO_INLINE")
                        .build()
                )
                .addType(classBuilder.build())
                .build()

        outputDir.mkdirs()
        file.writeTo(outputDir)
    }

    private fun snakeToCamel(snake: String): String =
        snake.split("_").mapIndexed { i, part ->
            if (i == 0) part else part.replaceFirstChar(Char::uppercase)
        }.joinToString("")

    private fun cTypeToKotlin(cType: String): ClassName =
        when {
            cType.contains("char *") || cType.contains("char*") -> ClassName("kotlin", "String")
            cType == "bool" -> ClassName("kotlin", "Boolean")
            cType == "uint32_t" -> ClassName("kotlin", "UInt")
            cType == "uint16_t" -> ClassName("kotlin", "UShort")
            cType == "uint8_t" -> ClassName("kotlin", "UByte")
            cType == "int32_t" -> ClassName("kotlin", "Int")
            cType.contains("KatatuiStyle") -> ClassName(CINTEROP_PACKAGE, "KatatuiStyle")
            else -> ClassName("kotlin", "Any")
        }

    private fun defaultValueFor(type: ClassName): String =
        when (type.simpleName) {
            "String" -> "null"
            "Boolean" -> "false"
            "UInt" -> "0u"
            "UShort" -> "0u"
            "UByte" -> "0u"
            "Int" -> "0"
            else -> "null"
        }
}
