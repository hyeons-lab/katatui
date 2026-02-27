package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.CFunction
import com.hyeonslab.katatui.codegen.model.CParam
import com.hyeonslab.katatui.codegen.model.WidgetGroup
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

  private fun emitBlock(vararg extra: CFunction): String {
    val group =
      WidgetGroup(
        "KatatuiBlock",
        "Block",
        listOf(ctorFn, dtorFn, titleSetter, bordersSetter, *extra),
      )
    val tempDir = Files.createTempDirectory("katatui-test").toFile()
    WrapperEmitter(tempDir).emit(listOf(group))
    return tempDir.resolve("com/hyeonslab/katatui/Block.kt").readText()
  }

  @Test
  fun `generated file contains class declaration`() {
    assertTrue("class Block" in emitBlock())
  }

  @Test
  fun `generated file contains close override`() {
    val text = emitBlock()
    assertTrue("fun close()" in text)
    assertTrue("katatui_block_free" in text)
  }

  @Test
  fun `generated file contains companion invoke factory`() {
    val text = emitBlock()
    assertTrue("operator fun invoke" in text)
    assertTrue("katatui_block_new" in text)
  }

  @Test
  fun `simple String setter is emitted as mutable property`() {
    val text = emitBlock()
    assertTrue("var title" in text)
    assertTrue("katatui_block_set_title" in text)
  }

  @Test
  fun `UInt setter is emitted as mutable property`() {
    val text = emitBlock()
    assertTrue("var borders" in text)
    assertTrue("katatui_block_set_borders" in text)
  }

  @Test
  fun `complex type KatatuiStyle is excluded from generated setters`() {
    val text =
      WrapperEmitterTest().run {
        val group = WidgetGroup("KatatuiBlock", "Block", listOf(ctorFn, dtorFn, styleSetter))
        val tempDir = Files.createTempDirectory("katatui-test-complex").toFile()
        WrapperEmitter(tempDir).emit(listOf(group))
        tempDir.resolve("com/hyeonslab/katatui/Block.kt").readText()
      }
    assertFalse("var style" in text, "KatatuiStyle setter must be excluded from codegen")
  }

  @Test
  fun `adder method is emitted for _add_ functions`() {
    val adder =
      CFunction("void", "katatui_block_add_data", listOf(self, CParam("value", "uint64_t")))
    val text = emitBlock(adder)
    assertTrue("addData" in text)
    assertTrue("katatui_block_add_data" in text)
  }
}
