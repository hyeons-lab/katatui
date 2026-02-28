package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.CFunction
import com.hyeonslab.katatui.codegen.model.CParam
import com.hyeonslab.katatui.codegen.model.WidgetGroup
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import java.nio.file.Files
import kotlin.test.Test

class WrapperEmitterTest {
  private val ptr = "struct KatatuiBlock *"
  private val self = CParam("block", ptr)

  private val ctorFn = CFunction(ptr, "katatui_block_new", emptyList())
  private val dtorFn = CFunction("void", "katatui_block_free", listOf(self))
  private val titleSetter =
    CFunction("void", "katatui_block_set_title", listOf(self, CParam("title", "const char *")))
  private val bordersSetter =
    CFunction("void", "katatui_block_set_borders", listOf(self, CParam("borders", "uint32_t")))
  private val styleSetter =
    CFunction("void", "katatui_block_set_style", listOf(self, CParam("style", "KatatuiStyle")))
  private val graphTypeSetter =
    CFunction(
      "void",
      "katatui_block_set_graph_type",
      listOf(self, CParam("graph_type", "KatatuiGraphType")),
    )

  private fun withTempDir(block: (File) -> String): String {
    val dir = Files.createTempDirectory("katatui-test").toFile()
    return try {
      block(dir)
    } finally {
      dir.deleteRecursively()
    }
  }

  private fun emitBlock(vararg extra: CFunction): String = withTempDir { dir ->
    val group =
      WidgetGroup(
        "KatatuiBlock",
        "Block",
        listOf(ctorFn, dtorFn, titleSetter, bordersSetter, *extra),
      )
    WrapperEmitter(dir).emit(listOf(group))
    dir.resolve("com/hyeonslab/katatui/Block.kt").readText()
  }

  @Test
  fun `generated file contains class declaration`() {
    emitBlock() shouldContain "class Block"
  }

  @Test
  fun `generated file contains close override`() {
    val text = emitBlock()
    text shouldContain "fun close()"
    text shouldContain "katatui_block_free"
  }

  @Test
  fun `generated file contains companion invoke factory`() {
    val text = emitBlock()
    text shouldContain "operator fun invoke"
    text shouldContain "katatui_block_new"
  }

  @Test
  fun `simple String setter is emitted as mutable property`() {
    val text = emitBlock()
    text shouldContain "var title"
    text shouldContain "katatui_block_set_title"
  }

  @Test
  fun `UInt setter is emitted as mutable property`() {
    val text = emitBlock()
    text shouldContain "var borders"
    text shouldContain "katatui_block_set_borders"
  }

  @Test
  fun `complex type KatatuiStyle is excluded from generated setters`() {
    val group = WidgetGroup("KatatuiBlock", "Block", listOf(ctorFn, dtorFn, styleSetter))
    val text = withTempDir { dir ->
      WrapperEmitter(dir).emit(listOf(group))
      dir.resolve("com/hyeonslab/katatui/Block.kt").readText()
    }
    text shouldNotContain "var style"
  }

  @Test
  fun `setter with complex Katatui enum param is excluded`() {
    val group = WidgetGroup("KatatuiBlock", "Block", listOf(ctorFn, dtorFn, graphTypeSetter))
    val text = withTempDir { dir ->
      WrapperEmitter(dir).emit(listOf(group))
      dir.resolve("com/hyeonslab/katatui/Block.kt").readText()
    }
    text shouldNotContain "var graphType"
  }

  @Test
  fun `adder method is emitted for _add_ functions`() {
    val adder =
      CFunction("void", "katatui_block_add_data", listOf(self, CParam("value", "uint64_t")))
    val text = emitBlock(adder)
    text shouldContain "addData"
    text shouldContain "katatui_block_add_data"
  }

  @Test
  fun `generated file carries OptIn annotation`() {
    val text = emitBlock()
    text shouldContain "@file:OptIn"
    text shouldContain "ExperimentalForeignApi::class"
  }

  @Test
  fun `generated file imports used FFI functions`() {
    val text = emitBlock()
    text shouldContain "import com.hyeonslab.katatui.cinterop.katatui_block_new"
    text shouldContain "import com.hyeonslab.katatui.cinterop.katatui_block_free"
    text shouldContain "import com.hyeonslab.katatui.cinterop.katatui_block_set_title"
  }
}
