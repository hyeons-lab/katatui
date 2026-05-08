package com.hyeonslab.katatui.widgets

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BordersTest {
  @Test
  fun `none has bits 0`() {
    Borders.none.bits shouldBe 0u
  }

  @Test
  fun `individual side bits are powers of two`() {
    Borders.top.bits shouldBe 1u
    Borders.right.bits shouldBe 2u
    Borders.bottom.bits shouldBe 4u
    Borders.left.bits shouldBe 8u
  }

  @Test
  fun `all has bits 15 equal to union of all sides`() {
    Borders.all.bits shouldBe 15u
  }

  @Test
  fun `plus operator ORs bits`() {
    val topRight = Borders.top + Borders.right
    topRight.bits shouldBe 3u
  }

  @Test
  fun `plus of all four sides equals all`() {
    val combined = Borders.top + Borders.right + Borders.bottom + Borders.left
    combined.bits shouldBe Borders.all.bits
  }

  @Test
  fun `plus is idempotent for same side`() {
    val doubled = Borders.top + Borders.top
    doubled.bits shouldBe Borders.top.bits
  }

  @Test
  fun `plus none leaves bits unchanged`() {
    val result = Borders.left + Borders.none
    result.bits shouldBe Borders.left.bits
  }
}
