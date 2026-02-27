package com.hyeonslab.katatui.widgets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StyleTest {
  @Test
  fun `default Style has Reset colors and no modifiers`() {
    val s = Style()
    assertEquals(Color.Reset, s.fg)
    assertEquals(Color.Reset, s.bg)
    assertFalse(s.bold)
    assertFalse(s.italic)
    assertFalse(s.underlined)
    assertFalse(s.dim)
    assertFalse(s.crossedOut)
  }

  @Test
  fun `RESET constant equals default Style`() {
    assertEquals(Style(), Style.RESET)
  }

  @Test
  fun `custom fg and bg are stored correctly`() {
    val s = Style(fg = Color.Blue, bg = Color.Rgb(10u, 20u, 30u))
    assertEquals(Color.Blue, s.fg)
    assertEquals(Color.Rgb(10u, 20u, 30u), s.bg)
  }

  @Test
  fun `modifiers are stored correctly`() {
    val s = Style(bold = true, italic = true, underlined = true, dim = true, crossedOut = true)
    assertTrue(s.bold)
    assertTrue(s.italic)
    assertTrue(s.underlined)
    assertTrue(s.dim)
    assertTrue(s.crossedOut)
  }

  @Test
  fun `data class equality holds for equal styles`() {
    val a = Style(fg = Color.Red, bold = true)
    val b = Style(fg = Color.Red, bold = true)
    assertEquals(a, b)
  }

  @Test
  fun `copy produces independent modified style`() {
    val base = Style(bold = true)
    val copied = base.copy(italic = true)
    assertTrue(copied.bold)
    assertTrue(copied.italic)
    assertFalse(base.italic)
  }
}
