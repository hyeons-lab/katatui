package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.Constraint
import com.hyeonslab.katatui.Frame
import com.hyeonslab.katatui.GraphType
import com.hyeonslab.katatui.ImageState
import com.hyeonslab.katatui.Layout
import com.hyeonslab.katatui.LogoSize
import com.hyeonslab.katatui.Marker
import com.hyeonslab.katatui.MascotEyeColor
import com.hyeonslab.katatui.Picker
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel

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

@OptIn(ExperimentalForeignApi::class, ExperimentalCoroutinesApi::class)
fun main() {
  terminal {
    val scrollbarState = ScrollbarState()
    val appStore = AppStore()
    val env = readEnv()
    val scope = CoroutineScope(Dispatchers.Default)
    // Picker is created on the main thread so Picker::from_query_stdio() can query the
    // terminal for the best image protocol (Kitty, Sixel, halfblocks, etc.).
    // The actual image decode is offloaded to a background thread via the coroutine.
    val picker = Picker()
    var imageFuture: Deferred<ImageState?>? = null
    var imageState: ImageState? = null
    var previousTab = -1

    while (appStore.state.running) {
      appStore.dispatch(AppIntent.Tick)
      val state = appStore.state

      // Free image resources when leaving the Image tab so memory is not held indefinitely.
      // They are recreated on the next visit.
      if (previousTab == 6 && state.activeTab != 6) {
        imageFuture?.cancel()
        imageFuture = null
        imageState?.close()
        imageState = null
      }
      previousTab = state.activeTab

      draw {
        block(size) { setStyle(Style(bg = Color.Rgb(41u, 44u, 51u))) }
        val areas = Layout.vertical(Constraint.Length(3), Constraint.Fill(1)).split(size)
        val tabRow = areas[0]
        val content = areas[1]

        // Tab bar
        tabs(tabRow) {
          TAB_NAMES.forEach { addTitle(it) }
          selected = state.activeTab.toUInt()
        }

        // Clear the content area on every frame to prevent cross-tab artifacts
        clear(content)

        when (state.activeTab) {
          0 -> renderDashboard(content, state.history, state.cpuPct, state.memPct)
          1 -> renderChart(content, state.tick)
          2 -> renderCanvas(content, state.tick)
          3 -> renderScrollbar(content, scrollbarState, state.scrollOffset)
          4 -> renderBranding(content, state.tick)
          5 -> renderWidgetTable(content)
          6 -> {
            val future =
              imageFuture
                ?: scope
                  .async { ImageState.fromBytesWithPicker(LEAP_DARK_PNG, picker) }
                  .also { imageFuture = it }
            if (imageState == null && future.isCompleted) imageState = future.getCompleted()
            renderImageTab(content, imageState, loading = !future.isCompleted)
          }
          7 -> renderKatatuiCodeTab(content, state.codeState, env)
        }
      }

      if (poll()) appStore.dispatch(AppIntent.KeyPress(readKey()))
    }

    scope.cancel()
    imageState?.close()
    picker.close()
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
    text = "◄ ►  or  1–8 : switch tabs     ↑/↓ : scroll (Scrollbar tab)     q : quit"
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
    circle(50.0, 50.0, r, Color.Cyan)
    // 4 rotating spokes
    for (k in 0..3) {
      val angle = tick * 0.05 + k * PI / 2.0
      line(50.0, 50.0, 50.0 + 40.0 * cos(angle), 50.0 + 40.0 * sin(angle), Color.Green)
    }
    // Corner rectangles
    listOf(2.0 to 2.0, 83.0 to 2.0, 2.0 to 88.0, 83.0 to 88.0).forEach { (x, y) ->
      rectangle(x, y, 15.0, 10.0, Color.Yellow)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun Frame.renderScrollbar(area: Rect, scrollbarState: ScrollbarState, offset: Int) {
  block(area) {
    title = "Scrollbar"
    borders = Borders.all.bits
  }
  val inner = area.inner()
  val viewportLines = inner.height.toInt()

  scrollbarState.contentLength = SCROLL_LINES.size
  scrollbarState.viewportContentLength = viewportLines
  scrollbarState.position = offset

  paragraph {
    text = SCROLL_LINES.drop(offset).take(viewportLines).joinToString("\n")
    this.area = inner
  }
  // Render scrollbar overlaid on the border (right edge of area)
  scrollbar(scrollbarState, area) { setOrientation(ScrollbarOrientation.VerticalRight) }
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
private fun Frame.renderImageTab(area: Rect, imageState: ImageState?, loading: Boolean = false) {
  block(area) {
    title = "Image"
    borders = Borders.all.bits
  }
  when {
    imageState != null -> image(imageState, area.inner())
    loading ->
      paragraph {
        text = "Loading image…"
        this.area = area.inner()
      }
    else ->
      paragraph {
        text =
          "Image could not be decoded.\n\nAdd the file to sample-app/ and register it\nin generateResources inside build.gradle.kts."
        this.area = area.inner()
      }
  }
}
