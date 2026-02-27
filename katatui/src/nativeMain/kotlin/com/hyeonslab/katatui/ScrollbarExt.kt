@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.HorizontalBottom
import com.hyeonslab.katatui.cinterop.HorizontalTop
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation
import com.hyeonslab.katatui.cinterop.VerticalLeft
import com.hyeonslab.katatui.cinterop.VerticalRight
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_begin_style
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_end_style
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_orientation
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_thumb_style
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_set_track_style
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

enum class ScrollbarOrientation {
  VerticalRight,
  VerticalLeft,
  HorizontalBottom,
  HorizontalTop,
}

fun Scrollbar.setOrientation(orientation: ScrollbarOrientation) {
  val cOrientation: KatatuiScrollbarOrientation =
    when (orientation) {
      ScrollbarOrientation.VerticalRight -> VerticalRight
      ScrollbarOrientation.VerticalLeft -> VerticalLeft
      ScrollbarOrientation.HorizontalBottom -> HorizontalBottom
      ScrollbarOrientation.HorizontalTop -> HorizontalTop
    }
  katatui_scrollbar_set_orientation(ptr, cOrientation)
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
