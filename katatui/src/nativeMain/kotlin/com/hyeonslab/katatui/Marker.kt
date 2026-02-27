@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiMarker
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Bar
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Block
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Braille
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Dot
import com.hyeonslab.katatui.cinterop.KatatuiMarker_HalfBlock
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Quadrant
import kotlinx.cinterop.ExperimentalForeignApi

enum class Marker {
  Dot,
  Block,
  Bar,
  Braille,
  HalfBlock,
  Quadrant,
}

internal fun Marker.toCMarker(): KatatuiMarker =
  when (this) {
    Marker.Dot -> KatatuiMarker_Dot
    Marker.Block -> KatatuiMarker_Block
    Marker.Bar -> KatatuiMarker_Bar
    Marker.Braille -> KatatuiMarker_Braille
    Marker.HalfBlock -> KatatuiMarker_HalfBlock
    Marker.Quadrant -> KatatuiMarker_Quadrant
  }
