package com.uteq.software.appsupabasesoftw2026.models
import kotlinx.serialization.Serializable
@Serializable
data class Materia(
    val id: Long,
    val nombre: String? = null,
    val nivel: Int? = null
)
