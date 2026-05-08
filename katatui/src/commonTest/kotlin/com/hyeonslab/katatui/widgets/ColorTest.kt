package com.hyeonslab.katatui.widgets

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class ColorTest {
  @Test
  fun `Rgb stores r g b components`() {
    val c = Color.Rgb(10u, 20u, 30u)
    c.r shouldBe 10.toUByte()
    c.g shouldBe 20.toUByte()
    c.b shouldBe 30.toUByte()
  }

  @Test
  fun `Rgb data class equality compares by value`() {
    Color.Rgb(1u, 2u, 3u) shouldBe Color.Rgb(1u, 2u, 3u)
    Color.Rgb(1u, 2u, 3u) shouldNotBe Color.Rgb(1u, 2u, 4u)
  }

  @Test
  fun `Indexed stores index`() {
    val c = Color.Indexed(42u)
    c.index shouldBe 42.toUByte()
  }

  @Test
  fun `Indexed data class equality compares by value`() {
    Color.Indexed(5u) shouldBe Color.Indexed(5u)
    Color.Indexed(5u) shouldNotBe Color.Indexed(6u)
  }

  @Test
  fun `named singletons are sealed Color subtypes`() {
    Color.Reset.shouldBeInstanceOf<Color>()
    Color.Black.shouldBeInstanceOf<Color>()
    Color.White.shouldBeInstanceOf<Color>()
    Color.LightCyan.shouldBeInstanceOf<Color>()
  }

  @Test
  fun `Rgb and Indexed are distinct from named singletons`() {
    Color.Rgb(255u, 0u, 0u).shouldBeInstanceOf<Color.Rgb>()
    Color.Indexed(200u).shouldBeInstanceOf<Color.Indexed>()
  }
}
