package com.hyeonslab.katatui.widgets

class Borders(val bits: UInt) {
  operator fun plus(other: Borders): Borders = Borders(bits or other.bits)

  companion object {
    val none = Borders(0u)
    val top = Borders(1u)
    val right = Borders(2u)
    val bottom = Borders(4u)
    val left = Borders(8u)
    val all = Borders(15u)
  }
}
