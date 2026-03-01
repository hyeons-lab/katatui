@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_event_read_extended
import kotlinx.cinterop.ExperimentalForeignApi

/** Key code for the Up arrow key. */
const val KEY_UP: Char = '\u00F1'

/** Key code for the Down arrow key. */
const val KEY_DOWN: Char = '\u00F2'

/** Key code for the Left arrow key. */
const val KEY_LEFT: Char = '\u00F3'

/** Key code for the Right arrow key. */
const val KEY_RIGHT: Char = '\u00F4'

/** Key code for the Backspace key. */
const val KEY_BACKSPACE: Char = '\b'

/** Key code for the Tab key. */
const val KEY_TAB: Char = '\t'

/** Key code for the Enter key. */
const val KEY_ENTER: Char = '\r'

/** Key code for the Escape key. */
const val KEY_ESC: Char = '\u001B'

/** Terminal event returned by [readEvent]. */
sealed interface TerminalEvent {
  /** Synthetic tick: no key was pressed within the poll interval. */
  object Tick : TerminalEvent

  /**
   * A key was pressed. [key] is the character code; always non-null when produced by [readEvent].
   */
  data class Key(val key: Char?) : TerminalEvent

  /** A non-key event (real resize, mouse, paste, etc.) — safe to ignore. */
  object Other : TerminalEvent
}

/**
 * Blocking event read with tick timeout.
 *
 * Blocks until either a key is pressed or [timeoutMs] milliseconds elapse. Returns:
 * - [TerminalEvent.Tick] when the timeout elapses (use as an animation tick signal)
 * - [TerminalEvent.Key] on a key press
 * - [TerminalEvent.Other] for non-key events (resize, mouse, paste, etc.)
 */
fun readEvent(timeoutMs: Long = 100L): TerminalEvent {
  require(timeoutMs >= 0L) { "timeoutMs must be non-negative, got $timeoutMs" }
  return when (val code = katatui_event_read_extended(timeoutMs.toULong()).toInt()) {
    256 -> TerminalEvent.Tick
    0 -> TerminalEvent.Other
    else -> TerminalEvent.Key(code.toChar())
  }
}
