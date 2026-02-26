package com.hyeonslab.katatui.codegen.model

data class CField(val name: String, val type: String)

data class CVariant(val name: String, val value: Int)

data class CStruct(val name: String, val fields: List<CField>)

data class CEnum(val name: String, val variants: List<CVariant>)
