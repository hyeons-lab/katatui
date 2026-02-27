@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import cnames.structs.KatatuiLayout
import com.hyeonslab.katatui.cinterop.Fill
import com.hyeonslab.katatui.cinterop.Horizontal
import com.hyeonslab.katatui.cinterop.KatatuiConstraint
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind
import com.hyeonslab.katatui.cinterop.KatatuiDirection
import com.hyeonslab.katatui.cinterop.KatatuiRect
import com.hyeonslab.katatui.cinterop.Length
import com.hyeonslab.katatui.cinterop.Max
import com.hyeonslab.katatui.cinterop.Min
import com.hyeonslab.katatui.cinterop.Percentage
import com.hyeonslab.katatui.cinterop.Vertical
import com.hyeonslab.katatui.cinterop.katatui_layout_add_constraint
import com.hyeonslab.katatui.cinterop.katatui_layout_free
import com.hyeonslab.katatui.cinterop.katatui_layout_new
import com.hyeonslab.katatui.cinterop.katatui_layout_split
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValue
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped

object Layout {
  fun vertical(constraints: kotlin.collections.List<Constraint>): LayoutBuilder =
    LayoutBuilder(Direction.Vertical, constraints.toTypedArray())

  fun vertical(vararg constraints: Constraint): LayoutBuilder =
    LayoutBuilder(Direction.Vertical, constraints)

  fun horizontal(constraints: kotlin.collections.List<Constraint>): LayoutBuilder =
    LayoutBuilder(Direction.Horizontal, constraints.toTypedArray())

  fun horizontal(vararg constraints: Constraint): LayoutBuilder =
    LayoutBuilder(Direction.Horizontal, constraints)
}

class LayoutBuilder
internal constructor(private val dir: Direction, private val constraints: Array<out Constraint>) {
  fun split(area: Rect): kotlin.collections.List<Rect> {
    // C enum constants are package-level values in cinterop (not class members)
    val cDir: KatatuiDirection =
      when (dir) {
        Direction.Horizontal -> Horizontal
        Direction.Vertical -> Vertical
      }
    val layout: CPointer<KatatuiLayout> =
      checkNotNull(katatui_layout_new(cDir)) { "katatui_layout_new() returned null" }
    try {
      constraints.forEach { c ->
        val cConstraint =
          cValue<KatatuiConstraint> {
            kind = c.toCKind()
            value = c.value
          }
        katatui_layout_add_constraint(layout, cConstraint)
      }
      return memScoped {
        val maxRects = constraints.size + 1
        val outRects = allocArray<KatatuiRect>(maxRects)
        val count = katatui_layout_split(layout, area.toCValue(), outRects).toInt()
        buildList {
          for (i in 0 until count) {
            val r = outRects[i]
            add(Rect(r.x, r.y, r.width, r.height))
          }
        }
      }
    } finally {
      katatui_layout_free(layout)
    }
  }
}

internal fun Constraint.toCKind(): KatatuiConstraintKind =
  when (this) {
    is Constraint.Length -> Length
    is Constraint.Percentage -> Percentage
    is Constraint.Min -> Min
    is Constraint.Max -> Max
    is Constraint.Fill -> Fill
  }
