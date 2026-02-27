package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.Constraint
import com.hyeonslab.katatui.Frame
import com.hyeonslab.katatui.ImageState
import com.hyeonslab.katatui.KEY_LEFT
import com.hyeonslab.katatui.KEY_RIGHT
import com.hyeonslab.katatui.Layout
import com.hyeonslab.katatui.Rect
import com.hyeonslab.katatui.addWidth
import com.hyeonslab.katatui.bar
import com.hyeonslab.katatui.barChart
import com.hyeonslab.katatui.block
import com.hyeonslab.katatui.clear
import com.hyeonslab.katatui.draw
import com.hyeonslab.katatui.gauge
import com.hyeonslab.katatui.image
import com.hyeonslab.katatui.inner
import com.hyeonslab.katatui.lineGauge
import com.hyeonslab.katatui.nextRow
import com.hyeonslab.katatui.paragraph
import com.hyeonslab.katatui.poll
import com.hyeonslab.katatui.readKey
import com.hyeonslab.katatui.sparkline
import com.hyeonslab.katatui.style
import com.hyeonslab.katatui.table
import com.hyeonslab.katatui.tabs
import com.hyeonslab.katatui.terminal
import com.hyeonslab.katatui.widgets.Borders
import com.hyeonslab.katatui.widgets.Color
import com.hyeonslab.katatui.widgets.Style
import kotlinx.cinterop.ExperimentalForeignApi

private val TAB_NAMES = listOf("Dashboard", "Widgets", "Image")

// Sine-shaped wave cycling through 20 values (0–100 range)
private val WAVE =
  ulongArrayOf(
    50uL,
    65uL,
    78uL,
    88uL,
    95uL,
    98uL,
    95uL,
    88uL,
    78uL,
    65uL,
    50uL,
    35uL,
    22uL,
    12uL,
    5uL,
    2uL,
    5uL,
    12uL,
    22uL,
    35uL,
  )

private val WIDGET_ROWS =
  listOf(
    listOf("Block", "Container", "Session 1", "Bordered region with an optional title"),
    listOf("Paragraph", "Text", "Session 1", "Line-wrapped text content"),
    listOf("List", "Selection", "Session 1", "Scrollable list of items"),
    listOf("Clear", "Utility", "Session 3", "Erases an area before redraw"),
    listOf("Gauge", "Progress", "Session 3", "Filled horizontal percentage bar"),
    listOf("LineGauge", "Progress", "Session 3", "Thin ratio bar (filled / unfilled)"),
    listOf("Sparkline", "Chart", "Session 3", "Compact time-series bar chart"),
    listOf("BarChart", "Chart", "Session 3", "Categorical vertical bar chart"),
    listOf("Tabs", "Navigation", "Session 3", "Tabbed section switcher"),
    listOf("Table", "Grid", "Session 3", "Multi-column data grid with headers"),
  )

@OptIn(ExperimentalForeignApi::class)
fun main() {
  terminal {
    // LEAP_DARK_PNG is embedded at build time from sample-app/leap-dark.png.
    // To use a different image, add it to generateResources in sample-app/build.gradle.kts.
    val imageState = ImageState.fromBytes(LEAP_DARK_PNG)

    var activeTab = 0
    var tick = 0
    val history = ArrayDeque<ULong>(40)

    while (true) {
      // Advance simulation state
      tick++
      history.addLast(WAVE[tick % WAVE.size])
      if (history.size > 40) history.removeFirst()

      val cpuPct = (tick * 3 % 101).toUByte()
      // Mem is offset by a half-period so the two gauges move in opposite directions
      val memPct = WAVE[(tick + WAVE.size / 2) % WAVE.size].toUByte()

      draw {
        block(size) { style = Style(bg = Color.Rgb(41u, 44u, 51u)) }
        val areas = Layout.vertical(Constraint.Length(3), Constraint.Fill(1)).split(size)
        val tabRow = areas[0]
        val content = areas[1]

        // Tab bar
        tabs(tabRow) {
          TAB_NAMES.forEach { addTitle(it) }
          selected = activeTab.toUInt()
        }

        // Clear the content area on every frame to prevent cross-tab artifacts
        clear(content)

        when (activeTab) {
          0 -> renderDashboard(content, history, cpuPct, memPct)
          1 -> renderWidgetTable(content)
          2 -> renderImageTab(content, imageState)
        }
      }

      if (poll()) {
        when (readKey()) {
          'q' -> break
          '1' -> activeTab = 0
          '2' -> activeTab = 1
          '3' -> activeTab = 2
          KEY_LEFT -> if (activeTab > 0) activeTab--
          KEY_RIGHT -> if (activeTab < TAB_NAMES.lastIndex) activeTab++
        }
      }
    }

    imageState?.close()
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderDashboard(
  area: Rect,
  history: Collection<ULong>,
  cpuPct: UByte,
  memPct: UByte,
) {
  val sections =
    Layout.vertical(
        Constraint.Length(5), // Sparkline
        Constraint.Fill(1), // Gauges + BarChart
        Constraint.Length(1), // Help line
      )
      .split(area)
  val sparkArea = sections[0]
  val middleArea = sections[1]
  val helpArea = sections[2]

  val cols = Layout.horizontal(Constraint.Percentage(45), Constraint.Fill(1)).split(middleArea)
  val gaugeCol = cols[0]
  val barCol = cols[1]

  // Sparkline — shows the rolling history as a compact bar chart
  block(sparkArea) {
    title = "Activity"
    borders = Borders.all.bits
  }
  sparkline(sparkArea.inner()) {
    history.forEach { addData(it) }
    max = 100uL
  }

  // Gauge rows stacked in the left column
  val gaugeRows =
    Layout.vertical(Constraint.Length(3), Constraint.Length(3), Constraint.Fill(1)).split(gaugeCol)
  val cpuRow = gaugeRows[0]
  val memRow = gaugeRows[1]

  block(cpuRow) {
    title = "CPU  ${cpuPct}%"
    borders = Borders.all.bits
  }
  gauge(cpuRow.inner()) { percent = cpuPct }

  block(memRow) {
    title = "Mem  ${memPct}%"
    borders = Borders.all.bits
  }
  lineGauge(memRow.inner()) { percent = memPct }

  // Bar chart in the right column
  block(barCol) {
    title = "Downloads"
    borders = Borders.all.bits
  }
  barChart(barCol.inner()) {
    barWidth = 5u
    barGap = 1u
    bar("Rust", 82uL)
    bar("KMP", 65uL)
    bar("Swift", 47uL)
    bar("Go", 71uL)
  }

  // Help text
  paragraph {
    text = "◄ ►  or  1 / 2 / 3 : switch tabs     q : quit"
    this.area = helpArea
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderWidgetTable(area: Rect) {
  block(area) {
    title = "Katatui Widgets"
    borders = Borders.all.bits
  }
  table(area.inner()) {
    addHeader("Widget")
    addHeader("Category")
    addHeader("Added")
    addHeader("Description")
    WIDGET_ROWS.forEach { row ->
      row.forEach { addCell(it) }
      nextRow()
    }
    addWidth(Constraint.Percentage(13))
    addWidth(Constraint.Percentage(13))
    addWidth(Constraint.Percentage(12))
    addWidth(Constraint.Fill(1))
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderImageTab(area: Rect, imageState: ImageState?) {
  block(area) {
    title = "Image"
    borders = Borders.all.bits
  }
  if (imageState != null) {
    image(imageState, area.inner())
  } else {
    paragraph {
      text =
        "Image could not be decoded.\n\nAdd the file to sample-app/ and register it\nin generateResources inside build.gradle.kts."
      this.area = area.inner()
    }
  }
}
