package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.KEY_BACKSPACE
import com.hyeonslab.katatui.KEY_DOWN
import com.hyeonslab.katatui.KEY_ENTER
import com.hyeonslab.katatui.KEY_ESC
import com.hyeonslab.katatui.KEY_LEFT
import com.hyeonslab.katatui.KEY_RIGHT
import com.hyeonslab.katatui.KEY_UP

val TAB_NAMES =
  listOf(
    "Dashboard",
    "Chart",
    "Canvas",
    "Scrollbar",
    "Branding",
    "Widgets",
    "Image",
    "Katatui Code",
  )

// Sine-shaped wave cycling through 20 values (0–100 range)
val WAVE =
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

val SCROLL_LINES =
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

// ---- MVI State ----

data class AppState(
  val running: Boolean = true,
  val activeTab: Int = 0,
  val tick: Int = 0,
  val history: List<ULong> = emptyList(),
  val scrollOffset: Int = 0,
  val codeState: KatatuiCodeState = KatatuiCodeState(),
) {
  val cpuPct: UByte
    get() = (tick * 3 % 101).toUByte()

  // Mem is offset by a half-period so the two gauges move in opposite directions
  val memPct: UByte
    get() = WAVE[(tick + WAVE.size / 2) % WAVE.size].toUByte()
}

// ---- MVI Intent ----

sealed interface AppIntent {
  object Quit : AppIntent

  object Tick : AppIntent

  data class SelectTab(val index: Int) : AppIntent

  object TabLeft : AppIntent

  object TabRight : AppIntent

  object ScrollUp : AppIntent

  object ScrollDown : AppIntent

  data class Code(val intent: KatatuiCodeIntent) : AppIntent

  data class KeyPress(val key: Char?) : AppIntent
}

// ---- Reducer ----

fun reduce(state: AppState, intent: AppIntent): AppState =
  when (intent) {
    AppIntent.Quit -> state.copy(running = false)
    AppIntent.Tick -> {
      val t = state.tick + 1
      state.copy(tick = t, history = (state.history + WAVE[t % WAVE.size]).takeLast(40))
    }
    is AppIntent.SelectTab -> state.copy(activeTab = intent.index.coerceIn(0, TAB_NAMES.lastIndex))
    AppIntent.TabLeft -> state.copy(activeTab = maxOf(0, state.activeTab - 1))
    AppIntent.TabRight -> state.copy(activeTab = minOf(TAB_NAMES.lastIndex, state.activeTab + 1))
    AppIntent.ScrollUp -> state.copy(scrollOffset = maxOf(0, state.scrollOffset - 1))
    AppIntent.ScrollDown ->
      state.copy(scrollOffset = (state.scrollOffset + 1).coerceAtMost(SCROLL_LINES.size - 1))
    is AppIntent.Code -> state.copy(codeState = reduce(state.codeState, intent.intent))
    is AppIntent.KeyPress -> reduceKey(state, intent.key)
  }

private fun reduceKey(state: AppState, key: Char?): AppState =
  when (key) {
    'q' -> reduce(state, AppIntent.Quit)
    '1' -> reduce(state, AppIntent.SelectTab(0))
    '2' -> reduce(state, AppIntent.SelectTab(1))
    '3' -> reduce(state, AppIntent.SelectTab(2))
    '4' -> reduce(state, AppIntent.SelectTab(3))
    '5' -> reduce(state, AppIntent.SelectTab(4))
    '6' -> reduce(state, AppIntent.SelectTab(5))
    '7' -> reduce(state, AppIntent.SelectTab(6))
    '8' -> reduce(state, AppIntent.SelectTab(7))
    KEY_LEFT ->
      when {
        state.activeTab == 7 && state.codeState.input.isNotEmpty() ->
          reduce(state, AppIntent.Code(KatatuiCodeIntent.CursorLeft))
        else -> reduce(state, AppIntent.TabLeft)
      }
    KEY_RIGHT ->
      when {
        state.activeTab == 7 && state.codeState.input.isNotEmpty() ->
          reduce(state, AppIntent.Code(KatatuiCodeIntent.CursorRight))
        else -> reduce(state, AppIntent.TabRight)
      }
    KEY_UP ->
      when {
        state.activeTab == 3 -> reduce(state, AppIntent.ScrollUp)
        state.activeTab == 7 && state.codeState.isCommandMode ->
          reduce(state, AppIntent.Code(KatatuiCodeIntent.PrevSuggestion))
        state.activeTab == 7 -> reduce(state, AppIntent.Code(KatatuiCodeIntent.ScrollUp))
        else -> state
      }
    KEY_DOWN ->
      when {
        state.activeTab == 3 -> reduce(state, AppIntent.ScrollDown)
        state.activeTab == 7 && state.codeState.isCommandMode ->
          reduce(state, AppIntent.Code(KatatuiCodeIntent.NextSuggestion))
        state.activeTab == 7 -> reduce(state, AppIntent.Code(KatatuiCodeIntent.ScrollDown))
        else -> state
      }
    KEY_BACKSPACE ->
      if (state.activeTab == 7) reduce(state, AppIntent.Code(KatatuiCodeIntent.Backspace))
      else state
    KEY_ENTER ->
      if (state.activeTab == 7) reduce(state, AppIntent.Code(KatatuiCodeIntent.Accept)) else state
    KEY_ESC ->
      if (state.activeTab == 7) reduce(state, AppIntent.Code(KatatuiCodeIntent.Cancel)) else state
    else ->
      if (key != null && key in ' '..'~' && state.activeTab == 7)
        reduce(state, AppIntent.Code(KatatuiCodeIntent.TypeChar(key)))
      else state
  }

// ---- Store ----

class AppStore {
  var state = AppState()
    private set

  fun dispatch(intent: AppIntent) {
    state = reduce(state, intent)
  }
}
