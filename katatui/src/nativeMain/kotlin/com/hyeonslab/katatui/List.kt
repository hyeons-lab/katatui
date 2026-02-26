package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiList
import com.hyeonslab.katatui.cinterop.KatatuiListState
import com.hyeonslab.katatui.cinterop.katatui_list_add_item
import com.hyeonslab.katatui.cinterop.katatui_list_free
import com.hyeonslab.katatui.cinterop.katatui_list_new
import com.hyeonslab.katatui.cinterop.katatui_list_state_free
import com.hyeonslab.katatui.cinterop.katatui_list_state_new
import com.hyeonslab.katatui.cinterop.katatui_list_state_select
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCString

@OptIn(ExperimentalForeignApi::class)
class List internal constructor(internal val ptr: CPointer<KatatuiList>) : AutoCloseable {
    fun addItem(item: String) {
        memScoped { katatui_list_add_item(ptr, item.toCString(this)) }
    }

    override fun close() {
        katatui_list_free(ptr)
    }

    companion object {
        operator fun invoke(vararg items: String): List {
            val list =
                List(checkNotNull(katatui_list_new()) { "katatui_list_new() returned null" })
            items.forEach { list.addItem(it) }
            return list
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
class ListState internal constructor(internal val ptr: CPointer<KatatuiListState>) : AutoCloseable {
    var selected: Int = -1
        set(value) {
            field = value
            katatui_list_state_select(ptr, value)
        }

    override fun close() {
        katatui_list_state_free(ptr)
    }

    companion object {
        operator fun invoke(): ListState =
            ListState(
                checkNotNull(katatui_list_state_new()) { "katatui_list_state_new() returned null" }
            )
    }
}
