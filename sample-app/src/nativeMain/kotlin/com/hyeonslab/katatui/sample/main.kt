package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.BarChart
import com.hyeonslab.katatui.Block
import com.hyeonslab.katatui.Clear
import com.hyeonslab.katatui.Constraint
import com.hyeonslab.katatui.Frame
import com.hyeonslab.katatui.Gauge
import com.hyeonslab.katatui.Layout
import com.hyeonslab.katatui.LineGauge
import com.hyeonslab.katatui.Paragraph
import com.hyeonslab.katatui.Rect
import com.hyeonslab.katatui.Sparkline
import com.hyeonslab.katatui.Table
import com.hyeonslab.katatui.Tabs
import com.hyeonslab.katatui.Terminal
import com.hyeonslab.katatui.addWidth
import com.hyeonslab.katatui.bar
import com.hyeonslab.katatui.cinterop.katatui_event_poll
import com.hyeonslab.katatui.cinterop.katatui_event_read_key_code
import com.hyeonslab.katatui.draw
import com.hyeonslab.katatui.init
import com.hyeonslab.katatui.nextRow
import com.hyeonslab.katatui.widgets.Borders
import kotlinx.cinterop.ExperimentalForeignApi

private const val KEY_Q = 'q'.code.toByte()
private const val KEY_1 = '1'.code.toByte()
private const val KEY_2 = '2'.code.toByte()
private const val KEY_LEFT: Byte = -13 // 0xF3 as signed byte
private const val KEY_RIGHT: Byte = -12 // 0xF4 as signed byte

private val TAB_NAMES = listOf("Dashboard", "Widgets")

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
  Terminal().use { terminal ->
    terminal.init()

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

      terminal.draw {
        val areas = Layout.vertical(Constraint.Length(3), Constraint.Fill(1)).split(size)
        val tabRow = areas[0]
        val content = areas[1]

        // Tab bar
        Tabs {
            TAB_NAMES.forEach { addTitle(it) }
            selected = activeTab.toUInt()
          }
          .use { render(it, tabRow) }

        // Clear the content area on every frame to prevent cross-tab artifacts
        Clear().use { render(it, content) }

        when (activeTab) {
          0 -> renderDashboard(content, history, cpuPct, memPct)
          1 -> renderWidgetTable(content)
        }
      }

      if (katatui_event_poll(100u)) {
        when (katatui_event_read_key_code().toByte()) {
          KEY_Q -> break
          KEY_1 -> activeTab = 0
          KEY_2 -> activeTab = 1
          KEY_LEFT -> if (activeTab > 0) activeTab--
          KEY_RIGHT -> if (activeTab < TAB_NAMES.lastIndex) activeTab++
        }
      }
    }
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
  Block {
      title = "Activity"
      borders = Borders.ALL.bits
    }
    .use { render(it, sparkArea) }
  Sparkline {
      history.forEach { addData(it) }
      max = 100uL
    }
    .use { render(it, sparkArea.inner()) }

  // Gauge rows stacked in the left column
  val gaugeRows =
    Layout.vertical(Constraint.Length(3), Constraint.Length(3), Constraint.Fill(1)).split(gaugeCol)
  val cpuRow = gaugeRows[0]
  val memRow = gaugeRows[1]

  Block {
      title = "CPU  ${cpuPct}%"
      borders = Borders.ALL.bits
    }
    .use { render(it, cpuRow) }
  Gauge { percent = cpuPct }.use { render(it, cpuRow.inner()) }

  Block {
      title = "Mem  ${memPct}%"
      borders = Borders.ALL.bits
    }
    .use { render(it, memRow) }
  LineGauge { percent = memPct }.use { render(it, memRow.inner()) }

  // Bar chart in the right column
  Block {
      title = "Downloads"
      borders = Borders.ALL.bits
    }
    .use { render(it, barCol) }
  BarChart {
      barWidth = 5u
      barGap = 1u
      bar("Rust", 82uL)
      bar("KMP", 65uL)
      bar("Swift", 47uL)
      bar("Go", 71uL)
    }
    .use { render(it, barCol.inner()) }

  // Help text
  Paragraph("◄ ►  or  1 / 2 : switch tabs     q : quit").use { render(it, helpArea) }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderWidgetTable(area: Rect) {
  Block {
      title = "Katatui Widgets"
      borders = Borders.ALL.bits
    }
    .use { render(it, area) }
  Table {
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
    .use { render(it, area.inner()) }
}

/** Returns the area one cell inside the border of this rect. */
private fun Rect.inner(): Rect =
  Rect(
    (x + 1u).toUShort(),
    (y + 1u).toUShort(),
    maxOf(0, width.toInt() - 2).toUShort(),
    maxOf(0, height.toInt() - 2).toUShort(),
  )
