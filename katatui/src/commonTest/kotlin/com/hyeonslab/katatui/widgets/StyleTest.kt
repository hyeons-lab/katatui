package com.hyeonslab.katatui.widgets

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StyleTest {
  @Test
  fun `default Style has Reset colors and no modifiers`() {
    val s = Style()
    s.fg shouldBe Color.Reset
    s.bg shouldBe Color.Reset
    s.bold shouldBe false
    s.italic shouldBe false
    s.underlined shouldBe false
    s.dim shouldBe false
    s.crossedOut shouldBe false
  }

  @Test
  fun `RESET constant equals default Style`() {
    Style.RESET shouldBe Style()
  }

  @Test
  fun `custom fg and bg are stored correctly`() {
    val s = Style(fg = Color.Blue, bg = Color.Rgb(10u, 20u, 30u))
    s.fg shouldBe Color.Blue
    s.bg shouldBe Color.Rgb(10u, 20u, 30u)
  }

  @Test
  fun `modifiers are stored correctly`() {
    val s = Style(bold = true, italic = true, underlined = true, dim = true, crossedOut = true)
    s.bold shouldBe true
    s.italic shouldBe true
    s.underlined shouldBe true
    s.dim shouldBe true
    s.crossedOut shouldBe true
  }

  @Test
  fun `data class equality holds for equal styles`() {
    val a = Style(fg = Color.Red, bold = true)
    val b = Style(fg = Color.Red, bold = true)
    a shouldBe b
  }

  @Test
  fun `copy produces independent modified style`() {
    val base = Style(bold = true)
    val copied = base.copy(italic = true)
    copied.bold shouldBe true
    copied.italic shouldBe true
    base.italic shouldBe false
  }
}
