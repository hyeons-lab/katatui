package com.hyeonslab.katatui.widgets

data class Style(
  val fg: Color = Color.Reset,
  val bg: Color = Color.Reset,
  val bold: Boolean = false,
  val italic: Boolean = false,
  val underlined: Boolean = false,
  val dim: Boolean = false,
  val crossedOut: Boolean = false,
) {
  companion object {
    val RESET = Style()
  }
}
