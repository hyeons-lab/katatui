package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_block_set_style
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun Block.setStyle(style: Style) {
  katatui_block_set_style(ptr, style.toCValue())
}
