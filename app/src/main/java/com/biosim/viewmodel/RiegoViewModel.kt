package com.biosim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosim.data.database.BiosimDatabase
import com.biosim.data.entity.MetodoRiego
import com.biosim.data.repository.CultivoRepository
import com.biosim.data.repository.RiegoRepository
import com.biosim.model.AgregarRiegoUiState
import com.biosim.model.Cultivo
import com.biosim.model.Riego
import com.biosim.model.RiegosUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de Riegos.
 * 
 * ¿Por qué AndroidViewModel en lugar de ViewModel?
 * - Necesitamos el contexto de Application para acceder a Room.
 * - AndroidViewModel sobrevive a cambios de configuración (rotación).
 * - El contexto de Application no causa memory leaks.
 * 
 * ¿Por qué separar UiState de los datos?
 * - UiState representa el estado visual de la pantalla.
 * - Permite manejar Loading, Error, Empty, Success de forma clara.
 * - La UI solo necesita observar un solo StateFlow.
 */
class RiegoViewModel(application: Application) : AndroidViewModel(application) {

    // Base de datos y repositorios
    private val database = BiosimDatabase.obtenerDatabase(application)
    private val riegoRepository = RiegoRepository(
        riegoDao = database.riegoDao(),
        cultivoDao = database.cultivoDao()
    )
    private val cultivoRepository = CultivoRepository(database.cultivoDao())

    // ══════════════════════════════════════════════════════════════
    // Estado para la LISTA de riegos
    // ══════════════════════════════════════════════════════════════
    
    /**
     * Estado de UI para la pantalla de lista de riegos.
     * 
     * ¿Por qué stateIn con WhileSubscribed?
     * - Convierte el Flow de Room en StateFlow para Compose.
     * - WhileSubscribed(5000) mantiene la suscripción 5 segundos después
     *   de que la UI deje de observar (evita reconexiones frecuentes).
     */
    val riegosUiState: StateFlow<RiegosUiState> = riegoRepository.todosLosRiegos
        .map { riegos ->
            if (riegos.isEmpty()) {
                RiegosUiState.Empty
            } else {
                RiegosUiState.Success(riegos)
            }
        }
        .catch { e ->
            emit(RiegosUiState.Error(e.message ?: "Error desconocido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RiegosUiState.Loading
        )

    // ══════════════════════════════════════════════════════════════
    // Estado para AGREGAR riego
    // ══════════════════════════════════════════════════════════════
    
    private val _agregarRiegoState = MutableStateFlow(AgregarRiegoUiState())
    val agregarRiegoState: StateFlow<AgregarRiegoUiState> = _agregarRiegoState.asStateFlow()

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
    // Acciones del formulario de agregar riego
    // ══════════════════════════════════════════════════════════════

    /**
     * Selecciona un cultivo en el formulario.
     */
    fun seleccionarCultivo(cultivoId: Int) {
        _agregarRiegoState.value = _agregarRiegoState.value.copy(
            cultivoSeleccionadoId = cultivoId,
            errorCultivo = null
        )
    }

    /**
     * Actualiza la fecha del riego.
     */
    fun actualizarFecha(fecha: Long) {
        _agregarRiegoState.value = _agregarRiegoState.value.copy(fecha = fecha)
    }

    /**
     * Actualiza la cantidad de agua.
     */
    fun actualizarCantidad(cantidad: String) {
        _agregarRiegoState.value = _agregarRiegoState.value.copy(
            cantidadMl = cantidad,
            errorCantidad = null
        )
    }

    /**
     * Selecciona el método de riego.
     */
    fun seleccionarMetodo(metodo: MetodoRiego) {
        _agregarRiegoState.value = _agregarRiegoState.value.copy(
            metodoSeleccionado = metodo
        )
    }

    /**
     * Actualiza las notas.
     */
    fun actualizarNotas(notas: String) {
        _agregarRiegoState.value = _agregarRiegoState.value.copy(notas = notas)
    }

    /**
     * Valida y guarda el riego.
     * 
     * ¿Por qué validar en el ViewModel?
     * - Centraliza la lógica de validación.
     * - La UI solo muestra los errores, no decide qué es válido.
     * - Facilita testing de las reglas de validación.
     */
    fun guardarRiego() {
        val state = _agregarRiegoState.value
        var hayErrores = false

        // Validar cultivo
        if (state.cultivoSeleccionadoId == null) {
            _agregarRiegoState.value = state.copy(
                errorCultivo = "Selecciona un cultivo"
            )
            hayErrores = true
        }

        // Validar cantidad
        val cantidad = state.cantidadMl.toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            _agregarRiegoState.value = _agregarRiegoState.value.copy(
                errorCantidad = "Ingresa una cantidad válida (mayor a 0)"
            )
            hayErrores = true
        } else if (cantidad > 100000) {
            _agregarRiegoState.value = _agregarRiegoState.value.copy(
                errorCantidad = "Cantidad demasiado grande (máx 100L)"
            )
            hayErrores = true
        }

        if (hayErrores) return

        // Guardar en Room
        viewModelScope.launch {
            _agregarRiegoState.value = _agregarRiegoState.value.copy(isLoading = true)
            
            try {
                riegoRepository.insertarRiego(
                    cultivoId = state.cultivoSeleccionadoId!!,
                    fecha = state.fecha,
                    cantidadMl = cantidad!!,
                    metodo = state.metodoSeleccionado.name,
                    notas = state.notas.ifBlank { null }
                )
                
                _agregarRiegoState.value = _agregarRiegoState.value.copy(
                    isLoading = false,
                    guardadoExitoso = true
                )
            } catch (e: Exception) {
                _agregarRiegoState.value = _agregarRiegoState.value.copy(
                    isLoading = false,
                    errorCultivo = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    /**
     * Resetea el formulario para agregar otro riego.
     */
    fun resetearFormulario() {
        _agregarRiegoState.value = AgregarRiegoUiState()
    }

    // ══════════════════════════════════════════════════════════════
    // Acciones de la lista de riegos
    // ══════════════════════════════════════════════════════════════

    /**
     * Elimina un riego por ID.
     */
    fun eliminarRiego(id: Int) {
        viewModelScope.launch {
            try {
                riegoRepository.eliminarPorId(id)
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }

    /**
     * Obtiene estadísticas de un cultivo.
     */
    suspend fun obtenerEstadisticasCultivo(cultivoId: Int): EstadisticasRiego {
        val totalAgua = riegoRepository.obtenerTotalAgua(cultivoId)
        val ultimoRiego = riegoRepository.obtenerUltimoRiego(cultivoId)
        return EstadisticasRiego(
            totalAguaMl = totalAgua,
            ultimoRiego = ultimoRiego
        )
    }
}

/**
 * Data class para estadísticas de riego.
 */
data class EstadisticasRiego(
    val totalAguaMl: Int,
    val ultimoRiego: Riego?
)

