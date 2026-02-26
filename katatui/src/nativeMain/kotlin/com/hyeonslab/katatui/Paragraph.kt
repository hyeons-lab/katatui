package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiParagraph
import com.hyeonslab.katatui.cinterop.katatui_paragraph_free
import com.hyeonslab.katatui.cinterop.katatui_paragraph_new
import com.hyeonslab.katatui.cinterop.katatui_paragraph_set_wrap
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCString

@OptIn(ExperimentalForeignApi::class)
class Paragraph internal constructor(internal val ptr: CPointer<KatatuiParagraph>) : AutoCloseable {
    var wrap: Boolean = false
        set(value) {
            field = value
            katatui_paragraph_set_wrap(ptr, value)
        }

    override fun close() {
        katatui_paragraph_free(ptr)
    }

    companion object {
        operator fun invoke(text: String, init: Paragraph.() -> Unit = {}): Paragraph =
            memScoped {
                Paragraph(
                    checkNotNull(katatui_paragraph_new(text.toCString(this))) {
                        "katatui_paragraph_new() returned null"
                    }
                )
            }.apply(init)
    }
}
