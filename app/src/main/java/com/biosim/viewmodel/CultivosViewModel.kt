package com.biosim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosim.data.database.BiosimDatabase
import com.biosim.data.repository.CultivoRepository
import com.biosim.model.Cultivo
import com.biosim.model.EstadoCultivo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Cultivos.
 * Maneja el estado de la lista de cultivos usando Room.
 * 
 * Usa AndroidViewModel para tener acceso al contexto de la aplicación.
 */
class CultivosViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia de la base de datos y repositorio
    private val database = BiosimDatabase.obtenerDatabase(application)
    private val repository = CultivoRepository(database.cultivoDao())

    // Estado de cultivos desde Room (Flow reactivo)
    val cultivos: StateFlow<List<Cultivo>> = repository.todosLosCultivos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Cultivo seleccionado para detalle
    private val _cultivoSeleccionado = MutableStateFlow<Cultivo?>(null)
    val cultivoSeleccionado: StateFlow<Cultivo?> = _cultivoSeleccionado.asStateFlow()

    /**
     * Carga un cultivo específico por ID.
     */
    fun cargarCultivo(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _cultivoSeleccionado.value = repository.obtenerPorId(id)
            } catch (e: Exception) {
                _error.value = "Error al cargar cultivo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene un cultivo por su ID de forma síncrona (desde la lista en memoria).
     */
    fun obtenerCultivoPorId(id: Int): Cultivo? {
        return cultivos.value.find { it.id == id }
    }

    /**
     * Agrega un nuevo cultivo.
     */
    fun agregarCultivo(cultivo: Cultivo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.insertar(cultivo)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al agregar cultivo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un cultivo existente.
     */
    fun actualizarCultivo(cultivo: Cultivo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.actualizar(cultivo)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar cultivo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un cultivo por ID.
     */
    fun eliminarCultivo(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.eliminarPorId(id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al eliminar cultivo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el estado de un cultivo.
     */
    fun actualizarEstado(id: Int, nuevoEstado: EstadoCultivo) {
        viewModelScope.launch {
            try {
                repository.actualizarEstado(id, nuevoEstado)
            } catch (e: Exception) {
                _error.value = "Error al actualizar estado: ${e.message}"
            }
        }
    }

    /**
     * Registra un riego (actualiza próximo riego).
     */
    fun registrarRiego(id: Int, proximoRiego: String) {
        viewModelScope.launch {
            try {
                repository.actualizarProximoRiego(id, proximoRiego)
            } catch (e: Exception) {
                _error.value = "Error al registrar riego: ${e.message}"
            }
        }
    }

    /**
     * Limpia el error actual.
     */
    fun limpiarError() {
        _error.value = null
    }

    /**
     * Refresca la lista de cultivos.
     * Los datos se actualizan automáticamente gracias al Flow.
     */
    fun refrescar() {
        // El Flow se actualiza automáticamente cuando hay cambios en Room
        // Este método puede usarse para forzar una recarga si es necesario
        _isLoading.value = false
    }
}

