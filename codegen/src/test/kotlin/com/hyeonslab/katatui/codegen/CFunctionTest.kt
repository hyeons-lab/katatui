package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.CFunction
import com.hyeonslab.katatui.codegen.model.CParam
import com.hyeonslab.katatui.codegen.model.FunctionRole
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CFunctionTest {
  private fun fn(name: String, vararg params: String) =
    CFunction("void", name, params.mapIndexed { i, t -> CParam("p$i", t) })

  // --- role ---

  @Test
  fun `_new suffix maps to Constructor`() {
    fn("katatui_block_new").role shouldBe FunctionRole.Constructor
  }

  @Test
  fun `_free suffix maps to Destructor`() {
    fn("katatui_block_free").role shouldBe FunctionRole.Destructor
  }

  @Test
  fun `_set_ infix maps to Setter`() {
    fn("katatui_block_set_title").role shouldBe FunctionRole.Setter
  }

  @Test
  fun `_add_ infix maps to Adder`() {
    fn("katatui_sparkline_add_data").role shouldBe FunctionRole.Adder
  }

  @Test
  fun `_render_ infix maps to Renderer`() {
    fn("katatui_frame_render_widget").role shouldBe FunctionRole.Renderer
  }

  @Test
  fun `_split suffix maps to Split`() {
    fn("katatui_layout_split").role shouldBe FunctionRole.Split
  }

  @Test
  fun `unrecognised name maps to Other`() {
    fn("katatui_event_poll").role shouldBe FunctionRole.Other
  }

  // --- setterProperty ---

  @Test
  fun `setterProperty extracts name after _set_`() {
    fn("katatui_block_set_title").setterProperty shouldBe "title"
  }

  @Test
  fun `setterProperty preserves underscores for multi-word names`() {
    fn("katatui_bar_chart_set_bar_width").setterProperty shouldBe "bar_width"
  }

  @Test
  fun `setterProperty is null for non-setter`() {
    fn("katatui_block_new").setterProperty shouldBe null
    fn("katatui_sparkline_add_data").setterProperty shouldBe null
  }

  // --- group ---

  @Test
  fun `group returns first component of function name`() {
    fn("katatui_block_new").group shouldBe "block"
    fn("katatui_sparkline_add_data").group shouldBe "sparkline"
  }
}
