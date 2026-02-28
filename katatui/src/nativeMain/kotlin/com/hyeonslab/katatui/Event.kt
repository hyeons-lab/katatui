@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_event_poll
import com.hyeonslab.katatui.cinterop.katatui_event_read_extended
import com.hyeonslab.katatui.cinterop.katatui_event_read_key_code
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Polls for a terminal event.
 *
 * @param timeout The maximum time to wait for an event.
 * @return `true` if an event is available, `false` otherwise.
 */
fun poll(timeout: Duration = 100.milliseconds): Boolean {
  return katatui_event_poll(timeout.inWholeMilliseconds.toULong())
}

/** Convenience overload for Swift callers that can't construct a [Duration] directly. */
fun pollMillis(timeoutMillis: Long = 100L): Boolean = poll(timeoutMillis.milliseconds)

/**
 * Reads the next key event from the terminal.
 *
 * Special keys: Up=`\u00F1`, Down=`\u00F2`, Left=`\u00F3`, Right=`\u00F4`, Enter=`\r`,
 * Esc=`\u001B`.
 *
 * @return The character representation of the key pressed, or `null` if no key event is available
 *   or the key is unknown.
 */
fun readKey(): Char? {
  val code = katatui_event_read_key_code().toInt()
  return if (code != 0) code.toChar() else null
}

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

  /** A key was pressed. [key] is the character, or `null` for unknown keys. */
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
fun readEvent(timeoutMs: Long = 100L): TerminalEvent =
  when (val code = katatui_event_read_extended(timeoutMs.toULong()).toInt()) {
    256 -> TerminalEvent.Tick
    0 -> TerminalEvent.Other
    else -> TerminalEvent.Key(code.toChar())
  }
