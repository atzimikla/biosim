package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para tratamientos de plagas.
 * Registra cada tratamiento aplicado a una inspección de plaga.
 */
@Entity(
    tableName = "plaga_tratamientos",
    foreignKeys = [
        ForeignKey(
            entity = PlagaInspeccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspeccionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["inspeccionId"])]
)
data class PlagaTratamientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val inspeccionId: Int,
    val producto: String,               // Nombre del producto aplicado
    val tipoProducto: String,           // QUIMICO, BIOLOGICO, ORGANICO
    val dosisMl: Int,                   // Dosis en mililitros
    val metodoAplicacion: String,       // FOLIAR, SUELO, DRENCH
    val fechaAplicacion: Long,
    val observaciones: String? = null,
    val efectividad: String? = null,    // PENDIENTE, EFECTIVO, PARCIAL, INEFECTIVO
    val fechaCreacion: Long = System.currentTimeMillis()
)

/**
 * Tipo de producto para tratamiento.
 */
enum class TipoProducto(val label: String, val emoji: String) {
    QUIMICO("Químico", "🧪"),
    BIOLOGICO("Biológico", "🦠"),
    ORGANICO("Orgánico", "🌿")
}

/**
 * Método de aplicación del tratamiento.
 */
enum class MetodoAplicacionTratamiento(val label: String, val emoji: String) {
    FOLIAR("Foliar", "🍃"),
    SUELO("Al suelo", "🪴"),
    DRENCH("Drench", "💧"),
    FUMIGACION("Fumigación", "💨")
}

/**
 * Efectividad del tratamiento.
 */
enum class Efectividad(val label: String, val emoji: String, val color: Long) {
    PENDIENTE("Pendiente", "⏳", 0xFF9E9E9E),
    EFECTIVO("Efectivo", "✅", 0xFF4CAF50),
    PARCIAL("Parcial", "⚠️", 0xFFFFC107),
    INEFECTIVO("Inefectivo", "❌", 0xFFF44336)
}

