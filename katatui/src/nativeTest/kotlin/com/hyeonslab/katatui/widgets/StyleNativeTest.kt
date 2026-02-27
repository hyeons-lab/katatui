package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.cinterop.KatatuiColor_Blue
import com.hyeonslab.katatui.cinterop.KatatuiColor_Indexed
import com.hyeonslab.katatui.cinterop.KatatuiColor_Reset
import com.hyeonslab.katatui.cinterop.KatatuiColor_Rgb
import com.hyeonslab.katatui.cinterop.KatatuiColor_White
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents

@OptIn(ExperimentalForeignApi::class)
class StyleNativeTest {
  // --- fg / bg: Rgb ---

  @Test
  fun `Rgb fg sets color tag and payload fields`() {
    Style(fg = Color.Rgb(10u, 20u, 30u)).toCValue().useContents {
      assertEquals(KatatuiColor_Rgb, fg)
      assertEquals(10.toUByte(), fg_r)
      assertEquals(20.toUByte(), fg_g)
      assertEquals(30.toUByte(), fg_b)
    }
  }

  @Test
  fun `Rgb bg sets color tag and payload fields`() {
    Style(bg = Color.Rgb(50u, 100u, 200u)).toCValue().useContents {
      assertEquals(KatatuiColor_Rgb, bg)
      assertEquals(50.toUByte(), bg_r)
      assertEquals(100.toUByte(), bg_g)
      assertEquals(200.toUByte(), bg_b)
    }
  }

  // --- fg / bg: Indexed ---

  @Test
  fun `Indexed fg sets color tag and index field`() {
    Style(fg = Color.Indexed(42u)).toCValue().useContents {
      assertEquals(KatatuiColor_Indexed, fg)
      assertEquals(42.toUByte(), fg_index)
    }
  }

  @Test
  fun `Indexed bg sets color tag and index field`() {
    Style(bg = Color.Indexed(100u)).toCValue().useContents {
      assertEquals(KatatuiColor_Indexed, bg)
      assertEquals(100.toUByte(), bg_index)
    }
  }

  // --- fg / bg: named colors ---

  @Test
  fun `named color Reset maps to cinterop Reset`() {
    Style(fg = Color.Reset).toCValue().useContents { assertEquals(KatatuiColor_Reset, fg) }
  }

  @Test
  fun `named color Blue maps to cinterop Blue`() {
    Style(fg = Color.Blue).toCValue().useContents { assertEquals(KatatuiColor_Blue, fg) }
  }

  @Test
  fun `named color White maps to cinterop White`() {
    Style(fg = Color.White).toCValue().useContents { assertEquals(KatatuiColor_White, fg) }
  }

  // --- modifiers ---

  @Test
  fun `bold is propagated`() {
    Style(bold = true).toCValue().useContents { assertTrue(bold) }
  }

  @Test
  fun `italic is propagated`() {
    Style(italic = true).toCValue().useContents { assertTrue(italic) }
  }

  @Test
  fun `underlined is propagated`() {
    Style(underlined = true).toCValue().useContents { assertTrue(underlined) }
  }

  @Test
  fun `dim is propagated`() {
    Style(dim = true).toCValue().useContents { assertTrue(dim) }
  }

  @Test
  fun `crossedOut maps to crossed_out field`() {
    Style(crossedOut = true).toCValue().useContents { assertTrue(crossed_out) }
  }

  // --- default style ---

  @Test
  fun `default Style produces Reset fg and bg with no modifiers`() {
    Style().toCValue().useContents {
      assertEquals(KatatuiColor_Reset, fg)
      assertEquals(KatatuiColor_Reset, bg)
      assertFalse(bold)
      assertFalse(italic)
      assertFalse(underlined)
      assertFalse(dim)
      assertFalse(crossed_out)
    }
  }
}
