package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.CFunction
import com.hyeonslab.katatui.codegen.model.CParam
import com.hyeonslab.katatui.codegen.model.FunctionRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CFunctionTest {
  private fun fn(name: String, vararg params: String) =
    CFunction("void", name, params.mapIndexed { i, t -> CParam("p$i", t) })

  // --- role ---

  @Test
  fun `_new suffix maps to Constructor`() {
    assertEquals(FunctionRole.Constructor, fn("katatui_block_new").role)
  }

  @Test
  fun `_free suffix maps to Destructor`() {
    assertEquals(FunctionRole.Destructor, fn("katatui_block_free").role)
  }

  @Test
  fun `_set_ infix maps to Setter`() {
    assertEquals(FunctionRole.Setter, fn("katatui_block_set_title").role)
  }

  @Test
  fun `_add_ infix maps to Adder`() {
    assertEquals(FunctionRole.Adder, fn("katatui_sparkline_add_data").role)
  }

  @Test
  fun `_render_ infix maps to Renderer`() {
    assertEquals(FunctionRole.Renderer, fn("katatui_frame_render_widget").role)
  }

  @Test
  fun `_split suffix maps to Split`() {
    assertEquals(FunctionRole.Split, fn("katatui_layout_split").role)
  }

  @Test
  fun `unrecognised name maps to Other`() {
    assertEquals(FunctionRole.Other, fn("katatui_event_poll").role)
  }

  // --- setterProperty ---

  @Test
  fun `setterProperty extracts name after _set_`() {
    assertEquals("title", fn("katatui_block_set_title").setterProperty)
  }

  @Test
  fun `setterProperty preserves underscores for multi-word names`() {
    assertEquals("bar_width", fn("katatui_bar_chart_set_bar_width").setterProperty)
  }

  @Test
  fun `setterProperty is null for non-setter`() {
    assertNull(fn("katatui_block_new").setterProperty)
    assertNull(fn("katatui_sparkline_add_data").setterProperty)
  }

  // --- group ---

  @Test
  fun `group returns first component of function name`() {
    assertEquals("block", fn("katatui_block_new").group)
    assertEquals("sparkline", fn("katatui_sparkline_add_data").group)
  }
}
