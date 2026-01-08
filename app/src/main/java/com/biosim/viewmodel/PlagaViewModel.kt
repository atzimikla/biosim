package com.biosim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosim.data.database.BiosimDatabase
import com.biosim.data.entity.Efectividad
import com.biosim.data.entity.MetodoAplicacionTratamiento
import com.biosim.data.entity.NivelIncidencia
import com.biosim.data.entity.ParteAfectada
import com.biosim.data.entity.TipoPlaga
import com.biosim.data.entity.TipoProducto
import com.biosim.data.repository.CultivoRepository
import com.biosim.data.repository.PlagaRepository
import com.biosim.model.AgregarInspeccionUiState
import com.biosim.model.AgregarTratamientoUiState
import com.biosim.model.Cultivo
import com.biosim.model.InspeccionDetalleUiState
import com.biosim.model.PlagaInspeccion
import com.biosim.model.PlagaTratamiento
import com.biosim.model.PlagasUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de Control de Plagas.
 */
class PlagaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BiosimDatabase.obtenerDatabase(application)
    private val plagaRepository = PlagaRepository(
        plagaDao = database.plagaDao(),
        cultivoDao = database.cultivoDao()
    )
    private val cultivoRepository = CultivoRepository(database.cultivoDao())

    // ══════════════════════════════════════════════════════════════
    // LISTA DE INSPECCIONES
    // ══════════════════════════════════════════════════════════════

    val plagasUiState: StateFlow<PlagasUiState> = plagaRepository.todasLasInspecciones
        .map { inspecciones ->
            if (inspecciones.isEmpty()) PlagasUiState.Empty
            else PlagasUiState.Success(inspecciones)
        }
        .catch { emit(PlagasUiState.Error(it.message ?: "Error desconocido")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlagasUiState.Loading)

    // ══════════════════════════════════════════════════════════════
    // DETALLE DE INSPECCIÓN
    // ══════════════════════════════════════════════════════════════

    private val _inspeccionSeleccionadaId = MutableStateFlow<Int?>(null)
    
    val inspeccionDetalleUiState: StateFlow<InspeccionDetalleUiState> = combine(
        _inspeccionSeleccionadaId,
        plagaRepository.todasLasInspecciones
    ) { id, _ -> id }
        .map { id ->
            if (id == null) {
                InspeccionDetalleUiState.Loading
            } else {
                val inspeccion = plagaRepository.obtenerInspeccionPorId(id)
                if (inspeccion != null) {
                    InspeccionDetalleUiState.Success(
                        inspeccion = inspeccion,
                        tratamientos = emptyList() // Se cargan por separado
                    )
                } else {
                    InspeccionDetalleUiState.Error("Inspección no encontrada")
                }
            }
        }
        .catch { emit(InspeccionDetalleUiState.Error(it.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InspeccionDetalleUiState.Loading)

    private val _tratamientosInspeccion = MutableStateFlow<List<PlagaTratamiento>>(emptyList())
    val tratamientosInspeccion: StateFlow<List<PlagaTratamiento>> = _tratamientosInspeccion.asStateFlow()

    fun cargarInspeccion(id: Int) {
        _inspeccionSeleccionadaId.value = id
        viewModelScope.launch {
            plagaRepository.obtenerTratamientosPorInspeccion(id)
                .collect { tratamientos ->
                    _tratamientosInspeccion.value = tratamientos
                }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // AGREGAR INSPECCIÓN
    // ══════════════════════════════════════════════════════════════

    private val _agregarInspeccionState = MutableStateFlow(AgregarInspeccionUiState())
    val agregarInspeccionState: StateFlow<AgregarInspeccionUiState> = _agregarInspeccionState.asStateFlow()

    val cultivosDisponibles: StateFlow<List<Cultivo>> = cultivoRepository.todosLosCultivos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarCultivoInspeccion(cultivoId: Int) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(
            cultivoSeleccionadoId = cultivoId,
            errorCultivo = null
        )
    }

    fun actualizarFechaInspeccion(fecha: Long) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(fecha = fecha)
    }

    fun seleccionarTipoPlaga(tipo: TipoPlaga) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(tipoPlaga = tipo)
    }

    fun actualizarNombrePlaga(nombre: String) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(
            nombrePlaga = nombre,
            errorNombre = null
        )
    }

    fun seleccionarNivelIncidencia(nivel: NivelIncidencia) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(nivelIncidencia = nivel)
    }

    fun seleccionarParteAfectada(parte: ParteAfectada) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(parteAfectada = parte)
    }

    fun actualizarObservacionesInspeccion(observaciones: String) {
        _agregarInspeccionState.value = _agregarInspeccionState.value.copy(observaciones = observaciones)
    }

    fun guardarInspeccion() {
        val state = _agregarInspeccionState.value
        var hayErrores = false

        if (state.cultivoSeleccionadoId == null) {
            _agregarInspeccionState.value = state.copy(errorCultivo = "Selecciona un cultivo")
            hayErrores = true
        }

        if (state.nombrePlaga.isBlank()) {
            _agregarInspeccionState.value = _agregarInspeccionState.value.copy(
                errorNombre = "Ingresa el nombre de la plaga"
            )
            hayErrores = true
        }

        if (hayErrores) return

        viewModelScope.launch {
            _agregarInspeccionState.value = _agregarInspeccionState.value.copy(isLoading = true)
            try {
                plagaRepository.insertarInspeccion(
                    cultivoId = state.cultivoSeleccionadoId!!,
                    fecha = state.fecha,
                    tipoPlaga = state.tipoPlaga.name,
                    nombrePlaga = state.nombrePlaga,
                    nivelIncidencia = state.nivelIncidencia.name,
                    parteAfectada = state.parteAfectada.name,
                    observaciones = state.observaciones.ifBlank { null }
                )
                _agregarInspeccionState.value = _agregarInspeccionState.value.copy(
                    isLoading = false,
                    guardadoExitoso = true
                )
            } catch (e: Exception) {
                _agregarInspeccionState.value = _agregarInspeccionState.value.copy(
                    isLoading = false,
                    errorCultivo = "Error: ${e.message}"
                )
            }
        }
    }

    fun resetearFormularioInspeccion() {
        _agregarInspeccionState.value = AgregarInspeccionUiState()
    }

    // ══════════════════════════════════════════════════════════════
    // AGREGAR TRATAMIENTO
    // ══════════════════════════════════════════════════════════════

    private val _agregarTratamientoState = MutableStateFlow(AgregarTratamientoUiState())
    val agregarTratamientoState: StateFlow<AgregarTratamientoUiState> = _agregarTratamientoState.asStateFlow()

    fun actualizarProducto(producto: String) {
        _agregarTratamientoState.value = _agregarTratamientoState.value.copy(
            producto = producto,
            errorProducto = null
        )
    }

    fun seleccionarTipoProducto(tipo: TipoProducto) {
        _agregarTratamientoState.value = _agregarTratamientoState.value.copy(tipoProducto = tipo)
    }

    fun actualizarDosisTratamiento(dosis: String) {
        _agregarTratamientoState.value = _agregarTratamientoState.value.copy(
            dosisMl = dosis,
            errorDosis = null
        )
    }

    fun seleccionarMetodoTratamiento(metodo: MetodoAplicacionTratamiento) {
        _agregarTratamientoState.value = _agregarTratamientoState.value.copy(metodoAplicacion = metodo)
    }

    fun actualizarFechaTratamiento(fecha: Long) {
        _agregarTratamientoState.value = _agregarTratamientoState.value.copy(fechaAplicacion = fecha)
    }

    fun actualizarObservacionesTratamiento(observaciones: String) {
        _agregarTratamientoState.value = _agregarTratamientoState.value.copy(observaciones = observaciones)
    }

    fun guardarTratamiento(inspeccionId: Int) {
        val state = _agregarTratamientoState.value
        var hayErrores = false

        if (state.producto.isBlank()) {
            _agregarTratamientoState.value = state.copy(errorProducto = "Ingresa el nombre del producto")
            hayErrores = true
        }

        val dosis = state.dosisMl.toIntOrNull()
        if (dosis == null || dosis <= 0) {
            _agregarTratamientoState.value = _agregarTratamientoState.value.copy(
                errorDosis = "Ingresa una dosis válida"
            )
            hayErrores = true
        }

        if (hayErrores) return

        viewModelScope.launch {
            _agregarTratamientoState.value = _agregarTratamientoState.value.copy(isLoading = true)
            try {
                plagaRepository.insertarTratamiento(
                    inspeccionId = inspeccionId,
                    producto = state.producto,
                    tipoProducto = state.tipoProducto.name,
                    dosisMl = dosis!!,
                    metodoAplicacion = state.metodoAplicacion.name,
                    fechaAplicacion = state.fechaAplicacion,
                    observaciones = state.observaciones.ifBlank { null }
                )
                _agregarTratamientoState.value = _agregarTratamientoState.value.copy(
                    isLoading = false,
                    guardadoExitoso = true
                )
            } catch (e: Exception) {
                _agregarTratamientoState.value = _agregarTratamientoState.value.copy(
                    isLoading = false,
                    errorProducto = "Error: ${e.message}"
                )
            }
        }
    }

    fun resetearFormularioTratamiento() {
        _agregarTratamientoState.value = AgregarTratamientoUiState()
    }

    // ══════════════════════════════════════════════════════════════
    // ACCIONES
    // ══════════════════════════════════════════════════════════════

    fun eliminarInspeccion(id: Int) {
        viewModelScope.launch {
            plagaRepository.eliminarInspeccionPorId(id)
        }
    }

    fun marcarInspeccionResuelta(id: Int, resuelta: Boolean) {
        viewModelScope.launch {
            plagaRepository.marcarInspeccionResuelta(id, resuelta)
        }
    }

    fun eliminarTratamiento(id: Int) {
        viewModelScope.launch {
            plagaRepository.eliminarTratamientoPorId(id)
        }
    }

    fun actualizarEfectividadTratamiento(id: Int, efectividad: Efectividad) {
        viewModelScope.launch {
            plagaRepository.actualizarEfectividad(id, efectividad.name)
        }
    }
}

