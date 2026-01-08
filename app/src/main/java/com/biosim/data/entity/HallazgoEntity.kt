package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para hallazgos de cultivo.
 * Registra fotos con ubicación GPS asociadas a un cultivo.
 */
@Entity(
    tableName = "hallazgos",
    foreignKeys = [
        ForeignKey(
            entity = CultivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cultivoId"])]
)
data class HallazgoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val cultivoId: Int,
    val fecha: Long,
    val fotoUri: String,            // URI de la foto guardada
    val latitud: Double,
    val longitud: Double,
    val descripcion: String? = null,
    val tipoHallazgo: String = "OBSERVACION",  // OBSERVACION, PROBLEMA, MEJORA
    val fechaCreacion: Long = System.currentTimeMillis()
)

/**
 * Tipo de hallazgo.
 */
enum class TipoHallazgo(val label: String, val emoji: String, val color: Long) {
    OBSERVACION("Observación", "👁️", 0xFF2196F3),
    PROBLEMA("Problema", "⚠️", 0xFFF44336),
    MEJORA("Mejora", "✨", 0xFF4CAF50),
    COSECHA("Cosecha", "🌾", 0xFFFF9800),
    CRECIMIENTO("Crecimiento", "📈", 0xFF9C27B0)
}

