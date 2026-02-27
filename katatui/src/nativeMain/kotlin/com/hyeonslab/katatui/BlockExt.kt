package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_block_set_style
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
public var Block.style: Style
  get() = Style() // Not easily retrievable from C opaque pointer without extra FFI
  set(value) {
    katatui_block_set_style(ptr, value.toCValue())
  }
