package com.biosim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosim.data.database.BiosimDatabase
import com.biosim.data.entity.MetodoAplicacion
import com.biosim.data.entity.TipoNutriente
import com.biosim.data.repository.CultivoRepository
import com.biosim.data.repository.NutrienteRepository
import com.biosim.model.AgregarNutrienteUiState
import com.biosim.model.Cultivo
import com.biosim.model.NutrienteAplicacion
import com.biosim.model.NutrientesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de Nutrientes.
 */
class NutrienteViewModel(application: Application) : AndroidViewModel(application) {

    // Base de datos y repositorios
    private val database = BiosimDatabase.obtenerDatabase(application)
    private val nutrienteRepository = NutrienteRepository(
        nutrienteDao = database.nutrienteDao(),
        cultivoDao = database.cultivoDao()
    )
    private val cultivoRepository = CultivoRepository(database.cultivoDao())

    // ══════════════════════════════════════════════════════════════
    // Estado para la LISTA de aplicaciones de nutrientes
    // ══════════════════════════════════════════════════════════════
    
    val nutrientesUiState: StateFlow<NutrientesUiState> = nutrienteRepository.todasLasAplicaciones
        .map { aplicaciones ->
            if (aplicaciones.isEmpty()) {
                NutrientesUiState.Empty
            } else {
                NutrientesUiState.Success(aplicaciones)
            }
        }
        .catch { e ->
            emit(NutrientesUiState.Error(e.message ?: "Error desconocido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NutrientesUiState.Loading
        )

    // ══════════════════════════════════════════════════════════════
    // Estado para AGREGAR aplicación de nutriente
    // ══════════════════════════════════════════════════════════════
    
    private val _agregarNutrienteState = MutableStateFlow(AgregarNutrienteUiState())
    val agregarNutrienteState: StateFlow<AgregarNutrienteUiState> = _agregarNutrienteState.asStateFlow()

    /**
     * Lista de cultivos disponibles para el dropdown.
     */
    val cultivosDisponibles: StateFlow<List<Cultivo>> = cultivoRepository.todosLosCultivos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ══════════════════════════════════════════════════════════════
    // Acciones del formulario
    // ══════════════════════════════════════════════════════════════

    /**
     * Selecciona un cultivo en el formulario.
     */
    fun seleccionarCultivo(cultivoId: Int) {
        _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
            cultivoSeleccionadoId = cultivoId,
            errorCultivo = null
        )
    }

    /**
     * Actualiza la fecha de la aplicación.
     */
    fun actualizarFecha(fecha: Long) {
        _agregarNutrienteState.value = _agregarNutrienteState.value.copy(fecha = fecha)
    }

    /**
     * Selecciona el tipo de nutriente.
     */
    fun seleccionarTipoNutriente(tipo: TipoNutriente) {
        _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
            tipoSeleccionado = tipo
        )
    }

    /**
     * Actualiza la cantidad en gramos.
     */
    fun actualizarCantidad(cantidad: String) {
        _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
            cantidadGramos = cantidad,
            errorCantidad = null
        )
    }

    /**
     * Selecciona el método de aplicación.
     */
    fun seleccionarMetodo(metodo: MetodoAplicacion) {
        _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
            metodoSeleccionado = metodo
        )
    }

    /**
     * Actualiza el comentario.
     */
    fun actualizarComentario(comentario: String) {
        _agregarNutrienteState.value = _agregarNutrienteState.value.copy(comentario = comentario)
    }

    /**
     * Valida y guarda la aplicación de nutriente.
     */
    fun guardarAplicacion() {
        val state = _agregarNutrienteState.value
        var hayErrores = false

        // Validar cultivo
        if (state.cultivoSeleccionadoId == null) {
            _agregarNutrienteState.value = state.copy(
                errorCultivo = "Selecciona un cultivo"
            )
            hayErrores = true
        }

        // Validar cantidad
        val cantidad = state.cantidadGramos.toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
                errorCantidad = "Ingresa una cantidad válida (mayor a 0)"
            )
            hayErrores = true
        } else if (cantidad > 10000) {
            _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
                errorCantidad = "Cantidad demasiado grande (máx 10kg)"
            )
            hayErrores = true
        }

        if (hayErrores) return

        // Guardar en Room
        viewModelScope.launch {
            _agregarNutrienteState.value = _agregarNutrienteState.value.copy(isLoading = true)
            
            try {
                nutrienteRepository.insertarAplicacion(
                    cultivoId = state.cultivoSeleccionadoId!!,
                    fecha = state.fecha,
                    tipoNutriente = state.tipoSeleccionado.name,
                    cantidadGramos = cantidad!!,
                    metodoAplicacion = state.metodoSeleccionado.name,
                    comentario = state.comentario.ifBlank { null }
                )
                
                _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
                    isLoading = false,
                    guardadoExitoso = true
                )
            } catch (e: Exception) {
                _agregarNutrienteState.value = _agregarNutrienteState.value.copy(
                    isLoading = false,
                    errorCultivo = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    /**
     * Resetea el formulario.
     */
    fun resetearFormulario() {
        _agregarNutrienteState.value = AgregarNutrienteUiState()
    }

    // ══════════════════════════════════════════════════════════════
    // Acciones de la lista
    // ══════════════════════════════════════════════════════════════

    /**
     * Elimina una aplicación por ID.
     */
    fun eliminarAplicacion(id: Int) {
        viewModelScope.launch {
            try {
                nutrienteRepository.eliminarPorId(id)
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }

    /**
     * Obtiene estadísticas de nutrientes de un cultivo.
     */
    suspend fun obtenerEstadisticas(cultivoId: Int): EstadisticasNutriente {
        val totalAplicado = nutrienteRepository.obtenerTotalAplicado(cultivoId)
        val ultimaAplicacion = nutrienteRepository.obtenerUltimaAplicacion(cultivoId)
        return EstadisticasNutriente(
            totalGramos = totalAplicado,
            ultimaAplicacion = ultimaAplicacion
        )
    }
}

/**
 * Data class para estadísticas de nutrientes.
 */
data class EstadisticasNutriente(
    val totalGramos: Int,
    val ultimaAplicacion: NutrienteAplicacion?
)

