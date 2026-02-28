package com.hyeonslab.katatui

import cnames.structs.KatatuiScrollbarState
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_state_free
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_state_new
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_state_set_content_length
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_state_set_position
import com.hyeonslab.katatui.cinterop.katatui_scrollbar_state_set_viewport_content_length
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class ScrollbarState internal constructor(internal val ptr: CPointer<KatatuiScrollbarState>) :
  AutoCloseable {

  var contentLength: Int = 0
    set(value) {
      require(value in 0..65535) { "contentLength must be in 0..65535, got $value" }
      field = value
      katatui_scrollbar_state_set_content_length(ptr, value.toUShort())
    }

  var position: Int = 0
    set(value) {
      require(value in 0..65535) { "position must be in 0..65535, got $value" }
      field = value
      katatui_scrollbar_state_set_position(ptr, value.toUShort())
    }

  var viewportContentLength: Int = 0
    set(value) {
      require(value in 0..65535) { "viewportContentLength must be in 0..65535, got $value" }
      field = value
      katatui_scrollbar_state_set_viewport_content_length(ptr, value.toUShort())
    }

  override fun close() {
    katatui_scrollbar_state_free(ptr)
  }

  companion object {
    operator fun invoke(): ScrollbarState =
      ScrollbarState(
        checkNotNull(katatui_scrollbar_state_new()) {
          "katatui_scrollbar_state_new() returned null"
        }
      )
  }
}
