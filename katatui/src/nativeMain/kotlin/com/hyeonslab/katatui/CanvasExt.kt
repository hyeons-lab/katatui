@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiMarker
import com.hyeonslab.katatui.cinterop.MarkerBar as Bar
import com.hyeonslab.katatui.cinterop.MarkerBlock as Block
import com.hyeonslab.katatui.cinterop.MarkerBraille as Braille
import com.hyeonslab.katatui.cinterop.MarkerDot as Dot
import com.hyeonslab.katatui.cinterop.MarkerHalfBlock as HalfBlock
import com.hyeonslab.katatui.cinterop.MarkerQuadrant as Quadrant
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

enum class CanvasMarker {
  Dot,
  Block,
  Bar,
  Braille,
  HalfBlock,
  Quadrant,
}

fun Canvas.setMarker(marker: CanvasMarker) {
  val cMarker: KatatuiMarker =
    when (marker) {
      CanvasMarker.Dot -> Dot
      CanvasMarker.Block -> Block
      CanvasMarker.Bar -> Bar
      CanvasMarker.Braille -> Braille
      CanvasMarker.HalfBlock -> HalfBlock
      CanvasMarker.Quadrant -> Quadrant
    }
  katatui_canvas_set_marker(ptr, cMarker)
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
