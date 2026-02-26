package com.hyeonslab.katatui

import cnames.structs.KatatuiListState
import com.hyeonslab.katatui.cinterop.katatui_list_state_free
import com.hyeonslab.katatui.cinterop.katatui_list_state_new
import com.hyeonslab.katatui.cinterop.katatui_list_state_select
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class ListState internal constructor(internal val ptr: CPointer<KatatuiListState>) : AutoCloseable {
  var selected: Int = -1
    set(value) {
      field = value
      katatui_list_state_select(ptr, value)
    }

  override fun close() {
    katatui_list_state_free(ptr)
  }

  companion object {
    operator fun invoke(): ListState =
      ListState(checkNotNull(katatui_list_state_new()) { "katatui_list_state_new() returned null" })
  }
}
