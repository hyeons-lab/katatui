package com.hyeonslab.katatui

import cnames.structs.KatatuiFrame
import com.hyeonslab.katatui.cinterop.katatui_frame_render_bar_chart
import com.hyeonslab.katatui.cinterop.katatui_frame_render_block
import com.hyeonslab.katatui.cinterop.katatui_frame_render_clear
import com.hyeonslab.katatui.cinterop.katatui_frame_render_gauge
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

  fun render(widget: Block, area: Rect) {
    katatui_frame_render_block(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Paragraph, area: Rect) {
    katatui_frame_render_paragraph(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: List, area: Rect, state: ListState) {
    katatui_frame_render_list(ptr, area.toCValue(), widget.ptr, state.ptr)
  }

  fun render(widget: Clear, area: Rect) {
    katatui_frame_render_clear(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Gauge, area: Rect) {
    katatui_frame_render_gauge(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: LineGauge, area: Rect) {
    katatui_frame_render_line_gauge(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Sparkline, area: Rect) {
    katatui_frame_render_sparkline(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: BarChart, area: Rect) {
    katatui_frame_render_bar_chart(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Tabs, area: Rect) {
    katatui_frame_render_tabs(ptr, area.toCValue(), widget.ptr)
  }

  fun render(widget: Table, area: Rect) {
    katatui_frame_render_table(ptr, area.toCValue(), widget.ptr)
  }
}
