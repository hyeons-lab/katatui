@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.EyeDefault
import com.hyeonslab.katatui.cinterop.EyeRed
import com.hyeonslab.katatui.cinterop.KatatuiLogoSize
import com.hyeonslab.katatui.cinterop.KatatuiMascotEyeColor
import com.hyeonslab.katatui.cinterop.Small
import com.hyeonslab.katatui.cinterop.Tiny
import com.hyeonslab.katatui.cinterop.katatui_logo_set_size
import com.hyeonslab.katatui.cinterop.katatui_mascot_set_eye_color
import kotlinx.cinterop.ExperimentalForeignApi

enum class LogoSize {
  Tiny,
  Small,
}

enum class MascotEyeColor {
  Default,
  Red,
}

fun Logo.setSize(size: LogoSize) {
  val cSize: KatatuiLogoSize =
    when (size) {
      LogoSize.Tiny -> Tiny
      LogoSize.Small -> Small
    }
  katatui_logo_set_size(ptr, cSize)
}

fun Mascot.setEyeColor(color: MascotEyeColor) {
  val cColor: KatatuiMascotEyeColor =
    when (color) {
      MascotEyeColor.Default -> EyeDefault
      MascotEyeColor.Red -> EyeRed
    }
  katatui_mascot_set_eye_color(ptr, cColor)
}
