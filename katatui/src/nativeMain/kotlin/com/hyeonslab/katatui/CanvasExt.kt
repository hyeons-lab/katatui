@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_canvas_begin_points
import com.hyeonslab.katatui.cinterop.katatui_canvas_circle
import com.hyeonslab.katatui.cinterop.katatui_canvas_clear
import com.hyeonslab.katatui.cinterop.katatui_canvas_commit_points
import com.hyeonslab.katatui.cinterop.katatui_canvas_line
import com.hyeonslab.katatui.cinterop.katatui_canvas_point
import com.hyeonslab.katatui.cinterop.katatui_canvas_rectangle
import com.hyeonslab.katatui.cinterop.katatui_canvas_set_marker
import com.hyeonslab.katatui.cinterop.katatui_canvas_x_bounds
import com.hyeonslab.katatui.cinterop.katatui_canvas_y_bounds
import com.hyeonslab.katatui.widgets.Color
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

fun Canvas.setMarker(marker: Marker) {
  katatui_canvas_set_marker(ptr, marker.toCMarker())
}

fun Canvas.xBounds(min: Double, max: Double) {
  katatui_canvas_x_bounds(ptr, min, max)
}

fun Canvas.yBounds(min: Double, max: Double) {
  katatui_canvas_y_bounds(ptr, min, max)
}

fun Canvas.clear() {
  katatui_canvas_clear(ptr)
}

fun Canvas.circle(x: Double, y: Double, radius: Double, color: Color) {
  katatui_canvas_circle(ptr, x, y, radius, Style(fg = color).toCValue())
}

fun Canvas.line(x1: Double, y1: Double, x2: Double, y2: Double, color: Color) {
  katatui_canvas_line(ptr, x1, y1, x2, y2, Style(fg = color).toCValue())
}

fun Canvas.rectangle(x: Double, y: Double, width: Double, height: Double, color: Color) {
  katatui_canvas_rectangle(ptr, x, y, width, height, Style(fg = color).toCValue())
}

/** Begins a points batch with the given color. Append individual points with [point]. */
fun Canvas.beginPoints(color: Color) {
  katatui_canvas_begin_points(ptr, Style(fg = color).toCValue())
}

fun Canvas.point(x: Double, y: Double) {
  katatui_canvas_point(ptr, x, y)
}

fun Canvas.commitPoints() {
  katatui_canvas_commit_points(ptr)
}
