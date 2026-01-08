package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla de riegos.
 * 
 * ¿Por qué @Entity con ForeignKey?
 * - Garantiza integridad referencial: un riego siempre pertenece a un cultivo válido.
 * - CASCADE en onDelete: si se elimina un cultivo, sus riegos también se eliminan.
 * - Index en cultivoId: mejora el rendimiento de consultas filtradas por cultivo.
 */
@Entity(
    tableName = "riegos",
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
data class RiegoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val cultivoId: Int,
    val fecha: Long,                    // Timestamp del riego
    val cantidadMl: Int,                // Cantidad en mililitros
    val metodo: String,                 // GOTEO, ASPERSION, MANUAL, INUNDACION
    val notas: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)

/**
 * Métodos de riego disponibles.
 * 
 * ¿Por qué enum separado?
 * - Centraliza los valores válidos.
 * - Facilita mostrar opciones en la UI.
 * - Evita strings mágicos dispersos en el código.
 */
enum class MetodoRiego(val label: String, val emoji: String) {
    GOTEO("Goteo", "💧"),
    ASPERSION("Aspersión", "🌧️"),
    MANUAL("Manual", "🪣"),
    INUNDACION("Inundación", "🌊"),
    NEBULIZACION("Nebulización", "🌫️")
}

