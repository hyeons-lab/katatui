@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiMarker
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Bar
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Block
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Braille
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Dot
import com.hyeonslab.katatui.cinterop.KatatuiMarker_HalfBlock
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Quadrant
import com.hyeonslab.katatui.cinterop.katatui_canvas_begin_points
import com.hyeonslab.katatui.cinterop.katatui_canvas_circle
import com.hyeonslab.katatui.cinterop.katatui_canvas_commit_points
import com.hyeonslab.katatui.cinterop.katatui_canvas_line
import com.hyeonslab.katatui.cinterop.katatui_canvas_point
import com.hyeonslab.katatui.cinterop.katatui_canvas_rectangle
import com.hyeonslab.katatui.cinterop.katatui_canvas_set_marker
import com.hyeonslab.katatui.cinterop.katatui_canvas_x_bounds
import com.hyeonslab.katatui.cinterop.katatui_canvas_y_bounds
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

fun Canvas.setMarker(marker: CanvasMarker) {
  katatui_canvas_set_marker(ptr, marker.toCMarker())
}

fun Canvas.xBounds(min: Double, max: Double) {
  katatui_canvas_x_bounds(ptr, min, max)
}

fun Canvas.yBounds(min: Double, max: Double) {
  katatui_canvas_y_bounds(ptr, min, max)
}

fun Canvas.circle(x: Double, y: Double, radius: Double, color: Style) {
  katatui_canvas_circle(ptr, x, y, radius, color.toCValue())
}

fun Canvas.line(x1: Double, y1: Double, x2: Double, y2: Double, color: Style) {
  katatui_canvas_line(ptr, x1, y1, x2, y2, color.toCValue())
}

fun Canvas.rectangle(x: Double, y: Double, width: Double, height: Double, color: Style) {
  katatui_canvas_rectangle(ptr, x, y, width, height, color.toCValue())
}

/** Begins a points batch with the given color. Append individual points with [point]. */
fun Canvas.beginPoints(color: Style) {
  katatui_canvas_begin_points(ptr, color.toCValue())
}

fun Canvas.point(x: Double, y: Double) {
  katatui_canvas_point(ptr, x, y)
}

fun Canvas.commitPoints() {
  katatui_canvas_commit_points(ptr)
}

enum class CanvasMarker {
  Dot,
  Block,
  Bar,
  Braille,
  HalfBlock,
  Quadrant,
}

internal fun CanvasMarker.toCMarker(): KatatuiMarker =
  when (this) {
    CanvasMarker.Dot -> KatatuiMarker_Dot
    CanvasMarker.Block -> KatatuiMarker_Block
    CanvasMarker.Bar -> KatatuiMarker_Bar
    CanvasMarker.Braille -> KatatuiMarker_Braille
    CanvasMarker.HalfBlock -> KatatuiMarker_HalfBlock
    CanvasMarker.Quadrant -> KatatuiMarker_Quadrant
  }
