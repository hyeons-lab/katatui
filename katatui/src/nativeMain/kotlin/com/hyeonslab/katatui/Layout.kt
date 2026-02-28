@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import cnames.structs.KatatuiLayout
import com.hyeonslab.katatui.cinterop.KatatuiConstraint
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Fill
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Length
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Max
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Min
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Percentage
import com.hyeonslab.katatui.cinterop.KatatuiDirection
import com.hyeonslab.katatui.cinterop.KatatuiDirection_Horizontal
import com.hyeonslab.katatui.cinterop.KatatuiDirection_Vertical
import com.hyeonslab.katatui.cinterop.KatatuiRect
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
    val layout: CPointer<KatatuiLayout> =
      checkNotNull(katatui_layout_new(dir.toCDirection())) { "katatui_layout_new() returned null" }
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
        // +1 is required by the C API contract: katatui_layout_split expects
        // a buffer of at least constraints.len() + 1 elements (see layout.rs).
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

internal fun Direction.toCDirection(): KatatuiDirection =
  when (this) {
    Direction.Horizontal -> KatatuiDirection_Horizontal
    Direction.Vertical -> KatatuiDirection_Vertical
  }

internal fun Constraint.toCKind(): KatatuiConstraintKind =
  when (this) {
    is Constraint.Length -> KatatuiConstraintKind_Length
    is Constraint.Percentage -> KatatuiConstraintKind_Percentage
    is Constraint.Min -> KatatuiConstraintKind_Min
    is Constraint.Max -> KatatuiConstraintKind_Max
    is Constraint.Fill -> KatatuiConstraintKind_Fill
  }
