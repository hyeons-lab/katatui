package com.hyeonslab.katatui

import cnames.structs.KatatuiFrame
import com.hyeonslab.katatui.cinterop.katatui_frame_render_bar_chart
import com.hyeonslab.katatui.cinterop.katatui_frame_render_block
import com.hyeonslab.katatui.cinterop.katatui_frame_render_clear
import com.hyeonslab.katatui.cinterop.katatui_frame_render_gauge
import com.hyeonslab.katatui.cinterop.katatui_frame_render_image
import com.hyeonslab.katatui.cinterop.katatui_frame_render_line_gauge
import com.hyeonslab.katatui.cinterop.katatui_frame_render_list
import com.hyeonslab.katatui.cinterop.katatui_frame_render_paragraph
import com.hyeonslab.katatui.cinterop.katatui_frame_render_sparkline
import com.hyeonslab.katatui.cinterop.katatui_frame_render_table
import com.hyeonslab.katatui.cinterop.katatui_frame_render_tabs
import com.hyeonslab.katatui.cinterop.katatui_frame_size
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class Frame internal constructor(private val ptr: CPointer<KatatuiFrame>) {
  val size: Rect
    get() = katatui_frame_size(ptr).toKotlin()

  fun render(widget: Block, area: Rect = size) {
    katatui_frame_render_block(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Paragraph, area: Rect = size) {
    katatui_frame_render_paragraph(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: List, area: Rect = size, state: ListState) {
    katatui_frame_render_list(ptr, area.toCValue(), widget.ptr, state.ptr)
  }

  fun render(widget: Clear, area: Rect = size) {
    katatui_frame_render_clear(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Gauge, area: Rect = size) {
    katatui_frame_render_gauge(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: LineGauge, area: Rect = size) {
    katatui_frame_render_line_gauge(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Sparkline, area: Rect = size) {
    katatui_frame_render_sparkline(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: BarChart, area: Rect = size) {
    katatui_frame_render_bar_chart(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Tabs, area: Rect = size) {
    katatui_frame_render_tabs(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Table, area: Rect = size, state: TableState? = null) {
    katatui_frame_render_table(ptr, area.toCValue(), widget.ptr, state?.ptr)
  }

  fun render(state: ImageState, area: Rect = size) {
    katatui_frame_render_image(ptr, area.toCValue(), state.ptr)
  }
}

fun Frame.image(state: ImageState, area: Rect = size) = render(state, area)

fun Frame.block(area: Rect = size, init: Block.() -> Unit = {}) =
  Block(init).use { render(it, it.area ?: area) }

fun Frame.paragraph(text: String = "", area: Rect = size, init: Paragraph.() -> Unit = {}) =
  Paragraph(text, init).use { render(it, it.area ?: area) }

fun Frame.list(state: ListState, area: Rect = size, init: List.() -> Unit = {}) =
  List(init).use { render(it, it.area ?: area, state) }

fun Frame.clear(area: Rect = size) = Clear().use { render(it, it.area ?: area) }

fun Frame.gauge(area: Rect = size, init: Gauge.() -> Unit = {}) =
  Gauge(init).use { render(it, it.area ?: area) }

fun Frame.lineGauge(area: Rect = size, init: LineGauge.() -> Unit = {}) =
  LineGauge(init).use { render(it, it.area ?: area) }

fun Frame.sparkline(area: Rect = size, init: Sparkline.() -> Unit = {}) =
  Sparkline(init).use { render(it, it.area ?: area) }

fun Frame.barChart(area: Rect = size, init: BarChart.() -> Unit = {}) =
  BarChart(init).use { render(it, it.area ?: area) }

fun Frame.tabs(area: Rect = size, init: Tabs.() -> Unit = {}) =
  Tabs(init).use { render(it, it.area ?: area) }

fun Frame.table(area: Rect = size, state: TableState? = null, init: Table.() -> Unit = {}) =
  Table(init).use { render(it, it.area ?: area, state) }
