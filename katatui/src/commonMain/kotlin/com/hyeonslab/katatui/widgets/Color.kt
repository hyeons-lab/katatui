package com.hyeonslab.katatui.widgets

sealed class Color {
  object Reset : Color()

  object Black : Color()

  object Red : Color()

  object Green : Color()

  object Yellow : Color()

  object Blue : Color()

  object Magenta : Color()

  object Cyan : Color()

  object Gray : Color()

  object DarkGray : Color()

  object LightRed : Color()

  object LightGreen : Color()

  object LightYellow : Color()

  object LightBlue : Color()

  object LightMagenta : Color()

  object LightCyan : Color()

  object White : Color()

  data class Rgb(val r: UByte, val g: UByte, val b: UByte) : Color()

  data class Indexed(val index: UByte) : Color()
}
