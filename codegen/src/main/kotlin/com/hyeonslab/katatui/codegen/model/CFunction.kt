package com.hyeonslab.katatui.codegen.model

data class CParam(val name: String, val type: String)

data class CFunction(
    val returnType: String,
    val name: String,
    val params: List<CParam>,
) {
    val group: String
        get() = name.removePrefix("katatui_").substringBefore("_")

    val role: FunctionRole
        get() =
            when {
                name.endsWith("_new") -> FunctionRole.Constructor
                name.endsWith("_free") -> FunctionRole.Destructor
                name.contains("_set_") -> FunctionRole.Setter
                name.contains("_add_") -> FunctionRole.Adder
                name.contains("_render_") -> FunctionRole.Renderer
                name.endsWith("_split") -> FunctionRole.Split
                else -> FunctionRole.Other
            }

    val setterProperty: String?
        get() =
            if (role == FunctionRole.Setter) {
                name.substringAfter("_set_")
            } else {
                null
            }
}

enum class FunctionRole {
    Constructor,
    Destructor,
    Setter,
    Adder,
    Renderer,
    Split,
    Other,
}
