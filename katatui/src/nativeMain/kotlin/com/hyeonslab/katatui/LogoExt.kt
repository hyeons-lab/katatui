@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiLogoSize
import com.hyeonslab.katatui.cinterop.KatatuiLogoSize_Small
import com.hyeonslab.katatui.cinterop.KatatuiLogoSize_Tiny
import com.hyeonslab.katatui.cinterop.KatatuiMascotEyeColor
import com.hyeonslab.katatui.cinterop.KatatuiMascotEyeColor_Default
import com.hyeonslab.katatui.cinterop.KatatuiMascotEyeColor_Red
import com.hyeonslab.katatui.cinterop.katatui_logo_set_size
import com.hyeonslab.katatui.cinterop.katatui_mascot_set_eye_color
import kotlinx.cinterop.ExperimentalForeignApi

fun Logo.setSize(size: LogoSize) {
  katatui_logo_set_size(ptr, size.toCLogoSize())
}

fun Mascot.setEyeColor(color: MascotEyeColor) {
  katatui_mascot_set_eye_color(ptr, color.toCEyeColor())
}

enum class LogoSize {
  Tiny,
  Small,
}

enum class MascotEyeColor {
  Default,
  Red,
}

internal fun LogoSize.toCLogoSize(): KatatuiLogoSize =
  when (this) {
    LogoSize.Tiny -> KatatuiLogoSize_Tiny
    LogoSize.Small -> KatatuiLogoSize_Small
  }

internal fun MascotEyeColor.toCEyeColor(): KatatuiMascotEyeColor =
  when (this) {
    MascotEyeColor.Default -> KatatuiMascotEyeColor_Default
    MascotEyeColor.Red -> KatatuiMascotEyeColor_Red
  }
