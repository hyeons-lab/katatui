package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiRect
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents

data class Rect(val x: UShort, val y: UShort, val width: UShort, val height: UShort) {
  companion object {
    val ZERO = Rect(0u, 0u, 0u, 0u)
  }
}

@OptIn(ExperimentalForeignApi::class)
internal fun CValue<KatatuiRect>.toKotlin(): Rect = useContents { Rect(x, y, width, height) }

@OptIn(ExperimentalForeignApi::class)
internal fun Rect.toCValue(): CValue<KatatuiRect> =
  cValue<KatatuiRect> {
    x = this@toCValue.x
    y = this@toCValue.y
    width = this@toCValue.width
    height = this@toCValue.height
  }
