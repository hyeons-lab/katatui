package com.hyeonslab.katatui.widgets

import kotlin.test.Test
import kotlin.test.assertEquals

class BordersTest {
  @Test
  fun `none has bits 0`() {
    assertEquals(0u, Borders.none.bits)
  }

  @Test
  fun `individual side bits are powers of two`() {
    assertEquals(1u, Borders.top.bits)
    assertEquals(2u, Borders.right.bits)
    assertEquals(4u, Borders.bottom.bits)
    assertEquals(8u, Borders.left.bits)
  }

  @Test
  fun `all has bits 15 equal to union of all sides`() {
    assertEquals(15u, Borders.all.bits)
  }

  @Test
  fun `plus operator ORs bits`() {
    val topRight = Borders.top + Borders.right
    assertEquals(3u, topRight.bits)
  }

  @Test
  fun `plus of all four sides equals all`() {
    val combined = Borders.top + Borders.right + Borders.bottom + Borders.left
    assertEquals(Borders.all.bits, combined.bits)
  }

  @Test
  fun `plus is idempotent for same side`() {
    val doubled = Borders.top + Borders.top
    assertEquals(Borders.top.bits, doubled.bits)
  }

  @Test
  fun `plus none leaves bits unchanged`() {
    val result = Borders.left + Borders.none
    assertEquals(Borders.left.bits, result.bits)
  }
}
