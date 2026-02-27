@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_HorizontalBottom
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_HorizontalTop
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_VerticalLeft
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_VerticalRight
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_begin_style
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_end_style
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_orientation
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_thumb_style
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_track_style
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

fun Scrollbar.setOrientation(orientation: ScrollbarOrientation) {
  katatui_scrollbar_set_orientation(ptr, orientation.toCOrientation())
}

fun Scrollbar.setThumbStyle(style: Style) {
  katatui_scrollbar_set_thumb_style(ptr, style.toCValue())
}

fun Scrollbar.setTrackStyle(style: Style) {
  katatui_scrollbar_set_track_style(ptr, style.toCValue())
}

fun Scrollbar.setBeginStyle(style: Style) {
  katatui_scrollbar_set_begin_style(ptr, style.toCValue())
}

fun Scrollbar.setEndStyle(style: Style) {
  katatui_scrollbar_set_end_style(ptr, style.toCValue())
}

enum class ScrollbarOrientation {
  VerticalRight,
  VerticalLeft,
  HorizontalBottom,
  HorizontalTop,
}

internal fun ScrollbarOrientation.toCOrientation(): KatatuiScrollbarOrientation =
  when (this) {
    ScrollbarOrientation.VerticalRight -> KatatuiScrollbarOrientation_VerticalRight
    ScrollbarOrientation.VerticalLeft -> KatatuiScrollbarOrientation_VerticalLeft
    ScrollbarOrientation.HorizontalBottom -> KatatuiScrollbarOrientation_HorizontalBottom
    ScrollbarOrientation.HorizontalTop -> KatatuiScrollbarOrientation_HorizontalTop
  }
