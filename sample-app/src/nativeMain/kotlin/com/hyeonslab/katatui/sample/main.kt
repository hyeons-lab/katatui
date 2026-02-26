package com.hyeonslab.katatui.sample

import com.hyeonslab.katatui.Block
import com.hyeonslab.katatui.Constraint
import com.hyeonslab.katatui.Layout
import com.hyeonslab.katatui.Paragraph
import com.hyeonslab.katatui.Terminal
import com.hyeonslab.katatui.cinterop.katatui_event_poll
import com.hyeonslab.katatui.cinterop.katatui_event_read_key_code
import com.hyeonslab.katatui.widgets.Borders
import kotlinx.cinterop.ExperimentalForeignApi

private const val KEY_Q = 'q'.code.toByte()
private const val KEY_UP: Byte = -15 // 0xF1 as signed byte
private const val KEY_DOWN: Byte = -14 // 0xF2 as signed byte

@OptIn(ExperimentalForeignApi::class)
fun main() {
    Terminal().use { terminal ->
        terminal.init()
        var count = 0
        while (true) {
            terminal.draw { frame ->
                val areas =
                    Layout.vertical(
                        Constraint.Length(3),
                        Constraint.Fill(1),
                    ).split(frame.size)
                val header = areas[0]
                val body = areas[1]

                Block { title = "Katatui Demo"; borders = Borders.ALL }.use { block ->
                    render(block, header)
                }
                Paragraph("Count: $count\n\nPress ↑/↓ to change, q to quit").use { para ->
                    render(para, body)
                }
            }

            if (katatui_event_poll(100u)) {
                when (katatui_event_read_key_code().toByte()) {
                    KEY_Q -> break
                    KEY_UP -> count++
                    KEY_DOWN -> count--
                }
            }
        }
    }
}
