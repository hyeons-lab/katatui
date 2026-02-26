package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.CEnum
import com.hyeonslab.katatui.codegen.model.CField
import com.hyeonslab.katatui.codegen.model.CFunction
import com.hyeonslab.katatui.codegen.model.CParam
import com.hyeonslab.katatui.codegen.model.CStruct
import com.hyeonslab.katatui.codegen.model.CVariant
import com.hyeonslab.katatui.codegen.model.WidgetGroup

/**
 * Parses a cbindgen-generated C header for katatui FFI types and functions.
 *
 * cbindgen output is highly structured, so simple line-by-line parsing is sufficient.
 */
class HeaderParser {
  val opaqueTypes = mutableSetOf<String>()
  val structs = mutableMapOf<String, CStruct>()
  val enums = mutableMapOf<String, CEnum>()
  val functions = mutableListOf<CFunction>()

  fun parse(lines: List<String>) {
    var i = 0
    while (i < lines.size) {
      val line = lines[i].trim()
      when {
        // Opaque struct typedef: "typedef struct Foo Foo;"
        line.matches(Regex("""typedef struct (\w+) (\w+);""")) -> {
          val m = Regex("""typedef struct (\w+) (\w+);""").find(line)!!
          opaqueTypes += m.groupValues[2]
        }
        // Start of named struct: "typedef struct Foo {"
        line.matches(Regex("""typedef struct \{""")) ||
          line.matches(Regex("""typedef struct \w+ \{""")) -> {
          val structLines = mutableListOf<String>()
          i++
          while (i < lines.size && !lines[i].trim().startsWith("}")) {
            structLines += lines[i].trim()
            i++
          }
          // "} KatatuiRect;"
          val nameLine = lines[i].trim()
          val name = Regex("""}\s*(\w+);""").find(nameLine)?.groupValues?.get(1)
          if (name != null) {
            val fields =
              structLines
                .filter { it.isNotBlank() && !it.startsWith("//") }
                .mapNotNull { parseField(it) }
            structs[name] = CStruct(name, fields)
          }
        }
        // Start of enum: "typedef enum {"
        line.matches(Regex("""typedef enum \{""")) ||
          line.matches(Regex("""typedef enum \w+ \{""")) -> {
          val variantLines = mutableListOf<String>()
          i++
          while (i < lines.size && !lines[i].trim().startsWith("}")) {
            variantLines += lines[i].trim()
            i++
          }
          val nameLine = lines[i].trim()
          val name = Regex("""}\s*(\w+);""").find(nameLine)?.groupValues?.get(1)
          if (name != null) {
            val variants =
              variantLines
                .filter { it.isNotBlank() && !it.startsWith("//") }
                .mapNotNull { parseVariant(it) }
            enums[name] = CEnum(name, variants)
          }
        }
        // Function declaration (not typedef, not comment)
        line.contains("katatui_") && line.endsWith(";") && !line.startsWith("//") -> {
          parseFunction(line)?.let { functions += it }
        }
      }
      i++
    }
  }

  private fun parseField(line: String): CField? {
    // "uint16_t x;" or "enum KatatuiColor fg;" or "bool bold;"
    val clean = line.removeSuffix(";").trim()
    val parts = clean.split(Regex("""\s+"""))
    if (parts.size < 2) return null
    val name = parts.last()
    val type = parts.dropLast(1).joinToString(" ")
    return CField(name, type)
  }

  private fun parseVariant(line: String): CVariant? {
    // "RESET = 0," or "BLACK = 1,"
    val clean = line.removeSuffix(",").trim()
    val eqIdx = clean.indexOf('=')
    if (eqIdx < 0) return null
    val name = clean.substring(0, eqIdx).trim()
    val value = clean.substring(eqIdx + 1).trim().toIntOrNull() ?: return null
    return CVariant(name, value)
  }

  private fun parseFunction(line: String): CFunction? {
    // e.g. "struct KatatuiBlock *katatui_block_new(void);"
    // or "void katatui_block_set_title(struct KatatuiBlock *block, const char *title);"
    val noSemi = line.removeSuffix(";").trim()
    val parenOpen = noSemi.indexOf('(')
    val parenClose = noSemi.lastIndexOf(')')
    if (parenOpen < 0 || parenClose < 0) return null

    val beforeParen = noSemi.substring(0, parenOpen).trim()
    val paramsStr = noSemi.substring(parenOpen + 1, parenClose).trim()

    // Split return type + name: last token before '(' is the name
    val tokens = beforeParen.split(Regex("""\s+"""))
    val name = tokens.last().removePrefix("*")
    if (!name.startsWith("katatui_")) return null
    val returnType = tokens.dropLast(1).joinToString(" ")

    val params =
      if (paramsStr == "void" || paramsStr.isEmpty()) {
        emptyList()
      } else {
        paramsStr.split(",").mapNotNull { parseParam(it.trim()) }
      }

    return CFunction(returnType, name, params)
  }

  private fun parseParam(param: String): CParam? {
    val parts = param.split(Regex("""\s+"""))
    if (parts.isEmpty()) return null
    val name = parts.last().removePrefix("*")
    val type = parts.dropLast(1).joinToString(" ")
    return CParam(name, type)
  }

  fun widgetGroups(): List<WidgetGroup> {
    // Group katatui_<widget>_* functions by <widget> prefix, exclude terminal/event/frame/layout
    val excluded = setOf("terminal", "event", "frame", "layout")
    val grouped =
      functions
        .filter { it.name.startsWith("katatui_") }
        .groupBy { it.group }
        .filter { (group, _) -> group !in excluded }

    // Override Kotlin class names that would collide with stdlib names
    val kotlinNameOverrides = mapOf("list" to "ListWidget")

    return grouped.map { (group, fns) ->
      val cName =
        "Katatui" + group.split("_").joinToString("") { it.replaceFirstChar(Char::uppercase) }
      val kotlinName =
        kotlinNameOverrides[group]
          ?: group.split("_").joinToString("") { it.replaceFirstChar(Char::uppercase) }
      WidgetGroup(cName, kotlinName, fns)
    }
  }
}
