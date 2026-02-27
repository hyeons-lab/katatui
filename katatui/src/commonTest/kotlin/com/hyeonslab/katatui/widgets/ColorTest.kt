package com.hyeonslab.katatui.widgets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class ColorTest {
  @Test
  fun `Rgb stores r g b components`() {
    val c = Color.Rgb(10u, 20u, 30u)
    assertEquals(10.toUByte(), c.r)
    assertEquals(20.toUByte(), c.g)
    assertEquals(30.toUByte(), c.b)
  }

  @Test
  fun `Rgb data class equality compares by value`() {
    assertEquals(Color.Rgb(1u, 2u, 3u), Color.Rgb(1u, 2u, 3u))
    assertNotEquals(Color.Rgb(1u, 2u, 3u), Color.Rgb(1u, 2u, 4u))
  }

  @Test
  fun `Indexed stores index`() {
    val c = Color.Indexed(42u)
    assertEquals(42.toUByte(), c.index)
  }

  @Test
  fun `Indexed data class equality compares by value`() {
    assertEquals(Color.Indexed(5u), Color.Indexed(5u))
    assertNotEquals(Color.Indexed(5u), Color.Indexed(6u))
  }

  @Test
  fun `named singletons are sealed Color subtypes`() {
    assertIs<Color>(Color.Reset)
    assertIs<Color>(Color.Black)
    assertIs<Color>(Color.White)
    assertIs<Color>(Color.LightCyan)
  }

  @Test
  fun `Rgb and Indexed are distinct from named singletons`() {
    val rgb = Color.Rgb(255u, 0u, 0u)
    assertIs<Color.Rgb>(rgb)
    val indexed = Color.Indexed(200u)
    assertIs<Color.Indexed>(indexed)
  }
}
