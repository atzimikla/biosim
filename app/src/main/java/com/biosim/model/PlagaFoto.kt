package com.biosim.model

import com.biosim.data.entity.PlagaFotoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo UI para fotos de inspecciones de plagas con geolocalización.
 */
data class PlagaFoto(
    val id: Int = 0,
    val inspeccionId: Int,
    val fotoUri: String,
    val fecha: Long,
    val descripcion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
) {
    val fechaFormateada: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(fecha))
        }

    val tieneUbicacion: Boolean
        get() = latitud != null && longitud != null

    val coordenadasFormateadas: String
        get() = if (tieneUbicacion) "%.6f, %.6f".format(latitud, longitud) else "Sin ubicación"
}

/**
 * Estado UI para la pantalla de captura de fotos con ubicación.
 */
data class CapturarFotoPlagaUiState(
    val inspeccionId: Int = 0,
    val fotos: List<PlagaFoto> = emptyList(),
    val capturando: Boolean = false,
    val guardando: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val permisosCamara: Boolean = false,
    val permisosUbicacion: Boolean = false,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUriTemporal: String? = null,
    val errorMessage: String? = null,
    val fotoGuardada: Boolean = false
)

/**
 * Estado UI para la pantalla del mapa de una foto.
 */
sealed class FotoMapaUiState {
    data object Loading : FotoMapaUiState()
    data class Success(val foto: PlagaFoto) : FotoMapaUiState()
    data class Error(val mensaje: String) : FotoMapaUiState()
}

// ══════════════════════════════════════════════════════════════
// EXTENSIONES DE CONVERSIÓN
// ══════════════════════════════════════════════════════════════

fun PlagaFotoEntity.toModel(): PlagaFoto {
    return PlagaFoto(
        id = this.id,
        inspeccionId = this.inspeccionId,
        fotoUri = this.fotoUri,
        fecha = this.fecha,
        descripcion = this.descripcion,
        latitud = this.latitud,
        longitud = this.longitud
    )
}

fun PlagaFoto.toEntity(): PlagaFotoEntity {
    return PlagaFotoEntity(
        id = this.id,
        inspeccionId = this.inspeccionId,
        fotoUri = this.fotoUri,
        fecha = this.fecha,
        descripcion = this.descripcion,
        latitud = this.latitud,
        longitud = this.longitud
    )
}

