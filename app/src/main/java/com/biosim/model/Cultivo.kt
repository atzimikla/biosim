package com.biosim.model

/**
 * Modelo de datos para un Cultivo.
 * Por ahora es un data class simple. Luego se puede anotar con @Entity para Room.
 */
data class Cultivo(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val emoji: String,
    val ubicacion: String,
    val fechaSiembra: String,
    val estado: EstadoCultivo,
    val diasDesdeSimebra: Int,
    val proximoRiego: String
)

/**
 * Estados posibles de un cultivo.
 */
enum class EstadoCultivo(val label: String, val color: Long) {
    GERMINANDO("Germinando", 0xFF8D6E63),
    CRECIENDO("Creciendo", 0xFF43A047),
    FLORECIENDO("Floreciendo", 0xFFAB47BC),
    PRODUCIENDO("Produciendo", 0xFFFF7043),
    COSECHADO("Cosechado", 0xFF5C6BC0)
}

