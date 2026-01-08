package com.biosim.model

import com.biosim.data.entity.Efectividad
import com.biosim.data.entity.MetodoAplicacionTratamiento
import com.biosim.data.entity.NivelIncidencia
import com.biosim.data.entity.ParteAfectada
import com.biosim.data.entity.PlagaInspeccionEntity
import com.biosim.data.entity.PlagaTratamientoEntity
import com.biosim.data.entity.TipoPlaga
import com.biosim.data.entity.TipoProducto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════
// MODELO DE INSPECCIÓN
// ══════════════════════════════════════════════════════════════

data class PlagaInspeccion(
    val id: Int = 0,
    val cultivoId: Int,
    val cultivoNombre: String = "",
    val cultivoEmoji: String = "🌱",
    val fecha: Long,
    val tipoPlaga: TipoPlaga,
    val nombrePlaga: String,
    val nivelIncidencia: NivelIncidencia,
    val parteAfectada: ParteAfectada,
    val observaciones: String? = null,
    val fotoPath: String? = null,
    val resuelta: Boolean = false,
    val cantidadTratamientos: Int = 0
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
}

// ══════════════════════════════════════════════════════════════
// MODELO DE TRATAMIENTO
// ══════════════════════════════════════════════════════════════

data class PlagaTratamiento(
    val id: Int = 0,
    val inspeccionId: Int,
    val producto: String,
    val tipoProducto: TipoProducto,
    val dosisMl: Int,
    val metodoAplicacion: MetodoAplicacionTratamiento,
    val fechaAplicacion: Long,
    val observaciones: String? = null,
    val efectividad: Efectividad = Efectividad.PENDIENTE
) {
    val fechaFormateada: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(fechaAplicacion))
        }

    val dosisFormateada: String
        get() = if (dosisMl >= 1000) "%.1f L".format(dosisMl / 1000f) else "$dosisMl ml"
}

// ══════════════════════════════════════════════════════════════
// UI STATES
// ══════════════════════════════════════════════════════════════

sealed class PlagasUiState {
    data object Loading : PlagasUiState()
    data class Success(val inspecciones: List<PlagaInspeccion>) : PlagasUiState()
    data class Error(val mensaje: String) : PlagasUiState()
    data object Empty : PlagasUiState()
}

sealed class InspeccionDetalleUiState {
    data object Loading : InspeccionDetalleUiState()
    data class Success(
        val inspeccion: PlagaInspeccion,
        val tratamientos: List<PlagaTratamiento>
    ) : InspeccionDetalleUiState()
    data class Error(val mensaje: String) : InspeccionDetalleUiState()
}

data class AgregarInspeccionUiState(
    val cultivoSeleccionadoId: Int? = null,
    val fecha: Long = System.currentTimeMillis(),
    val tipoPlaga: TipoPlaga = TipoPlaga.INSECTO,
    val nombrePlaga: String = "",
    val nivelIncidencia: NivelIncidencia = NivelIncidencia.BAJO,
    val parteAfectada: ParteAfectada = ParteAfectada.HOJAS,
    val observaciones: String = "",
    val isLoading: Boolean = false,
    val errorCultivo: String? = null,
    val errorNombre: String? = null,
    val guardadoExitoso: Boolean = false
)

data class AgregarTratamientoUiState(
    val producto: String = "",
    val tipoProducto: TipoProducto = TipoProducto.ORGANICO,
    val dosisMl: String = "",
    val metodoAplicacion: MetodoAplicacionTratamiento = MetodoAplicacionTratamiento.FOLIAR,
    val fechaAplicacion: Long = System.currentTimeMillis(),
    val observaciones: String = "",
    val isLoading: Boolean = false,
    val errorProducto: String? = null,
    val errorDosis: String? = null,
    val guardadoExitoso: Boolean = false
)

// ══════════════════════════════════════════════════════════════
// EXTENSIONES DE CONVERSIÓN
// ══════════════════════════════════════════════════════════════

fun PlagaInspeccionEntity.toInspeccionModel(
    cultivoNombre: String = "",
    cultivoEmoji: String = "🌱",
    cantidadTratamientos: Int = 0
): PlagaInspeccion {
    return PlagaInspeccion(
        id = this.id,
        cultivoId = this.cultivoId,
        cultivoNombre = cultivoNombre,
        cultivoEmoji = cultivoEmoji,
        fecha = this.fecha,
        tipoPlaga = try { TipoPlaga.valueOf(this.tipoPlaga) } catch (e: Exception) { TipoPlaga.OTRO },
        nombrePlaga = this.nombrePlaga,
        nivelIncidencia = try { NivelIncidencia.valueOf(this.nivelIncidencia) } catch (e: Exception) { NivelIncidencia.BAJO },
        parteAfectada = try { ParteAfectada.valueOf(this.parteAfectada) } catch (e: Exception) { ParteAfectada.HOJAS },
        observaciones = this.observaciones,
        fotoPath = this.fotoPath,
        resuelta = this.resuelta,
        cantidadTratamientos = cantidadTratamientos
    )
}

fun PlagaInspeccion.toInspeccionEntity(): PlagaInspeccionEntity {
    return PlagaInspeccionEntity(
        id = this.id,
        cultivoId = this.cultivoId,
        fecha = this.fecha,
        tipoPlaga = this.tipoPlaga.name,
        nombrePlaga = this.nombrePlaga,
        nivelIncidencia = this.nivelIncidencia.name,
        parteAfectada = this.parteAfectada.name,
        observaciones = this.observaciones,
        fotoPath = this.fotoPath,
        resuelta = this.resuelta
    )
}

fun PlagaTratamientoEntity.toTratamientoModel(): PlagaTratamiento {
    return PlagaTratamiento(
        id = this.id,
        inspeccionId = this.inspeccionId,
        producto = this.producto,
        tipoProducto = try { TipoProducto.valueOf(this.tipoProducto) } catch (e: Exception) { TipoProducto.ORGANICO },
        dosisMl = this.dosisMl,
        metodoAplicacion = try { MetodoAplicacionTratamiento.valueOf(this.metodoAplicacion) } catch (e: Exception) { MetodoAplicacionTratamiento.FOLIAR },
        fechaAplicacion = this.fechaAplicacion,
        observaciones = this.observaciones,
        efectividad = try { this.efectividad?.let { Efectividad.valueOf(it) } ?: Efectividad.PENDIENTE } catch (e: Exception) { Efectividad.PENDIENTE }
    )
}

fun PlagaTratamiento.toTratamientoEntity(): PlagaTratamientoEntity {
    return PlagaTratamientoEntity(
        id = this.id,
        inspeccionId = this.inspeccionId,
        producto = this.producto,
        tipoProducto = this.tipoProducto.name,
        dosisMl = this.dosisMl,
        metodoAplicacion = this.metodoAplicacion.name,
        fechaAplicacion = this.fechaAplicacion,
        observaciones = this.observaciones,
        efectividad = this.efectividad.name
    )
}

