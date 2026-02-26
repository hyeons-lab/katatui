package com.hyeonslab.katatui.codegen.model

data class WidgetGroup(
  val cName: String, // e.g. "KatatuiBlock"
  val kotlinName: String, // e.g. "Block"
  val functions: List<CFunction>,
) {
  val constructor: CFunction?
    get() = functions.firstOrNull { it.role == FunctionRole.Constructor }

  val destructor: CFunction?
    get() = functions.firstOrNull { it.role == FunctionRole.Destructor }

  val setters: List<CFunction>
    get() = functions.filter { it.role == FunctionRole.Setter }

  val adders: List<CFunction>
    get() = functions.filter { it.role == FunctionRole.Adder }
}
