@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiGraphType
import com.hyeonslab.katatui.cinterop.KatatuiGraphType_Bar
import com.hyeonslab.katatui.cinterop.KatatuiGraphType_Line
import com.hyeonslab.katatui.cinterop.KatatuiGraphType_Scatter
import com.hyeonslab.katatui.cinterop.KatatuiMarker
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Bar
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Block
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Braille
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Dot
import com.hyeonslab.katatui.cinterop.KatatuiMarker_HalfBlock
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Quadrant
import com.hyeonslab.katatui.cinterop.katatui_chart_commit_dataset
import com.hyeonslab.katatui.cinterop.katatui_chart_dataset_point
import com.hyeonslab.katatui.cinterop.katatui_chart_set_dataset_graph_type
import com.hyeonslab.katatui.cinterop.katatui_chart_set_dataset_marker
import com.hyeonslab.katatui.cinterop.katatui_chart_set_dataset_style
import com.hyeonslab.katatui.cinterop.katatui_chart_set_style
import com.hyeonslab.katatui.cinterop.katatui_chart_set_x_style
import com.hyeonslab.katatui.cinterop.katatui_chart_set_y_style
import com.hyeonslab.katatui.cinterop.katatui_chart_x_bounds
import com.hyeonslab.katatui.cinterop.katatui_chart_y_bounds
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.widgets.toCValue
import kotlinx.cinterop.ExperimentalForeignApi

/** Adds a data point (x, y) to the current dataset being built. */
fun Chart.datasetPoint(x: Double, y: Double) {
  katatui_chart_dataset_point(ptr, x, y)
}

/** Sets the graph type of the current dataset. */
fun Chart.setDatasetGraphType(graphType: GraphType) {
  katatui_chart_set_dataset_graph_type(ptr, graphType.toCGraphType())
}

/** Sets the marker of the current dataset. */
fun Chart.setDatasetMarker(marker: ChartMarker) {
  katatui_chart_set_dataset_marker(ptr, marker.toCMarker())
}

/** Sets the style of the current dataset. */
fun Chart.setDatasetStyle(style: Style) {
  katatui_chart_set_dataset_style(ptr, style.toCValue())
}

/** Commits the current dataset and resets the accumulator. */
fun Chart.commitDataset() {
  katatui_chart_commit_dataset(ptr)
}

/** Sets x-axis bounds [min, max]. */
fun Chart.xBounds(min: Double, max: Double) {
  katatui_chart_x_bounds(ptr, min, max)
}

/** Sets y-axis bounds [min, max]. */
fun Chart.yBounds(min: Double, max: Double) {
  katatui_chart_y_bounds(ptr, min, max)
}

fun Chart.setXStyle(style: Style) {
  katatui_chart_set_x_style(ptr, style.toCValue())
}

fun Chart.setYStyle(style: Style) {
  katatui_chart_set_y_style(ptr, style.toCValue())
}

fun Chart.setStyle(style: Style) {
  katatui_chart_set_style(ptr, style.toCValue())
}

enum class GraphType {
  Scatter,
  Line,
  Bar,
}

enum class ChartMarker {
  Dot,
  Block,
  Bar,
  Braille,
  HalfBlock,
  Quadrant,
}

internal fun GraphType.toCGraphType(): KatatuiGraphType =
  when (this) {
    GraphType.Scatter -> KatatuiGraphType_Scatter
    GraphType.Line -> KatatuiGraphType_Line
    GraphType.Bar -> KatatuiGraphType_Bar
  }

internal fun ChartMarker.toCMarker(): KatatuiMarker =
  when (this) {
    ChartMarker.Dot -> KatatuiMarker_Dot
    ChartMarker.Block -> KatatuiMarker_Block
    ChartMarker.Bar -> KatatuiMarker_Bar
    ChartMarker.Braille -> KatatuiMarker_Braille
    ChartMarker.HalfBlock -> KatatuiMarker_HalfBlock
    ChartMarker.Quadrant -> KatatuiMarker_Quadrant
  }
