package com.hyeonslab.katatui

import cnames.structs.KatatuiTableState
import com.hyeonslab.katatui.cinterop.katatui_table_state_free
import com.hyeonslab.katatui.cinterop.katatui_table_state_new
import com.hyeonslab.katatui.cinterop.katatui_table_state_select
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class TableState internal constructor(internal val ptr: CPointer<KatatuiTableState>) :
  AutoCloseable {
  var selected: Int = -1
    set(value) {
      field = value
      katatui_table_state_select(ptr, value)
    }

  override fun close() {
    katatui_table_state_free(ptr)
  }

  companion object {
    operator fun invoke(): TableState =
      TableState(
        checkNotNull(katatui_table_state_new()) { "katatui_table_state_new() returned null" }
      )
  }
}
