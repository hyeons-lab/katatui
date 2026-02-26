@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_bar_chart_bar
import kotlinx.cinterop.ExperimentalForeignApi

fun BarChart.bar(label: String, value: ULong) {
  katatui_bar_chart_bar(ptr, label, value)
}
