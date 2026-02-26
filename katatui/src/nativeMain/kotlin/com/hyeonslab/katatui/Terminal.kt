package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.katatui_terminal_begin_draw
import com.hyeonslab.katatui.cinterop.katatui_terminal_end_draw
import com.hyeonslab.katatui.cinterop.katatui_terminal_free
import com.hyeonslab.katatui.cinterop.katatui_terminal_init
import com.hyeonslab.katatui.cinterop.katatui_terminal_new
import com.hyeonslab.katatui.cinterop.katatui_terminal_restore
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class Terminal : AutoCloseable {
    private val ptr = checkNotNull(katatui_terminal_new()) { "katatui_terminal_new() returned null" }

    fun init() {
        katatui_terminal_init(ptr)
    }

    fun restore() {
        katatui_terminal_restore(ptr)
    }

    fun draw(block: Frame.() -> Unit) {
        val framePtr =
            checkNotNull(katatui_terminal_begin_draw(ptr)) { "begin_draw failed — already drawing?" }
        Frame(framePtr).block()
        katatui_terminal_end_draw(ptr)
    }

    override fun close() {
        restore()
        katatui_terminal_free(ptr)
    }
}
