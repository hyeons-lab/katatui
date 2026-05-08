package com.hyeonslab.katatui

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RectTest {
  // --- inner(): edge cases ---

  @Test
  fun `inner with zero width returns zero-size rect preserving origin`() {
    Rect(5u, 10u, 0u, 8u).inner() shouldBe Rect(5u, 10u, 0u, 0u)
  }

  @Test
  fun `inner with zero height returns zero-size rect preserving origin`() {
    Rect(5u, 10u, 8u, 0u).inner() shouldBe Rect(5u, 10u, 0u, 0u)
  }

  @Test
  fun `inner with width 1 returns zero-size rect`() {
    Rect(0u, 0u, 1u, 10u).inner() shouldBe Rect(0u, 0u, 0u, 0u)
  }

  @Test
  fun `inner with height 1 returns zero-size rect`() {
    Rect(0u, 0u, 10u, 1u).inner() shouldBe Rect(0u, 0u, 0u, 0u)
  }

  @Test
  fun `inner with width 2 and height 2 produces zero inner size`() {
    Rect(3u, 7u, 2u, 2u).inner() shouldBe Rect(4u, 8u, 0u, 0u)
  }

  @Test
  fun `inner with width 3 and height 3 produces 1x1 inner`() {
    Rect(0u, 0u, 3u, 3u).inner() shouldBe Rect(1u, 1u, 1u, 1u)
  }

  @Test
  fun `inner normal case shrinks by 1 on each side`() {
    Rect(2u, 3u, 10u, 8u).inner() shouldBe Rect(3u, 4u, 8u, 6u)
  }

  @Test
  fun `inner at origin with large size`() {
    Rect(0u, 0u, 100u, 50u).inner() shouldBe Rect(1u, 1u, 98u, 48u)
  }
}
