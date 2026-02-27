package com.hyeonslab.katatui

import kotlin.test.Test
import kotlin.test.assertEquals

class RectTest {
  // --- inner(): edge cases ---

  @Test
  fun `inner with zero width returns zero-size rect preserving origin`() {
    val r = Rect(5u, 10u, 0u, 8u)
    assertEquals(Rect(5u, 10u, 0u, 0u), r.inner())
  }

  @Test
  fun `inner with zero height returns zero-size rect preserving origin`() {
    val r = Rect(5u, 10u, 8u, 0u)
    assertEquals(Rect(5u, 10u, 0u, 0u), r.inner())
  }

  @Test
  fun `inner with width 1 returns zero-size rect`() {
    val r = Rect(0u, 0u, 1u, 10u)
    assertEquals(Rect(0u, 0u, 0u, 0u), r.inner())
  }

  @Test
  fun `inner with height 1 returns zero-size rect`() {
    val r = Rect(0u, 0u, 10u, 1u)
    assertEquals(Rect(0u, 0u, 0u, 0u), r.inner())
  }

  @Test
  fun `inner with width 2 and height 2 produces zero inner size`() {
    val r = Rect(3u, 7u, 2u, 2u)
    assertEquals(Rect(4u, 8u, 0u, 0u), r.inner())
  }

  @Test
  fun `inner with width 3 and height 3 produces 1x1 inner`() {
    val r = Rect(0u, 0u, 3u, 3u)
    assertEquals(Rect(1u, 1u, 1u, 1u), r.inner())
  }

  @Test
  fun `inner normal case shrinks by 1 on each side`() {
    val r = Rect(2u, 3u, 10u, 8u)
    assertEquals(Rect(3u, 4u, 8u, 6u), r.inner())
  }

  @Test
  fun `inner at origin with large size`() {
    val r = Rect(0u, 0u, 100u, 50u)
    assertEquals(Rect(1u, 1u, 98u, 48u), r.inner())
  }
}
