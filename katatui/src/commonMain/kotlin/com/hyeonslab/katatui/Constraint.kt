package com.hyeonslab.katatui

sealed class Constraint(val value: UShort) {
    class Length(v: UShort) : Constraint(v)
    class Percentage(v: UShort) : Constraint(v)
    class Min(v: UShort) : Constraint(v)
    class Max(v: UShort) : Constraint(v)
    class Fill(v: UShort) : Constraint(v)

    companion object {
        fun Length(v: Int): Constraint = Length(v.toUShort())
        fun Percentage(v: Int): Constraint = Percentage(v.toUShort())
        fun Min(v: Int): Constraint = Min(v.toUShort())
        fun Max(v: Int): Constraint = Max(v.toUShort())
        fun Fill(v: Int): Constraint = Fill(v.toUShort())
    }
}
