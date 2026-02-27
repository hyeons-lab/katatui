package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.cinterop.Indexed
import com.hyeonslab.katatui.cinterop.KatatuiStyle
import com.hyeonslab.katatui.cinterop.Rgb
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue

@OptIn(ExperimentalForeignApi::class)
internal fun Style.toCValue(): CValue<KatatuiStyle> = cValue {
  when (val c = this@toCValue.fg) {
    is Color.Rgb -> {
      fg = Rgb
      fg_r = c.r
      fg_g = c.g
      fg_b = c.b
    }
    is Color.Indexed -> {
      fg = Indexed
      fg_index = c.index
    }
    else -> fg = c.toColorEnum()
  }
  when (val c = this@toCValue.bg) {
    is Color.Rgb -> {
      bg = Rgb
      bg_r = c.r
      bg_g = c.g
      bg_b = c.b
    }
    is Color.Indexed -> {
      bg = Indexed
      bg_index = c.index
    }
    else -> bg = c.toColorEnum()
  }
  bold = this@toCValue.bold
  italic = this@toCValue.italic
  underlined = this@toCValue.underlined
  dim = this@toCValue.dim
  crossed_out = this@toCValue.crossedOut
}

@OptIn(ExperimentalForeignApi::class)
private fun Color.toColorEnum() =
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
    is Color.Rgb -> error("Color.Rgb must be handled by caller before toColorEnum()")
    is Color.Indexed -> error("Color.Indexed must be handled by caller before toColorEnum()")
  }
