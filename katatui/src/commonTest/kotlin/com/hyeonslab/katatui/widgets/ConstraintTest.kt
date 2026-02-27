package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.Constraint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ConstraintTest {
  @Test
  fun `Length factory converts Int to UShort`() {
    val c = Constraint.Length(42)
    assertIs<Constraint.Length>(c)
    assertEquals(42.toUShort(), c.value)
  }

  @Test
  fun `Percentage factory converts Int to UShort`() {
    val c = Constraint.Percentage(75)
    assertIs<Constraint.Percentage>(c)
    assertEquals(75.toUShort(), c.value)
  }

  @Test
  fun `Min factory converts Int to UShort`() {
    val c = Constraint.Min(10)
    assertIs<Constraint.Min>(c)
    assertEquals(10.toUShort(), c.value)
  }

  @Test
  fun `Max factory converts Int to UShort`() {
    val c = Constraint.Max(200)
    assertIs<Constraint.Max>(c)
    assertEquals(200.toUShort(), c.value)
  }

  @Test
  fun `Fill factory converts Int to UShort`() {
    val c = Constraint.Fill(1)
    assertIs<Constraint.Fill>(c)
    assertEquals(1.toUShort(), c.value)
  }

  @Test
  fun `value zero is preserved`() {
    assertEquals(0.toUShort(), Constraint.Length(0).value)
    assertEquals(0.toUShort(), Constraint.Fill(0).value)
  }
}
