package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para fotos de inspecciones de plagas con geolocalización.
 * Relación: 1 Inspección → N Fotos
 * 
 * Decisión: 
 * - Se usa CASCADE para eliminar automáticamente las fotos cuando se elimina la inspección.
 * - lat/lng son nullable para permitir fotos sin GPS (si el permiso falla o no hay señal).
 */
@Entity(
    tableName = "plaga_fotos",
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
data class PlagaFotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val inspeccionId: Int,
    val fotoUri: String,
    val fecha: Long = System.currentTimeMillis(),
    val descripcion: String? = null,
    
    // Geolocalización (nullable si GPS no disponible)
    val latitud: Double? = null,
    val longitud: Double? = null
) {
    /**
     * Indica si la foto tiene coordenadas válidas.
     */
    val tieneUbicacion: Boolean
        get() = latitud != null && longitud != null
}

