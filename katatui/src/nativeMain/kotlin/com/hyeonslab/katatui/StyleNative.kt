package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.cinterop.KatatuiColor
import com.hyeonslab.katatui.cinterop.KatatuiStyle
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue

@OptIn(ExperimentalForeignApi::class)
internal fun Style.toCValue(): CValue<KatatuiStyle> = cValue {
  fg = this@toCValue.fg.toCValue()
  bg = this@toCValue.bg.toCValue()
  bold = this@toCValue.bold
  italic = this@toCValue.italic
  underlined = this@toCValue.underlined
  dim = this@toCValue.dim
  crossed_out = this@toCValue.crossedOut
}

@OptIn(ExperimentalForeignApi::class)
internal fun Color.toCValue(): KatatuiColor =
  when (this) {
    Color.Reset -> com.hyeonslab.katatui.cinterop.Reset
    Color.Black -> com.hyeonslab.katatui.cinterop.Black
    Color.Red -> com.hyeonslab.katatui.cinterop.Red
    Color.Green -> com.hyeonslab.katatui.cinterop.Green
    Color.Yellow -> com.hyeonslab.katatui.cinterop.Yellow
    Color.Blue -> com.hyeonslab.katatui.cinterop.Blue
    Color.Magenta -> com.hyeonslab.katatui.cinterop.Magenta
    Color.Cyan -> com.hyeonslab.katatui.cinterop.Cyan
    Color.Gray -> com.hyeonslab.katatui.cinterop.Gray
    Color.DarkGray -> com.hyeonslab.katatui.cinterop.DarkGray
    Color.LightRed -> com.hyeonslab.katatui.cinterop.LightRed
    Color.LightGreen -> com.hyeonslab.katatui.cinterop.LightGreen
    Color.LightYellow -> com.hyeonslab.katatui.cinterop.LightYellow
    Color.LightBlue -> com.hyeonslab.katatui.cinterop.LightBlue
    Color.LightMagenta -> com.hyeonslab.katatui.cinterop.LightMagenta
    Color.LightCyan -> com.hyeonslab.katatui.cinterop.LightCyan
    Color.White -> com.hyeonslab.katatui.cinterop.White
    is Color.Rgb -> com.hyeonslab.katatui.cinterop.Rgb
    is Color.Indexed -> com.hyeonslab.katatui.cinterop.Indexed
  }
