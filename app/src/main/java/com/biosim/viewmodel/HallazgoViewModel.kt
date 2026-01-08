package com.biosim.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosim.data.database.BiosimDatabase
import com.biosim.data.entity.TipoHallazgo
import com.biosim.data.repository.CultivoRepository
import com.biosim.data.repository.HallazgoRepository
import com.biosim.model.CapturarHallazgoUiState
import com.biosim.model.Cultivo
import com.biosim.model.Hallazgo
import com.biosim.model.HallazgoDetalleUiState
import com.biosim.model.HallazgosUiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

/**
 * ViewModel para el módulo de Hallazgos de Cultivo.
 */
class HallazgoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BiosimDatabase.obtenerDatabase(application)
    private val hallazgoRepository = HallazgoRepository(
        hallazgoDao = database.hallazgoDao(),
        cultivoDao = database.cultivoDao()
    )
    private val cultivoRepository = CultivoRepository(database.cultivoDao())

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // ══════════════════════════════════════════════════════════════
    // LISTA DE HALLAZGOS
    // ══════════════════════════════════════════════════════════════

    val hallazgosUiState: StateFlow<HallazgosUiState> = hallazgoRepository.todosLosHallazgos
        .map { hallazgos ->
            if (hallazgos.isEmpty()) HallazgosUiState.Empty
            else HallazgosUiState.Success(hallazgos)
        }
        .catch { emit(HallazgosUiState.Error(it.message ?: "Error desconocido")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HallazgosUiState.Loading)

    // ══════════════════════════════════════════════════════════════
    // DETALLE DE HALLAZGO
    // ══════════════════════════════════════════════════════════════

    private val _hallazgoDetalleState = MutableStateFlow<HallazgoDetalleUiState>(HallazgoDetalleUiState.Loading)
    val hallazgoDetalleState: StateFlow<HallazgoDetalleUiState> = _hallazgoDetalleState.asStateFlow()

    fun cargarHallazgo(id: Int) {
        viewModelScope.launch {
            _hallazgoDetalleState.value = HallazgoDetalleUiState.Loading
            try {
                val hallazgo = hallazgoRepository.obtenerPorId(id)
                if (hallazgo != null) {
                    _hallazgoDetalleState.value = HallazgoDetalleUiState.Success(hallazgo)
                } else {
                    _hallazgoDetalleState.value = HallazgoDetalleUiState.Error("Hallazgo no encontrado")
                }
            } catch (e: Exception) {
                _hallazgoDetalleState.value = HallazgoDetalleUiState.Error(e.message ?: "Error")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CAPTURAR HALLAZGO
    // ══════════════════════════════════════════════════════════════

    private val _capturarState = MutableStateFlow(CapturarHallazgoUiState())
    val capturarState: StateFlow<CapturarHallazgoUiState> = _capturarState.asStateFlow()

    val cultivosDisponibles: StateFlow<List<Cultivo>> = cultivoRepository.todosLosCultivos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarCultivo(cultivoId: Int) {
        _capturarState.value = _capturarState.value.copy(
            cultivoSeleccionadoId = cultivoId,
            errorCultivo = null
        )
    }

    fun seleccionarTipoHallazgo(tipo: TipoHallazgo) {
        _capturarState.value = _capturarState.value.copy(tipoHallazgo = tipo)
    }

    fun actualizarDescripcion(descripcion: String) {
        _capturarState.value = _capturarState.value.copy(descripcion = descripcion)
    }

    fun actualizarPermisos(camara: Boolean, ubicacion: Boolean) {
        _capturarState.value = _capturarState.value.copy(
            permisosCamara = camara,
            permisosUbicacion = ubicacion
        )
    }

    /**
     * Captura una foto usando CameraX.
     */
    fun capturarFoto(
        imageCapture: ImageCapture,
        executor: Executor,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        _capturarState.value = _capturarState.value.copy(capturandoFoto = true, errorFoto = null)

        val context = getApplication<Application>()
        val photoFile = crearArchivoFoto(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(photoFile)
                    _capturarState.value = _capturarState.value.copy(
                        fotoUri = uri.toString(),
                        capturandoFoto = false
                    )
                    onSuccess(uri)
                    // Automáticamente obtener ubicación después de la foto
                    obtenerUbicacion()
                }

                override fun onError(exception: ImageCaptureException) {
                    _capturarState.value = _capturarState.value.copy(
                        capturandoFoto = false,
                        errorFoto = "Error al capturar: ${exception.message}"
                    )
                    onError(exception.message ?: "Error desconocido")
                }
            }
        )
    }

    /**
     * Obtiene la ubicación GPS actual.
     */
    fun obtenerUbicacion() {
        val context = getApplication<Application>()
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            _capturarState.value = _capturarState.value.copy(
                errorUbicacion = "Permiso de ubicación no concedido"
            )
            return
        }

        _capturarState.value = _capturarState.value.copy(obteniendoUbicacion = true, errorUbicacion = null)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(5000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _capturarState.value = _capturarState.value.copy(
                        latitud = location.latitude,
                        longitud = location.longitude,
                        obteniendoUbicacion = false
                    )
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            _capturarState.value = _capturarState.value.copy(
                obteniendoUbicacion = false,
                errorUbicacion = "Error de seguridad: ${e.message}"
            )
        }
    }

    /**
     * Guarda el hallazgo en la base de datos.
     */
    fun guardarHallazgo() {
        val state = _capturarState.value
        var hayErrores = false

        if (state.cultivoSeleccionadoId == null) {
            _capturarState.value = state.copy(errorCultivo = "Selecciona un cultivo")
            hayErrores = true
        }

        if (state.fotoUri == null) {
            _capturarState.value = _capturarState.value.copy(errorFoto = "Captura una foto")
            hayErrores = true
        }

        if (state.latitud == null || state.longitud == null) {
            _capturarState.value = _capturarState.value.copy(errorUbicacion = "Obtén la ubicación")
            hayErrores = true
        }

        if (hayErrores) return

        viewModelScope.launch {
            _capturarState.value = _capturarState.value.copy(guardando = true)
            try {
                hallazgoRepository.insertar(
                    cultivoId = state.cultivoSeleccionadoId!!,
                    fecha = System.currentTimeMillis(),
                    fotoUri = state.fotoUri!!,
                    latitud = state.latitud!!,
                    longitud = state.longitud!!,
                    descripcion = state.descripcion.ifBlank { null },
                    tipoHallazgo = state.tipoHallazgo.name
                )
                _capturarState.value = _capturarState.value.copy(
                    guardando = false,
                    guardadoExitoso = true
                )
            } catch (e: Exception) {
                _capturarState.value = _capturarState.value.copy(
                    guardando = false,
                    errorCultivo = "Error: ${e.message}"
                )
            }
        }
    }

    fun resetearFormulario() {
        _capturarState.value = CapturarHallazgoUiState()
    }

    // ══════════════════════════════════════════════════════════════
    // ACCIONES
    // ══════════════════════════════════════════════════════════════

    fun eliminarHallazgo(id: Int) {
        viewModelScope.launch {
            hallazgoRepository.eliminarPorId(id)
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════

    private fun crearArchivoFoto(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile("HALLAZGO_${timestamp}_", ".jpg", storageDir)
    }
}

