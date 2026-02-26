package com.hyeonslab.katatui

import cnames.structs.KatatuiTerminal
import com.hyeonslab.katatui.cinterop.katatui_terminal_begin_draw
import com.hyeonslab.katatui.cinterop.katatui_terminal_end_draw
import com.hyeonslab.katatui.cinterop.katatui_terminal_free
import com.hyeonslab.katatui.cinterop.katatui_terminal_init
import com.hyeonslab.katatui.cinterop.katatui_terminal_new
import com.hyeonslab.katatui.cinterop.katatui_terminal_restore
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class Terminal : AutoCloseable {
  private val ptr: CPointer<KatatuiTerminal> =
    checkNotNull(katatui_terminal_new()) { "katatui_terminal_new() returned null" }

  fun init() {
    katatui_terminal_init(ptr)
  }

  fun restore() {
    katatui_terminal_restore(ptr)
  }

  fun draw(block: Frame.() -> Unit) {
    val framePtr =
      checkNotNull(katatui_terminal_begin_draw(ptr)) { "begin_draw failed — already drawing?" }
    try {
      Frame(framePtr).block()
    } finally {
      katatui_terminal_end_draw(ptr)
    }
  }

  override fun close() {
    try {
      restore()
    } finally {
      katatui_terminal_free(ptr)
    }
  }
}
