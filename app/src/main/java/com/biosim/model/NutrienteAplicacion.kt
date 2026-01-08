package com.biosim.model

import com.biosim.data.entity.MetodoAplicacion
import com.biosim.data.entity.NutrienteAplicacionEntity
import com.biosim.data.entity.TipoNutriente
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de UI para una aplicación de nutriente.
 */
data class NutrienteAplicacion(
    val id: Int = 0,
    val cultivoId: Int,
    val cultivoNombre: String = "",
    val cultivoEmoji: String = "🌱",
    val fecha: Long,
    val tipoNutriente: TipoNutriente,
    val cantidadGramos: Int,
    val metodoAplicacion: MetodoAplicacion,
    val comentario: String? = null
) {
    /**
     * Fecha formateada para mostrar.
     */
    val fechaFormateada: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(fecha))
        }

    /**
     * Solo la fecha sin hora.
     */
    val fechaCorta: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(fecha))
        }

    /**
     * Cantidad formateada (g o kg según cantidad).
     */
    val cantidadFormateada: String
        get() = if (cantidadGramos >= 1000) {
            "%.1f kg".format(cantidadGramos / 1000f)
        } else {
            "$cantidadGramos g"
        }
}

/**
 * Estado de UI para la pantalla de nutrientes.
 */
sealed class NutrientesUiState {
    data object Loading : NutrientesUiState()
    data class Success(val aplicaciones: List<NutrienteAplicacion>) : NutrientesUiState()
    data class Error(val mensaje: String) : NutrientesUiState()
    data object Empty : NutrientesUiState()
}

/**
 * Estado de UI para el formulario de agregar nutriente.
 */
data class AgregarNutrienteUiState(
    val cultivoSeleccionadoId: Int? = null,
    val fecha: Long = System.currentTimeMillis(),
    val tipoSeleccionado: TipoNutriente = TipoNutriente.NPK,
    val cantidadGramos: String = "",
    val metodoSeleccionado: MetodoAplicacion = MetodoAplicacion.SUELO,
    val comentario: String = "",
    val isLoading: Boolean = false,
    val errorCultivo: String? = null,
    val errorCantidad: String? = null,
    val guardadoExitoso: Boolean = false
)

// ══════════════════════════════════════════════════════════════
// Extensiones para convertir entre Entity y Model
// ══════════════════════════════════════════════════════════════

/**
 * Convierte NutrienteAplicacionEntity (Room) a NutrienteAplicacion (UI Model).
 */
fun NutrienteAplicacionEntity.toNutrienteModel(
    cultivoNombre: String = "",
    cultivoEmoji: String = "🌱"
): NutrienteAplicacion {
    return NutrienteAplicacion(
        id = this.id,
        cultivoId = this.cultivoId,
        cultivoNombre = cultivoNombre,
        cultivoEmoji = cultivoEmoji,
        fecha = this.fecha,
        tipoNutriente = try {
            TipoNutriente.valueOf(this.tipoNutriente)
        } catch (e: IllegalArgumentException) {
            TipoNutriente.NPK
        },
        cantidadGramos = this.cantidadGramos,
        metodoAplicacion = try {
            MetodoAplicacion.valueOf(this.metodoAplicacion)
        } catch (e: IllegalArgumentException) {
            MetodoAplicacion.SUELO
        },
        comentario = this.comentario
    )
}

/**
 * Convierte NutrienteAplicacion (UI Model) a NutrienteAplicacionEntity (Room).
 */
fun NutrienteAplicacion.toNutrienteEntity(): NutrienteAplicacionEntity {
    return NutrienteAplicacionEntity(
        id = this.id,
        cultivoId = this.cultivoId,
        fecha = this.fecha,
        tipoNutriente = this.tipoNutriente.name,
        cantidadGramos = this.cantidadGramos,
        metodoAplicacion = this.metodoAplicacion.name,
        comentario = this.comentario
    )
}

