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
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.cinterop.ExperimentalForeignApi

class EnumMappingTest {

  // --- Direction ---

  @Test
  fun `Direction Horizontal maps to KatatuiDirection_Horizontal`() {
    Direction.Horizontal.toCDirection() shouldBe KatatuiDirection_Horizontal
  }

  @Test
  fun `Direction Vertical maps to KatatuiDirection_Vertical`() {
    Direction.Vertical.toCDirection() shouldBe KatatuiDirection_Vertical
  }

  // --- Constraint ---

  @Test
  fun `Constraint Length maps to KatatuiConstraintKind_Length`() {
    Constraint.Length(0).toCKind() shouldBe KatatuiConstraintKind_Length
  }

  @Test
  fun `Constraint Percentage maps to KatatuiConstraintKind_Percentage`() {
    Constraint.Percentage(0).toCKind() shouldBe KatatuiConstraintKind_Percentage
  }

  @Test
  fun `Constraint Min maps to KatatuiConstraintKind_Min`() {
    Constraint.Min(0).toCKind() shouldBe KatatuiConstraintKind_Min
  }

  @Test
  fun `Constraint Max maps to KatatuiConstraintKind_Max`() {
    Constraint.Max(0).toCKind() shouldBe KatatuiConstraintKind_Max
  }

  @Test
  fun `Constraint Fill maps to KatatuiConstraintKind_Fill`() {
    Constraint.Fill(0).toCKind() shouldBe KatatuiConstraintKind_Fill
  }

  // --- Marker ---

  @Test
  fun `Marker Dot maps to KatatuiMarker_Dot`() {
    Marker.Dot.toCMarker() shouldBe KatatuiMarker_Dot
  }

  @Test
  fun `Marker Block maps to KatatuiMarker_Block`() {
    Marker.Block.toCMarker() shouldBe KatatuiMarker_Block
  }

  @Test
  fun `Marker Bar maps to KatatuiMarker_Bar`() {
    Marker.Bar.toCMarker() shouldBe KatatuiMarker_Bar
  }

  @Test
  fun `Marker Braille maps to KatatuiMarker_Braille`() {
    Marker.Braille.toCMarker() shouldBe KatatuiMarker_Braille
  }

  @Test
  fun `Marker HalfBlock maps to KatatuiMarker_HalfBlock`() {
    Marker.HalfBlock.toCMarker() shouldBe KatatuiMarker_HalfBlock
  }

  @Test
  fun `Marker Quadrant maps to KatatuiMarker_Quadrant`() {
    Marker.Quadrant.toCMarker() shouldBe KatatuiMarker_Quadrant
  }

  // --- GraphType ---

  @Test
  fun `GraphType Scatter maps to KatatuiGraphType_Scatter`() {
    GraphType.Scatter.toCGraphType() shouldBe KatatuiGraphType_Scatter
  }

  @Test
  fun `GraphType Line maps to KatatuiGraphType_Line`() {
    GraphType.Line.toCGraphType() shouldBe KatatuiGraphType_Line
  }

  @Test
  fun `GraphType Bar maps to KatatuiGraphType_Bar`() {
    GraphType.Bar.toCGraphType() shouldBe KatatuiGraphType_Bar
  }

  // --- LogoSize ---

  @Test
  fun `LogoSize Tiny maps to KatatuiLogoSize_Tiny`() {
    LogoSize.Tiny.toCLogoSize() shouldBe KatatuiLogoSize_Tiny
  }

  @Test
  fun `LogoSize Small maps to KatatuiLogoSize_Small`() {
    LogoSize.Small.toCLogoSize() shouldBe KatatuiLogoSize_Small
  }

  // --- MascotEyeColor ---

  @Test
  fun `MascotEyeColor Default maps to KatatuiMascotEyeColor_Default`() {
    MascotEyeColor.Default.toCEyeColor() shouldBe KatatuiMascotEyeColor_Default
  }

  @Test
  fun `MascotEyeColor Red maps to KatatuiMascotEyeColor_Red`() {
    MascotEyeColor.Red.toCEyeColor() shouldBe KatatuiMascotEyeColor_Red
  }

  // --- ScrollbarOrientation ---

  @Test
  fun `ScrollbarOrientation VerticalRight maps to KatatuiScrollbarOrientation_VerticalRight`() {
    ScrollbarOrientation.VerticalRight.toCOrientation() shouldBe
      KatatuiScrollbarOrientation_VerticalRight
  }

  @Test
  fun `ScrollbarOrientation VerticalLeft maps to KatatuiScrollbarOrientation_VerticalLeft`() {
    ScrollbarOrientation.VerticalLeft.toCOrientation() shouldBe
      KatatuiScrollbarOrientation_VerticalLeft
  }

  @Test
  fun `ScrollbarOrientation HorizontalBottom maps to KatatuiScrollbarOrientation_HorizontalBottom`() {
    ScrollbarOrientation.HorizontalBottom.toCOrientation() shouldBe
      KatatuiScrollbarOrientation_HorizontalBottom
  }

  @Test
  fun `ScrollbarOrientation HorizontalTop maps to KatatuiScrollbarOrientation_HorizontalTop`() {
    ScrollbarOrientation.HorizontalTop.toCOrientation() shouldBe
      KatatuiScrollbarOrientation_HorizontalTop
  }
}
