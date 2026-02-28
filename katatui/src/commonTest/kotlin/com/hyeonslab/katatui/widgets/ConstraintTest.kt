package com.hyeonslab.katatui.widgets

import com.hyeonslab.katatui.Constraint
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class ConstraintTest {
  @Test
  fun `Length factory converts Int to UShort`() {
    val c = Constraint.Length(42)
    c.shouldBeInstanceOf<Constraint.Length>()
    c.value shouldBe 42.toUShort()
  }

  @Test
  fun `Percentage factory converts Int to UShort`() {
    val c = Constraint.Percentage(75)
    c.shouldBeInstanceOf<Constraint.Percentage>()
    c.value shouldBe 75.toUShort()
  }

  @Test
  fun `Min factory converts Int to UShort`() {
    val c = Constraint.Min(10)
    c.shouldBeInstanceOf<Constraint.Min>()
    c.value shouldBe 10.toUShort()
  }

  @Test
  fun `Max factory converts Int to UShort`() {
    val c = Constraint.Max(200)
    c.shouldBeInstanceOf<Constraint.Max>()
    c.value shouldBe 200.toUShort()
  }

  @Test
  fun `Fill factory converts Int to UShort`() {
    val c = Constraint.Fill(1)
    c.shouldBeInstanceOf<Constraint.Fill>()
    c.value shouldBe 1.toUShort()
  }

  @Test
  fun `value zero is preserved`() {
    Constraint.Length(0).value shouldBe 0.toUShort()
    Constraint.Fill(0).value shouldBe 0.toUShort()
  }
}
