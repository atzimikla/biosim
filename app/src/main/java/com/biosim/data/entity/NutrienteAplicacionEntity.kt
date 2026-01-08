package com.biosim.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para la tabla de aplicaciones de nutrientes.
 * Registra cada vez que se aplica fertilizante o nutriente a un cultivo.
 */
@Entity(
    tableName = "nutrientes_aplicaciones",
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
data class NutrienteAplicacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val cultivoId: Int,
    val fecha: Long,
    val tipoNutriente: String,          // NPK, CALCIO, MAGNESIO, etc.
    val cantidadGramos: Int,            // Cantidad en gramos
    val metodoAplicacion: String,       // SUELO, FERTIRRIEGO, FOLIAR
    val comentario: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)

/**
 * Tipos de nutrientes disponibles.
 */
enum class TipoNutriente(val label: String, val emoji: String, val descripcion: String) {
    NPK("NPK (Completo)", "🌿", "Nitrógeno, Fósforo, Potasio"),
    NITROGENO("Nitrógeno (N)", "💚", "Crecimiento vegetativo"),
    FOSFORO("Fósforo (P)", "🌸", "Raíces y floración"),
    POTASIO("Potasio (K)", "🍎", "Frutos y resistencia"),
    CALCIO("Calcio (Ca)", "🦴", "Estructura celular"),
    MAGNESIO("Magnesio (Mg)", "🥬", "Clorofila"),
    HIERRO("Hierro (Fe)", "🔩", "Síntesis de clorofila"),
    ZINC("Zinc (Zn)", "⚡", "Enzimas y hormonas"),
    ORGANICO("Orgánico", "🌱", "Compost, humus, etc.")
}

/**
 * Métodos de aplicación de nutrientes.
 */
enum class MetodoAplicacion(val label: String, val emoji: String) {
    SUELO("Al suelo", "🪴"),
    FERTIRRIEGO("Fertirriego", "💧"),
    FOLIAR("Foliar", "🍃")
}

