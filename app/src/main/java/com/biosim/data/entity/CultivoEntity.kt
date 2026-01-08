package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla de cultivos.
 * Representa un cultivo en la base de datos local.
 */
@Entity(tableName = "cultivos")
data class CultivoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val nombre: String,
    val tipo: String,
    val emoji: String,
    val ubicacion: String,
    val fechaSiembra: String,
    val estado: String, // Se guarda como String y se convierte a EstadoCultivo
    val diasDesdeSiembra: Int,
    val proximoRiego: String,
    
    // Campos adicionales para el futuro
    val notas: String? = null,
    val fotoPath: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)

