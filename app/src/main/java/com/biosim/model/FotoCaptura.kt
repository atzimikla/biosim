package com.biosim.model

/**
 * Data class que representa una foto capturada con su ubicación.
 * Contiene la ruta del archivo, coordenadas GPS y fecha de captura.
 */
data class FotoCaptura(
    val id: Long = System.currentTimeMillis(),
    val path: String,
    val latitud: Double?,
    val longitud: Double?,
    val fecha: Long = System.currentTimeMillis(),
    
    // Campos opcionales para metadatos adicionales
    val cultivoId: Int? = null,
    val descripcion: String? = null
) {
    /**
     * Verifica si la foto tiene coordenadas de ubicación.
     */
    fun tieneUbicacion(): Boolean = latitud != null && longitud != null
    
    /**
     * Formatea las coordenadas para mostrar.
     */
    fun coordenadasFormateadas(): String {
        return if (tieneUbicacion()) {
            "Lat: %.6f, Lon: %.6f".format(latitud, longitud)
        } else {
            "Sin ubicación"
        }
    }
}

/**
 * Estado de la captura de foto.
 */
sealed class EstadoCaptura {
    data object Idle : EstadoCaptura()
    data object Capturando : EstadoCaptura()
    data object ObteniendoUbicacion : EstadoCaptura()
    data class Exito(val foto: FotoCaptura) : EstadoCaptura()
    data class Error(val mensaje: String) : EstadoCaptura()
}

/**
 * Estado de los permisos necesarios para la cámara.
 */
data class EstadoPermisos(
    val camaraPermitida: Boolean = false,
    val ubicacionPermitida: Boolean = false
) {
    val todosConcedidos: Boolean get() = camaraPermitida && ubicacionPermitida
}

