package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiRect
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue

data class Rect(val x: UShort, val y: UShort, val width: UShort, val height: UShort) {
    companion object {
        val ZERO = Rect(0u, 0u, 0u, 0u)
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun CValue<KatatuiRect>.toKotlin(): Rect {
    val r = this.useContents { this }
    return Rect(r.x, r.y, r.width, r.height)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Rect.toCValue(): CValue<KatatuiRect> = cValue<KatatuiRect> {
    x = this@toCValue.x
    y = this@toCValue.y
    width = this@toCValue.width
    height = this@toCValue.height
}
