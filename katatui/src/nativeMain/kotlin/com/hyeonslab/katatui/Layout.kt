package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiConstraint
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind
import com.hyeonslab.katatui.cinterop.KatatuiDirection
import com.hyeonslab.katatui.cinterop.KatatuiRect
import com.hyeonslab.katatui.cinterop.katatui_layout_add_constraint
import com.hyeonslab.katatui.cinterop.katatui_layout_free
import com.hyeonslab.katatui.cinterop.katatui_layout_new
import com.hyeonslab.katatui.cinterop.katatui_layout_split
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value

object Layout {
    fun vertical(vararg constraints: Constraint): LayoutBuilder =
        LayoutBuilder(Direction.Vertical, constraints)

    fun horizontal(vararg constraints: Constraint): LayoutBuilder =
        LayoutBuilder(Direction.Horizontal, constraints)
}

@OptIn(ExperimentalForeignApi::class)
class LayoutBuilder internal constructor(
    private val dir: Direction,
    private val constraints: Array<out Constraint>,
) {
    fun split(area: Rect): kotlin.collections.List<Rect> {
        val cDir =
            when (dir) {
                Direction.Horizontal -> KatatuiDirection.HORIZONTAL
                Direction.Vertical -> KatatuiDirection.VERTICAL
            }
        val layout = checkNotNull(katatui_layout_new(cDir)) { "katatui_layout_new() returned null" }
        try {
            constraints.forEach { c ->
                val cConstraint = cValue<KatatuiConstraint> {
                    kind = c.toCKind()
                    value = c.value
                }
                katatui_layout_add_constraint(layout, cConstraint)
            }
            return memScoped {
                val outRects = allocArray<KatatuiRect>(constraints.size + 1)
                val outCount = alloc<kotlinx.cinterop.UIntVar>()
                outCount.value = 0u
                katatui_layout_split(layout, area.toCValue(), outRects, outCount.ptr)
                val count = outCount.value.toInt()
                (0 until count).map { i -> outRects[i].let { Rect(it.x, it.y, it.width, it.height) } }
            }
        } finally {
            katatui_layout_free(layout)
        }
    }
}

private fun Constraint.toCKind(): KatatuiConstraintKind =
    when (this) {
        is Constraint.Length -> KatatuiConstraintKind.LENGTH
        is Constraint.Percentage -> KatatuiConstraintKind.PERCENTAGE
        is Constraint.Min -> KatatuiConstraintKind.MIN
        is Constraint.Max -> KatatuiConstraintKind.MAX
        is Constraint.Fill -> KatatuiConstraintKind.FILL
    }
