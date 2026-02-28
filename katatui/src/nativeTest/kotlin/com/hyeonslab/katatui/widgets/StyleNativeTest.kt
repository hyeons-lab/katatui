package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.cinterop.KatatuiColor_Blue
import com.hyeonslab.katatui.cinterop.KatatuiColor_Indexed
import com.hyeonslab.katatui.cinterop.KatatuiColor_Reset
import com.hyeonslab.katatui.cinterop.KatatuiColor_Rgb
import com.hyeonslab.katatui.cinterop.KatatuiColor_White
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents

@OptIn(ExperimentalForeignApi::class)
class StyleNativeTest {
  // --- fg / bg: Rgb ---

  @Test
  fun `Rgb fg sets color tag and payload fields`() {
    Style(fg = Color.Rgb(10u, 20u, 30u)).toCValue().useContents {
      fg shouldBe KatatuiColor_Rgb
      fg_r shouldBe 10.toUByte()
      fg_g shouldBe 20.toUByte()
      fg_b shouldBe 30.toUByte()
    }
  }

  @Test
  fun `Rgb bg sets color tag and payload fields`() {
    Style(bg = Color.Rgb(50u, 100u, 200u)).toCValue().useContents {
      bg shouldBe KatatuiColor_Rgb
      bg_r shouldBe 50.toUByte()
      bg_g shouldBe 100.toUByte()
      bg_b shouldBe 200.toUByte()
    }
  }

  // --- fg / bg: Indexed ---

  @Test
  fun `Indexed fg sets color tag and index field`() {
    Style(fg = Color.Indexed(42u)).toCValue().useContents {
      fg shouldBe KatatuiColor_Indexed
      fg_index shouldBe 42.toUByte()
    }
  }

  @Test
  fun `Indexed bg sets color tag and index field`() {
    Style(bg = Color.Indexed(100u)).toCValue().useContents {
      bg shouldBe KatatuiColor_Indexed
      bg_index shouldBe 100.toUByte()
    }
  }

  // --- fg / bg: named colors ---

  @Test
  fun `named color Reset maps to cinterop Reset`() {
    Style(fg = Color.Reset).toCValue().useContents { fg shouldBe KatatuiColor_Reset }
  }

  @Test
  fun `named color Blue maps to cinterop Blue`() {
    Style(fg = Color.Blue).toCValue().useContents { fg shouldBe KatatuiColor_Blue }
  }

  @Test
  fun `named color White maps to cinterop White`() {
    Style(fg = Color.White).toCValue().useContents { fg shouldBe KatatuiColor_White }
  }

  // --- modifiers ---

  @Test
  fun `bold is propagated`() {
    Style(bold = true).toCValue().useContents { bold shouldBe true }
  }

  @Test
  fun `italic is propagated`() {
    Style(italic = true).toCValue().useContents { italic shouldBe true }
  }

  @Test
  fun `underlined is propagated`() {
    Style(underlined = true).toCValue().useContents { underlined shouldBe true }
  }

  @Test
  fun `dim is propagated`() {
    Style(dim = true).toCValue().useContents { dim shouldBe true }
  }

  @Test
  fun `crossedOut maps to crossed_out field`() {
    Style(crossedOut = true).toCValue().useContents { crossed_out shouldBe true }
  }

  // --- default style ---

  @Test
  fun `default Style produces Reset fg and bg with no modifiers`() {
    Style().toCValue().useContents {
      fg shouldBe KatatuiColor_Reset
      bg shouldBe KatatuiColor_Reset
      bold shouldBe false
      italic shouldBe false
      underlined shouldBe false
      dim shouldBe false
      crossed_out shouldBe false
    }
  }
}
