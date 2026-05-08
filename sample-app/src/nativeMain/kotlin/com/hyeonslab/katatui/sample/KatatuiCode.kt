@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui.sample

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.getenv

// ---- Domain model ----

enum class Sender {
  USER,
  ASSISTANT,
}

data class ChatMessage(val sender: Sender, val text: String)

val ALL_COMMANDS = listOf("/commit", "/test", "/review", "/help", "/plan", "/format", "/clear")

val INITIAL_MESSAGES =
  listOf(
    ChatMessage(Sender.USER, "what is ratatui?"),
    ChatMessage(
      Sender.ASSISTANT,
      "Ratatui is a Rust library for building terminal user interfaces (TUIs)." +
        " It provides composable widgets, layouts, and styling primitives.",
    ),
    ChatMessage(Sender.USER, "how does katatui wrap it?"),
    ChatMessage(
      Sender.ASSISTANT,
      "Katatui exposes ratatui via a C FFI layer (cbindgen) and wraps it in Kotlin" +
        " Multiplatform Native using cinterop. KotlinPoet codegen generates widget" +
        " wrappers from the C header.",
    ),
  )

// ---- MVI State ----

data class KatatuiCodeState(
  val messages: List<ChatMessage> = INITIAL_MESSAGES,
  val input: String = "",
  val cursorPos: Int = 0,
  val scrollOffset: Int = 0,
  val selectedSuggestion: Int = 0,
  val model: String = "claude-sonnet-4-6",
) {
  val suggestions: List<String>
    get() = if (input.startsWith("/")) ALL_COMMANDS.filter { it.startsWith(input) } else emptyList()

  val isCommandMode: Boolean
    get() = input.startsWith("/")
}

// ---- MVI Intent ----

sealed interface KatatuiCodeIntent {
  data class TypeChar(val ch: Char) : KatatuiCodeIntent

  object Backspace : KatatuiCodeIntent

  object CursorLeft : KatatuiCodeIntent

  object CursorRight : KatatuiCodeIntent

  object ScrollUp : KatatuiCodeIntent

  object ScrollDown : KatatuiCodeIntent

  object NextSuggestion : KatatuiCodeIntent

  object PrevSuggestion : KatatuiCodeIntent

  object Accept : KatatuiCodeIntent

  object Cancel : KatatuiCodeIntent
}

// ---- Reducer ----

fun reduce(state: KatatuiCodeState, intent: KatatuiCodeIntent): KatatuiCodeState =
  when (intent) {
    is KatatuiCodeIntent.TypeChar -> {
      val newInput =
        state.input.substring(0, state.cursorPos) +
          intent.ch +
          state.input.substring(state.cursorPos)
      state.copy(input = newInput, cursorPos = state.cursorPos + 1, selectedSuggestion = 0)
    }
    KatatuiCodeIntent.Backspace -> {
      if (state.cursorPos == 0) state
      else {
        val newInput =
          state.input.substring(0, state.cursorPos - 1) + state.input.substring(state.cursorPos)
        state.copy(input = newInput, cursorPos = state.cursorPos - 1)
      }
    }
    KatatuiCodeIntent.CursorLeft -> state.copy(cursorPos = maxOf(0, state.cursorPos - 1))
    KatatuiCodeIntent.CursorRight ->
      state.copy(cursorPos = minOf(state.input.length, state.cursorPos + 1))
    // scrollOffset is distance from the bottom: 0 = pinned to latest, N = scrolled up N rows.
    KatatuiCodeIntent.ScrollUp -> state.copy(scrollOffset = state.scrollOffset + 1)
    KatatuiCodeIntent.ScrollDown -> state.copy(scrollOffset = maxOf(0, state.scrollOffset - 1))
    KatatuiCodeIntent.NextSuggestion -> {
      val size = state.suggestions.size
      if (size == 0) state
      else state.copy(selectedSuggestion = (state.selectedSuggestion + 1) % size)
    }
    KatatuiCodeIntent.PrevSuggestion -> {
      val size = state.suggestions.size
      if (size == 0) state
      else state.copy(selectedSuggestion = (state.selectedSuggestion - 1 + size) % size)
    }
    KatatuiCodeIntent.Accept -> {
      if (state.isCommandMode && state.suggestions.isNotEmpty()) {
        val chosen = state.suggestions[state.selectedSuggestion]
        state.copy(input = chosen, cursorPos = chosen.length, selectedSuggestion = 0)
      } else if (state.input.isNotEmpty()) {
        val canned = "(demo) Received ${state.input.length} chars. Try typing /commit or /help."
        val newMessages =
          state.messages +
            listOf(ChatMessage(Sender.USER, state.input), ChatMessage(Sender.ASSISTANT, canned))
        state.copy(
          messages = newMessages,
          input = "",
          cursorPos = 0,
          selectedSuggestion = 0,
          scrollOffset = 0,
        )
      } else state
    }
    KatatuiCodeIntent.Cancel -> state.copy(input = "", cursorPos = 0, selectedSuggestion = 0)
  }

// ---- Environment ----

data class KatatuiCodeEnv(val cwd: String, val branch: String)

fun readEnv(): KatatuiCodeEnv {
  val home = getenv("HOME")?.toKString() ?: ""
  val raw = getenv("PWD")?.toKString() ?: "."
  val cwd = if (home.isNotEmpty() && raw.startsWith(home)) "~${raw.removePrefix(home)}" else raw
  return KatatuiCodeEnv(cwd, readGitBranch() ?: "")
}

private fun readGitBranch(): String? {
  val file = fopen(".git/HEAD", "r") ?: return null
  return try {
    memScoped {
      val buf = allocArray<ByteVar>(256)
      if (fgets(buf, 256, file) == null) return@memScoped null
      val line = buf.toKString().trim()
      if (line.startsWith("ref: refs/heads/")) line.removePrefix("ref: refs/heads/") else null
    }
  } finally {
    fclose(file)
  }
}
