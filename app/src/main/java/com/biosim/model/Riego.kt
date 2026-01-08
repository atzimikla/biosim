package com.biosim.model

import com.biosim.data.entity.MetodoRiego
import com.biosim.data.entity.RiegoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de UI para Riego.
 * 
 * ¿Por qué separar Entity (Room) de Model (UI)?
 * - Desacoplamiento: la UI no depende directamente de Room.
 * - Flexibilidad: puedes agregar campos calculados (fechaFormateada, etc.).
 * - Testing: más fácil de mockear en tests de UI.
 * - Evolución: puedes cambiar la estructura de BD sin afectar la UI.
 */
data class Riego(
    val id: Int = 0,
    val cultivoId: Int,
    val cultivoNombre: String = "",     // Para mostrar en la UI
    val cultivoEmoji: String = "🌱",    // Para mostrar en la UI
    val fecha: Long,
    val cantidadMl: Int,
    val metodo: MetodoRiego,
    val notas: String? = null
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
     * Cantidad formateada (ml o L según cantidad).
     */
    val cantidadFormateada: String
        get() = if (cantidadMl >= 1000) {
            "%.1f L".format(cantidadMl / 1000f)
        } else {
            "$cantidadMl ml"
        }
}

/**
 * Estado de UI para la pantalla de riegos.
 * 
 * ¿Por qué sealed class para UiState?
 * - Representa todos los estados posibles de la pantalla.
 * - El compilador verifica que manejes todos los casos en when().
 * - Claridad: cada estado tiene sus propios datos.
 */
sealed class RiegosUiState {
    data object Loading : RiegosUiState()
    data class Success(val riegos: List<Riego>) : RiegosUiState()
    data class Error(val mensaje: String) : RiegosUiState()
    data object Empty : RiegosUiState()
}

/**
 * Estado de UI para el formulario de agregar riego.
 */
data class AgregarRiegoUiState(
    val cultivoSeleccionadoId: Int? = null,
    val fecha: Long = System.currentTimeMillis(),
    val cantidadMl: String = "",
    val metodoSeleccionado: MetodoRiego = MetodoRiego.MANUAL,
    val notas: String = "",
    val isLoading: Boolean = false,
    val errorCultivo: String? = null,
    val errorCantidad: String? = null,
    val guardadoExitoso: Boolean = false
)

// ══════════════════════════════════════════════════════════════
// Extensiones para convertir entre Entity y Model
// ══════════════════════════════════════════════════════════════

/**
 * Convierte RiegoEntity (Room) a Riego (UI Model).
 */
fun RiegoEntity.toModel(cultivoNombre: String = "", cultivoEmoji: String = "🌱"): Riego {
    return Riego(
        id = this.id,
        cultivoId = this.cultivoId,
        cultivoNombre = cultivoNombre,
        cultivoEmoji = cultivoEmoji,
        fecha = this.fecha,
        cantidadMl = this.cantidadMl,
        metodo = try {
            MetodoRiego.valueOf(this.metodo)
        } catch (e: IllegalArgumentException) {
            MetodoRiego.MANUAL
        },
        notas = this.notas
    )
}

/**
 * Convierte Riego (UI Model) a RiegoEntity (Room).
 */
fun Riego.toEntity(): RiegoEntity {
    return RiegoEntity(
        id = this.id,
        cultivoId = this.cultivoId,
        fecha = this.fecha,
        cantidadMl = this.cantidadMl,
        metodo = this.metodo.name,
        notas = this.notas
    )
}

