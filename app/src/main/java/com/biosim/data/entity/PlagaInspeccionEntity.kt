package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para inspecciones de plagas.
 * Registra cada inspección realizada a un cultivo.
 */
@Entity(
    tableName = "plaga_inspecciones",
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
data class PlagaInspeccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val cultivoId: Int,
    val fecha: Long,
    val tipoPlaga: String,              // INSECTO, HONGO, BACTERIA, VIRUS, ACARO, NEMATODO
    val nombrePlaga: String,            // Nombre específico de la plaga
    val nivelIncidencia: String,        // BAJO, MEDIO, ALTO, CRITICO
    val parteAfectada: String,          // HOJAS, TALLO, RAIZ, FRUTO, FLOR
    val observaciones: String? = null,
    val fotoPath: String? = null,
    val resuelta: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)

/**
 * Tipos de plaga.
 */
enum class TipoPlaga(val label: String, val emoji: String) {
    INSECTO("Insecto", "🐛"),
    HONGO("Hongo", "🍄"),
    BACTERIA("Bacteria", "🦠"),
    VIRUS("Virus", "🔬"),
    ACARO("Ácaro", "🕷️"),
    NEMATODO("Nematodo", "🪱"),
    OTRO("Otro", "❓")
}

/**
 * Nivel de incidencia de la plaga.
 */
enum class NivelIncidencia(val label: String, val emoji: String, val color: Long) {
    BAJO("Bajo", "🟢", 0xFF4CAF50),
    MEDIO("Medio", "🟡", 0xFFFFC107),
    ALTO("Alto", "🟠", 0xFFFF9800),
    CRITICO("Crítico", "🔴", 0xFFF44336)
}

/**
 * Parte de la planta afectada.
 */
enum class ParteAfectada(val label: String, val emoji: String) {
    HOJAS("Hojas", "🍃"),
    TALLO("Tallo", "🌿"),
    RAIZ("Raíz", "🌱"),
    FRUTO("Fruto", "🍎"),
    FLOR("Flor", "🌸"),
    PLANTA_COMPLETA("Planta completa", "🌳")
}

