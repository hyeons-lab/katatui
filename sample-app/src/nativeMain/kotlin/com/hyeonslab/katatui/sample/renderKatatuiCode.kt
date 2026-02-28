@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.Constraint
import com.hyeonslab.katatui.Frame
import com.hyeonslab.katatui.Layout
import com.hyeonslab.katatui.Rect
import com.hyeonslab.katatui.ScrollbarOrientation
import com.hyeonslab.katatui.ScrollbarState
import com.hyeonslab.katatui.block
import com.hyeonslab.katatui.inner
import com.hyeonslab.katatui.paragraph
import com.hyeonslab.katatui.scrollbar
import com.hyeonslab.katatui.setOrientation
import com.hyeonslab.katatui.setStyle
import com.hyeonslab.katatui.widgets.Borders
import com.hyeonslab.katatui.widgets.Color
import com.hyeonslab.katatui.widgets.Style
import kotlinx.cinterop.ExperimentalForeignApi

// KATATUI_LOGO_SMALL text lines (katatui-ffi/src/widgets/logo.rs), plus a blank separator.
private val LOGO_LINES = listOf("█▌▞▝ ▄▀▀▄▝▜▛▘▄▀▀▄▝▜▛▘█  █ █", "█▌▚▗ █▀▀█ ▐▌ █▀▀█ ▐▌ ▀▄▄▀ █", "")

internal fun Frame.renderKatatuiCodeTab(area: Rect, state: KatatuiCodeState, env: KatatuiCodeEnv) {
  val suggHeight =
    if (state.isCommandMode && state.suggestions.isNotEmpty()) state.suggestions.size else 1
  val sections =
    Layout.vertical(
        Constraint.Fill(1), // messages block
        Constraint.Length(3), // input box
        Constraint.Length(suggHeight), // suggestions (below input, variable height)
        Constraint.Length(1), // status bar
      )
      .split(area)

  renderMessages(sections[0], state)
  renderInput(sections[1], state)
  renderSuggestions(sections[2], state)
  renderStatusBar(sections[3], env)
}

private fun Frame.renderMessages(area: Rect, state: KatatuiCodeState) {
  block(area) {
    title = "Katatui Code"
    borders = Borders.all.bits
    setStyle(Style(fg = Color.DarkGray))
  }
  val inner = area.inner()
  val viewportHeight = inner.height.toInt()
  val viewportWidth = inner.width.toInt()
  val textLines = flattenMessages(state.messages)

  val allLines = LOGO_LINES + textLines

  // Count how many terminal rows each logical line occupies after word-wrap.
  // Uses character-count ceiling as an approximation (accurate for typical messages).
  fun visualRows(line: String): Int =
    if (line.isEmpty() || viewportWidth <= 0) 1 else ((line.length - 1) / viewportWidth) + 1

  val totalLines = allLines.sumOf { visualRows(it) }
  // scrollOffset is distance from the bottom; convert to a top-anchored offset for rendering.
  val maxOffset = maxOf(0, totalLines - viewportHeight)
  val clampedOffset = maxOf(0, maxOffset - state.scrollOffset)

  // Skip lines that have scrolled above the viewport.
  var visualSoFar = 0
  var lineIndex = 0
  while (lineIndex < allLines.size && visualSoFar < clampedOffset) {
    visualSoFar += visualRows(allLines[lineIndex])
    lineIndex++
  }
  paragraph {
    text = allLines.drop(lineIndex).joinToString("\n")
    wrap = true
    this.area = inner
  }

  ScrollbarState().use { sbState ->
    sbState.contentLength = totalLines
    sbState.position = clampedOffset
    sbState.viewportContentLength = viewportHeight
    scrollbar(sbState, area) { setOrientation(ScrollbarOrientation.VerticalRight) }
  }
}

private fun Frame.renderSuggestions(area: Rect, state: KatatuiCodeState) {
  paragraph {
    text = formattedSuggestions(state)
    this.area = area
  }
}

private fun Frame.renderInput(area: Rect, state: KatatuiCodeState) {
  block(area) {
    title = " >"
    borders = Borders.all.bits
  }
  paragraph {
    text = inputWithCursor(state.input, state.cursorPos)
    this.area = area.inner()
  }
}

private fun Frame.renderStatusBar(area: Rect, env: KatatuiCodeEnv) {
  paragraph {
    text = "  ${env.cwd}   (${env.branch})   ·   ${env.model}"
    this.area = area
  }
}

// ---- Helpers ----

private fun flattenMessages(messages: List<ChatMessage>): List<String> {
  val lines = mutableListOf<String>()
  for (msg in messages) {
    when (msg.sender) {
      Sender.USER -> lines.add("  \u276F  ${msg.text}")
      Sender.ASSISTANT -> {
        lines.add("  \u25C6  assistant")
        lines.add("     ${msg.text}")
      }
    }
    lines.add("")
  }
  return lines
}

private fun formattedSuggestions(state: KatatuiCodeState): String =
  if (state.suggestions.isEmpty()) {
    "  \u2191\u2193 scroll   \u2190\u2192 cursor   / commands   ESC clear"
  } else {
    state.suggestions
      .mapIndexed { i, cmd -> if (i == state.selectedSuggestion) "  \u25B6 $cmd" else "    $cmd" }
      .joinToString("\n")
  }

private fun inputWithCursor(input: String, cursorPos: Int): String =
  input.substring(0, cursorPos) + "\u2502" + input.substring(cursorPos)
