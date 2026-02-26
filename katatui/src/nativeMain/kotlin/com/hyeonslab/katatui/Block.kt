package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiBlock
import com.hyeonslab.katatui.cinterop.katatui_block_free
import com.hyeonslab.katatui.cinterop.katatui_block_new
import com.hyeonslab.katatui.cinterop.katatui_block_set_borders
import com.hyeonslab.katatui.cinterop.katatui_block_set_title
import com.hyeonslab.katatui.widgets.Borders
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCString

@OptIn(ExperimentalForeignApi::class)
class Block internal constructor(internal val ptr: CPointer<KatatuiBlock>) : AutoCloseable {
    var title: String? = null
        set(value) {
            field = value
            memScoped { katatui_block_set_title(ptr, value?.toCString(this)) }
        }

    var borders: Borders = Borders.NONE
        set(value) {
            field = value
            katatui_block_set_borders(ptr, value.bits)
        }

    override fun close() {
        katatui_block_free(ptr)
    }

    companion object {
        operator fun invoke(init: Block.() -> Unit = {}): Block =
            Block(checkNotNull(katatui_block_new()) { "katatui_block_new() returned null" })
                .apply(init)
    }
}
