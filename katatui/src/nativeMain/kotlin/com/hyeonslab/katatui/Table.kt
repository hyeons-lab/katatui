@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiConstraint
import com.hyeonslab.katatui.cinterop.katatui_table_add_width
import com.hyeonslab.katatui.cinterop.katatui_table_next_row
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue

fun Table.nextRow() {
  katatui_table_next_row(ptr)
}

fun Table.addWidth(constraint: Constraint) {
  katatui_table_add_width(
    ptr,
    cValue<KatatuiConstraint> {
      kind = constraint.toCKind()
      value = constraint.value
    },
  )
}
