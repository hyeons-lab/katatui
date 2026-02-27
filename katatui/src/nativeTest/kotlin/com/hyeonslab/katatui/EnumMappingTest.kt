@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Fill
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Length
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Max
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Min
import com.hyeonslab.katatui.cinterop.KatatuiConstraintKind_Percentage
import com.hyeonslab.katatui.cinterop.KatatuiDirection_Horizontal
import com.hyeonslab.katatui.cinterop.KatatuiDirection_Vertical
import com.hyeonslab.katatui.cinterop.KatatuiGraphType_Bar
import com.hyeonslab.katatui.cinterop.KatatuiGraphType_Line
import com.hyeonslab.katatui.cinterop.KatatuiGraphType_Scatter
import com.hyeonslab.katatui.cinterop.KatatuiLogoSize_Small
import com.hyeonslab.katatui.cinterop.KatatuiLogoSize_Tiny
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Bar
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Block
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Braille
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Dot
import com.hyeonslab.katatui.cinterop.KatatuiMarker_HalfBlock
import com.hyeonslab.katatui.cinterop.KatatuiMarker_Quadrant
import com.hyeonslab.katatui.cinterop.KatatuiMascotEyeColor_Default
import com.hyeonslab.katatui.cinterop.KatatuiMascotEyeColor_Red
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_HorizontalBottom
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_HorizontalTop
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_VerticalLeft
import com.hyeonslab.katatui.cinterop.KatatuiScrollbarOrientation_VerticalRight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.ExperimentalForeignApi

class EnumMappingTest {

  // --- Direction ---

  @Test
  fun `Direction Horizontal maps to KatatuiDirection_Horizontal`() {
    assertEquals(KatatuiDirection_Horizontal, Direction.Horizontal.toCDirection())
  }

  @Test
  fun `Direction Vertical maps to KatatuiDirection_Vertical`() {
    assertEquals(KatatuiDirection_Vertical, Direction.Vertical.toCDirection())
  }

  // --- Constraint ---

  @Test
  fun `Constraint Length maps to KatatuiConstraintKind_Length`() {
    assertEquals(KatatuiConstraintKind_Length, Constraint.Length(0).toCKind())
  }

  @Test
  fun `Constraint Percentage maps to KatatuiConstraintKind_Percentage`() {
    assertEquals(KatatuiConstraintKind_Percentage, Constraint.Percentage(0).toCKind())
  }

  @Test
  fun `Constraint Min maps to KatatuiConstraintKind_Min`() {
    assertEquals(KatatuiConstraintKind_Min, Constraint.Min(0).toCKind())
  }

  @Test
  fun `Constraint Max maps to KatatuiConstraintKind_Max`() {
    assertEquals(KatatuiConstraintKind_Max, Constraint.Max(0).toCKind())
  }

  @Test
  fun `Constraint Fill maps to KatatuiConstraintKind_Fill`() {
    assertEquals(KatatuiConstraintKind_Fill, Constraint.Fill(0).toCKind())
  }

  // --- CanvasMarker ---

  @Test
  fun `CanvasMarker Dot maps to KatatuiMarker_Dot`() {
    assertEquals(KatatuiMarker_Dot, CanvasMarker.Dot.toCMarker())
  }

  @Test
  fun `CanvasMarker Block maps to KatatuiMarker_Block`() {
    assertEquals(KatatuiMarker_Block, CanvasMarker.Block.toCMarker())
  }

  @Test
  fun `CanvasMarker Bar maps to KatatuiMarker_Bar`() {
    assertEquals(KatatuiMarker_Bar, CanvasMarker.Bar.toCMarker())
  }

  @Test
  fun `CanvasMarker Braille maps to KatatuiMarker_Braille`() {
    assertEquals(KatatuiMarker_Braille, CanvasMarker.Braille.toCMarker())
  }

  @Test
  fun `CanvasMarker HalfBlock maps to KatatuiMarker_HalfBlock`() {
    assertEquals(KatatuiMarker_HalfBlock, CanvasMarker.HalfBlock.toCMarker())
  }

  @Test
  fun `CanvasMarker Quadrant maps to KatatuiMarker_Quadrant`() {
    assertEquals(KatatuiMarker_Quadrant, CanvasMarker.Quadrant.toCMarker())
  }

  // --- GraphType ---

  @Test
  fun `GraphType Scatter maps to KatatuiGraphType_Scatter`() {
    assertEquals(KatatuiGraphType_Scatter, GraphType.Scatter.toCGraphType())
  }

  @Test
  fun `GraphType Line maps to KatatuiGraphType_Line`() {
    assertEquals(KatatuiGraphType_Line, GraphType.Line.toCGraphType())
  }

  @Test
  fun `GraphType Bar maps to KatatuiGraphType_Bar`() {
    assertEquals(KatatuiGraphType_Bar, GraphType.Bar.toCGraphType())
  }

  // --- ChartMarker ---

  @Test
  fun `ChartMarker Dot maps to KatatuiMarker_Dot`() {
    assertEquals(KatatuiMarker_Dot, ChartMarker.Dot.toCMarker())
  }

  @Test
  fun `ChartMarker Block maps to KatatuiMarker_Block`() {
    assertEquals(KatatuiMarker_Block, ChartMarker.Block.toCMarker())
  }

  @Test
  fun `ChartMarker Bar maps to KatatuiMarker_Bar`() {
    assertEquals(KatatuiMarker_Bar, ChartMarker.Bar.toCMarker())
  }

  @Test
  fun `ChartMarker Braille maps to KatatuiMarker_Braille`() {
    assertEquals(KatatuiMarker_Braille, ChartMarker.Braille.toCMarker())
  }

  @Test
  fun `ChartMarker HalfBlock maps to KatatuiMarker_HalfBlock`() {
    assertEquals(KatatuiMarker_HalfBlock, ChartMarker.HalfBlock.toCMarker())
  }

  @Test
  fun `ChartMarker Quadrant maps to KatatuiMarker_Quadrant`() {
    assertEquals(KatatuiMarker_Quadrant, ChartMarker.Quadrant.toCMarker())
  }

  // --- LogoSize ---

  @Test
  fun `LogoSize Tiny maps to KatatuiLogoSize_Tiny`() {
    assertEquals(KatatuiLogoSize_Tiny, LogoSize.Tiny.toCLogoSize())
  }

  @Test
  fun `LogoSize Small maps to KatatuiLogoSize_Small`() {
    assertEquals(KatatuiLogoSize_Small, LogoSize.Small.toCLogoSize())
  }

  // --- MascotEyeColor ---

  @Test
  fun `MascotEyeColor Default maps to KatatuiMascotEyeColor_Default`() {
    assertEquals(KatatuiMascotEyeColor_Default, MascotEyeColor.Default.toCEyeColor())
  }

  @Test
  fun `MascotEyeColor Red maps to KatatuiMascotEyeColor_Red`() {
    assertEquals(KatatuiMascotEyeColor_Red, MascotEyeColor.Red.toCEyeColor())
  }

  // --- ScrollbarOrientation ---

  @Test
  fun `ScrollbarOrientation VerticalRight maps to KatatuiScrollbarOrientation_VerticalRight`() {
    assertEquals(
      KatatuiScrollbarOrientation_VerticalRight,
      ScrollbarOrientation.VerticalRight.toCOrientation(),
    )
  }

  @Test
  fun `ScrollbarOrientation VerticalLeft maps to KatatuiScrollbarOrientation_VerticalLeft`() {
    assertEquals(
      KatatuiScrollbarOrientation_VerticalLeft,
      ScrollbarOrientation.VerticalLeft.toCOrientation(),
    )
  }

  @Test
  fun `ScrollbarOrientation HorizontalBottom maps to KatatuiScrollbarOrientation_HorizontalBottom`() {
    assertEquals(
      KatatuiScrollbarOrientation_HorizontalBottom,
      ScrollbarOrientation.HorizontalBottom.toCOrientation(),
    )
  }

  @Test
  fun `ScrollbarOrientation HorizontalTop maps to KatatuiScrollbarOrientation_HorizontalTop`() {
    assertEquals(
      KatatuiScrollbarOrientation_HorizontalTop,
      ScrollbarOrientation.HorizontalTop.toCOrientation(),
    )
  }
}
