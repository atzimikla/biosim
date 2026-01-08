package com.biosim.model

import com.biosim.data.entity.HallazgoEntity
import com.biosim.data.entity.TipoHallazgo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo UI para hallazgos de cultivo.
 */
data class Hallazgo(
    val id: Int = 0,
    val cultivoId: Int,
    val cultivoNombre: String = "",
    val cultivoEmoji: String = "🌱",
    val fecha: Long,
    val fotoUri: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String? = null,
    val tipoHallazgo: TipoHallazgo = TipoHallazgo.OBSERVACION
) {
    val fechaFormateada: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(fecha))
        }

    val fechaCorta: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(fecha))
        }

    val coordenadasFormateadas: String
        get() = "%.6f, %.6f".format(latitud, longitud)
}

// ══════════════════════════════════════════════════════════════
// UI STATES
// ══════════════════════════════════════════════════════════════

sealed class HallazgosUiState {
    data object Loading : HallazgosUiState()
    data class Success(val hallazgos: List<Hallazgo>) : HallazgosUiState()
    data class Error(val mensaje: String) : HallazgosUiState()
    data object Empty : HallazgosUiState()
}

sealed class HallazgoDetalleUiState {
    data object Loading : HallazgoDetalleUiState()
    data class Success(val hallazgo: Hallazgo) : HallazgoDetalleUiState()
    data class Error(val mensaje: String) : HallazgoDetalleUiState()
}

data class CapturarHallazgoUiState(
    val cultivoSeleccionadoId: Int? = null,
    val tipoHallazgo: TipoHallazgo = TipoHallazgo.OBSERVACION,
    val descripcion: String = "",
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val obteniendoUbicacion: Boolean = false,
    val capturandoFoto: Boolean = false,
    val guardando: Boolean = false,
    val errorCultivo: String? = null,
    val errorFoto: String? = null,
    val errorUbicacion: String? = null,
    val guardadoExitoso: Boolean = false,
    val permisosCamara: Boolean = false,
    val permisosUbicacion: Boolean = false
)

// ══════════════════════════════════════════════════════════════
// EXTENSIONES DE CONVERSIÓN
// ══════════════════════════════════════════════════════════════

fun HallazgoEntity.toModel(
    cultivoNombre: String = "",
    cultivoEmoji: String = "🌱"
): Hallazgo {
    return Hallazgo(
        id = this.id,
        cultivoId = this.cultivoId,
        cultivoNombre = cultivoNombre,
        cultivoEmoji = cultivoEmoji,
        fecha = this.fecha,
        fotoUri = this.fotoUri,
        latitud = this.latitud,
        longitud = this.longitud,
        descripcion = this.descripcion,
        tipoHallazgo = try { TipoHallazgo.valueOf(this.tipoHallazgo) } catch (e: Exception) { TipoHallazgo.OBSERVACION }
    )
}

fun Hallazgo.toEntity(): HallazgoEntity {
    return HallazgoEntity(
        id = this.id,
        cultivoId = this.cultivoId,
        fecha = this.fecha,
        fotoUri = this.fotoUri,
        latitud = this.latitud,
        longitud = this.longitud,
        descripcion = this.descripcion,
        tipoHallazgo = this.tipoHallazgo.name
    )
}

