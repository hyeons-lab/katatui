package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.Constraint
import com.hyeonslab.katatui.Frame
import com.hyeonslab.katatui.GraphType
import com.hyeonslab.katatui.ImageState
import com.hyeonslab.katatui.KEY_LEFT
import com.hyeonslab.katatui.KEY_RIGHT
import com.hyeonslab.katatui.Layout
import com.hyeonslab.katatui.LogoSize
import com.hyeonslab.katatui.Marker
import com.hyeonslab.katatui.MascotEyeColor
import com.hyeonslab.katatui.Rect
import com.hyeonslab.katatui.ScrollbarOrientation
import com.hyeonslab.katatui.ScrollbarState
import com.hyeonslab.katatui.addWidth
import com.hyeonslab.katatui.bar
import com.hyeonslab.katatui.barChart
import com.hyeonslab.katatui.block
import com.hyeonslab.katatui.canvas
import com.hyeonslab.katatui.chart
import com.hyeonslab.katatui.circle
import com.hyeonslab.katatui.clear
import com.hyeonslab.katatui.commitDataset
import com.hyeonslab.katatui.datasetPoint
import com.hyeonslab.katatui.draw
import com.hyeonslab.katatui.gauge
import com.hyeonslab.katatui.image
import com.hyeonslab.katatui.inner
import com.hyeonslab.katatui.line
import com.hyeonslab.katatui.lineGauge
import com.hyeonslab.katatui.logo
import com.hyeonslab.katatui.mascot
import com.hyeonslab.katatui.nextRow
import com.hyeonslab.katatui.paragraph
import com.hyeonslab.katatui.poll
import com.hyeonslab.katatui.readKey
import com.hyeonslab.katatui.rectangle
import com.hyeonslab.katatui.scrollbar
import com.hyeonslab.katatui.setDatasetGraphType
import com.hyeonslab.katatui.setDatasetMarker
import com.hyeonslab.katatui.setDatasetStyle
import com.hyeonslab.katatui.setEyeColor
import com.hyeonslab.katatui.setMarker
import com.hyeonslab.katatui.setOrientation
import com.hyeonslab.katatui.setSize
import com.hyeonslab.katatui.setStyle
import com.hyeonslab.katatui.sparkline
import com.hyeonslab.katatui.table
import com.hyeonslab.katatui.tabs
import com.hyeonslab.katatui.terminal
import com.hyeonslab.katatui.widgets.Borders
import com.hyeonslab.katatui.widgets.Color
import com.hyeonslab.katatui.widgets.Style
import com.hyeonslab.katatui.xBounds
import com.hyeonslab.katatui.yBounds
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.cinterop.ExperimentalForeignApi

private val TAB_NAMES =
  listOf("Dashboard", "Chart", "Canvas", "Scrollbar", "Branding", "Widgets", "Image")

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

private val SCROLL_LINES =
  listOf(
    "Scrollbar widget — vertical right orientation",
    "ScrollbarState tracks three values:",
    "  contentLength         = total number of content lines",
    "  viewportContentLength = number of visible lines",
    "  position              = index of the topmost visible line",
    "",
    "The thumb size and position are computed automatically.",
    "Set all three fields before each call to scrollbar().",
    "",
    "This demo auto-scrolls to show the thumb moving.",
    "",
    "Line 11: Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
    "Line 12: Sed do eiusmod tempor incididunt ut labore et dolore magna.",
    "Line 13: Ut enim ad minim veniam, quis nostrud exercitation ullamco.",
    "Line 14: Duis aute irure dolor in reprehenderit in voluptate velit.",
    "Line 15: Excepteur sint occaecat cupidatat non proident, sunt in.",
    "Line 16: Culpa qui officia deserunt mollit anim id est laborum.",
    "Line 17: Nam libero tempore, cum soluta nobis eligendi optio cumque.",
    "Line 18: Quis autem vel eum iure reprehenderit qui in ea voluptate.",
    "Line 19: Temporibus autem quibusdam et aut officiis debitis rerum.",
    "Line 20: Itaque earum rerum hic tenetur a sapiente delectus ut aut.",
    "Line 21: At vero eos et accusamus et iusto odio dignissimos ducimus.",
    "Line 22: Nam eos qui ratione voluptatem sequi nesciunt, neque porro.",
    "Line 23: Quis dolorem ipsum, quia dolor sit, amet consectetur.",
    "Line 24: Nemo enim ipsam voluptatem quia voluptas sit aspernatur.",
    "Line 25: Neque porro quisquam est qui dolorem ipsum quia amet.",
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
    listOf("Scrollbar", "Navigation", "Session 9", "Scroll indicator with state"),
    listOf("Chart", "Chart", "Session 9", "2D datasets: line, scatter, bar"),
    listOf("Canvas", "Drawing", "Session 9", "Free-form shapes and point clouds"),
    listOf("Logo", "Branding", "Session 9", "ratatui / Katatui logo (Tiny / Small)"),
    listOf("Mascot", "Branding", "Session 9", "Katatui mascot with eye colour variants"),
  )

@OptIn(ExperimentalForeignApi::class)
fun main() {
  terminal {
    // LEAP_DARK_PNG is embedded at build time from sample-app/leap-dark.png.
    // To use a different image, add it to generateResources in sample-app/build.gradle.kts.
    val imageState = ImageState.fromBytes(LEAP_DARK_PNG)
    val scrollbarState = ScrollbarState()

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
        block(size) { setStyle(Style(bg = Color.Rgb(41u, 44u, 51u))) }
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
          1 -> renderChart(content, tick)
          2 -> renderCanvas(content, tick)
          3 -> renderScrollbar(content, scrollbarState, tick)
          4 -> renderBranding(content, tick)
          5 -> renderWidgetTable(content)
          6 -> renderImageTab(content, imageState)
        }
      }

      if (poll()) {
        when (readKey()) {
          'q' -> break
          '1' -> activeTab = 0
          '2' -> activeTab = 1
          '3' -> activeTab = 2
          '4' -> activeTab = 3
          '5' -> activeTab = 4
          '6' -> activeTab = 5
          '7' -> activeTab = 6
          KEY_LEFT -> if (activeTab > 0) activeTab--
          KEY_RIGHT -> if (activeTab < TAB_NAMES.lastIndex) activeTab++
        }
      }
    }

    imageState?.close()
    scrollbarState.close()
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
    text = "◄ ►  or  1–7 : switch tabs     q : quit"
    this.area = helpArea
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderChart(area: Rect, tick: Int) {
  block(area) {
    title = "Chart  │  cyan = sin   yellow = cos"
    borders = Borders.all.bits
  }
  chart(area.inner()) {
    xBounds(0.0, 59.0)
    yBounds(-1.5, 1.5)
    // Sine wave — line graph with Braille markers
    datasetName = "sin"
    for (i in 0..59) datasetPoint(i.toDouble(), sin((i + tick) * 0.2))
    setDatasetGraphType(GraphType.Line)
    setDatasetMarker(Marker.Braille)
    setDatasetStyle(Style(fg = Color.Cyan))
    commitDataset()
    // Cosine — scatter with Dot markers
    datasetName = "cos"
    for (i in 0..59 step 5) datasetPoint(i.toDouble(), cos((i + tick) * 0.2))
    setDatasetGraphType(GraphType.Scatter)
    setDatasetMarker(Marker.Dot)
    setDatasetStyle(Style(fg = Color.Yellow))
    commitDataset()
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderCanvas(area: Rect, tick: Int) {
  block(area) {
    title = "Canvas"
    borders = Borders.all.bits
  }
  canvas(area.inner()) {
    xBounds(0.0, 100.0)
    yBounds(0.0, 100.0)
    setMarker(Marker.Braille)
    // Pulsing circle
    val r = 15.0 + 10.0 * sin(tick * 0.1)
    circle(50.0, 50.0, r, Style(fg = Color.Cyan))
    // 4 rotating spokes
    for (k in 0..3) {
      val angle = tick * 0.05 + k * PI / 2.0
      line(50.0, 50.0, 50.0 + 40.0 * cos(angle), 50.0 + 40.0 * sin(angle), Style(fg = Color.Green))
    }
    // Corner rectangles
    listOf(2.0 to 2.0, 83.0 to 2.0, 2.0 to 88.0, 83.0 to 88.0).forEach { (x, y) ->
      rectangle(x, y, 15.0, 10.0, Style(fg = Color.Yellow))
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderScrollbar(area: Rect, scrollbarState: ScrollbarState, tick: Int) {
  block(area) {
    title = "Scrollbar"
    borders = Borders.all.bits
  }
  val inner = area.inner()
  val viewportLines = inner.height.toInt()
  val maxScroll = (SCROLL_LINES.size - viewportLines).coerceAtLeast(0)
  val offset = if (maxScroll > 0) (tick / 5) % (maxScroll + 1) else 0

  scrollbarState.contentLength = SCROLL_LINES.size
  scrollbarState.viewportContentLength = viewportLines
  scrollbarState.position = offset

  paragraph {
    text = SCROLL_LINES.drop(offset).take(viewportLines).joinToString("\n")
    this.area = inner
  }
  // Render scrollbar overlaid on the border (right edge of area)
  scrollbar(area, scrollbarState) { setOrientation(ScrollbarOrientation.VerticalRight) }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderBranding(area: Rect, tick: Int) {
  block(area) {
    title = "Branding"
    borders = Borders.all.bits
  }
  val inner = area.inner()
  val cols = Layout.horizontal(Constraint.Fill(1), Constraint.Fill(1)).split(inner)

  block(cols[0]) {
    title = "Logo"
    borders = Borders.all.bits
  }
  logo(cols[0].inner()) { setSize(LogoSize.Small) }

  val eyeColor = if (tick % 40 < 20) MascotEyeColor.Default else MascotEyeColor.Red
  block(cols[1]) {
    title = "Mascot"
    borders = Borders.all.bits
  }
  mascot(cols[1].inner()) { setEyeColor(eyeColor) }
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
