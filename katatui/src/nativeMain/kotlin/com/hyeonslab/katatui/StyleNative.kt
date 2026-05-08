@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.cinterop.KatatuiColor_Black
import com.hyeonslab.katatui.cinterop.KatatuiColor_Blue
import com.hyeonslab.katatui.cinterop.KatatuiColor_Cyan
import com.hyeonslab.katatui.cinterop.KatatuiColor_DarkGray
import com.hyeonslab.katatui.cinterop.KatatuiColor_Gray
import com.hyeonslab.katatui.cinterop.KatatuiColor_Green
import com.hyeonslab.katatui.cinterop.KatatuiColor_Indexed
import com.hyeonslab.katatui.cinterop.KatatuiColor_LightBlue
import com.hyeonslab.katatui.cinterop.KatatuiColor_LightCyan
import com.hyeonslab.katatui.cinterop.KatatuiColor_LightGreen
import com.hyeonslab.katatui.cinterop.KatatuiColor_LightMagenta
import com.hyeonslab.katatui.cinterop.KatatuiColor_LightRed
import com.hyeonslab.katatui.cinterop.KatatuiColor_LightYellow
import com.hyeonslab.katatui.cinterop.KatatuiColor_Magenta
import com.hyeonslab.katatui.cinterop.KatatuiColor_Red
import com.hyeonslab.katatui.cinterop.KatatuiColor_Reset
import com.hyeonslab.katatui.cinterop.KatatuiColor_Rgb
import com.hyeonslab.katatui.cinterop.KatatuiColor_White
import com.hyeonslab.katatui.cinterop.KatatuiColor_Yellow
import com.hyeonslab.katatui.cinterop.KatatuiStyle
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue

internal fun Style.toCValue(): CValue<KatatuiStyle> = cValue {
  when (val c = this@toCValue.fg) {
    is Color.Rgb -> {
      fg = KatatuiColor_Rgb
      fg_r = c.r
      fg_g = c.g
      fg_b = c.b
    }
    is Color.Indexed -> {
      fg = KatatuiColor_Indexed
      fg_index = c.index
    }
    else -> fg = c.toColorEnum()
  }
  when (val c = this@toCValue.bg) {
    is Color.Rgb -> {
      bg = KatatuiColor_Rgb
      bg_r = c.r
      bg_g = c.g
      bg_b = c.b
    }
    is Color.Indexed -> {
      bg = KatatuiColor_Indexed
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

private fun Color.toColorEnum() =
  when (this) {
    Color.Reset -> KatatuiColor_Reset
    Color.Black -> KatatuiColor_Black
    Color.Red -> KatatuiColor_Red
    Color.Green -> KatatuiColor_Green
    Color.Yellow -> KatatuiColor_Yellow
    Color.Blue -> KatatuiColor_Blue
    Color.Magenta -> KatatuiColor_Magenta
    Color.Cyan -> KatatuiColor_Cyan
    Color.Gray -> KatatuiColor_Gray
    Color.DarkGray -> KatatuiColor_DarkGray
    Color.LightRed -> KatatuiColor_LightRed
    Color.LightGreen -> KatatuiColor_LightGreen
    Color.LightYellow -> KatatuiColor_LightYellow
    Color.LightBlue -> KatatuiColor_LightBlue
    Color.LightMagenta -> KatatuiColor_LightMagenta
    Color.LightCyan -> KatatuiColor_LightCyan
    Color.White -> KatatuiColor_White
    is Color.Rgb -> error("Color.Rgb must be handled by caller before toColorEnum()")
    is Color.Indexed -> error("Color.Indexed must be handled by caller before toColorEnum()")
  }
