package com.hyeonslab.katatui.widgets

value class Modifier(val bits: UInt) {
  operator fun plus(other: Modifier): Modifier = Modifier(bits or other.bits)

  operator fun minus(other: Modifier): Modifier = Modifier(bits and other.bits.inv())

  companion object {
    val NONE = Modifier(0u)
    val BOLD = Modifier(1u)
    val DIM = Modifier(2u)
    val ITALIC = Modifier(4u)
    val UNDERLINED = Modifier(8u)
    val SLOW_BLINK = Modifier(16u)
    val RAPID_BLINK = Modifier(32u)
    val REVERSED = Modifier(64u)
    val HIDDEN = Modifier(128u)
    val CROSSED_OUT = Modifier(256u)
  }
}
