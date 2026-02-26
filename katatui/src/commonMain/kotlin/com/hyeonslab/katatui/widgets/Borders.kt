package com.hyeonslab.katatui.widgets

value class Borders(val bits: UInt) {
  operator fun plus(other: Borders): Borders = Borders(bits or other.bits)

  companion object {
    val NONE = Borders(0u)
    val TOP = Borders(1u)
    val RIGHT = Borders(2u)
    val BOTTOM = Borders(4u)
    val LEFT = Borders(8u)
    val ALL = Borders(15u)
  }
}
