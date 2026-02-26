package com.hyeonslab.katatui.codegen

import java.io.File

/**
 * Entry point for the katatui codegen tool.
 *
 * Usage: Main <headerPath> <outputDir>
 * - headerPath: path to katatui.h (cbindgen output)
 * - outputDir: directory to write generated .kt files
 */
fun main(args: Array<String>) {
  require(args.size == 2) { "Usage: codegen <headerPath> <outputDir>" }
  val headerFile = File(args[0])
  val outputDir = File(args[1])

  require(headerFile.exists()) { "Header file not found: ${headerFile.absolutePath}" }

  println("Parsing header: ${headerFile.absolutePath}")
  val parser = HeaderParser()
  parser.parse(headerFile.readLines())

  println(
    "Found: ${parser.opaqueTypes.size} opaque types, " +
      "${parser.structs.size} structs, " +
      "${parser.enums.size} enums, " +
      "${parser.functions.size} functions"
  )

  val groups = parser.widgetGroups()
  println("Widget groups: ${groups.map { it.kotlinName }}")

  val emitter = WrapperEmitter(outputDir)
  emitter.emit(groups)

  println("Generated ${groups.size} wrapper files → ${outputDir.absolutePath}")
}
