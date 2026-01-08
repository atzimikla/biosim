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
import com.biosim.data.repository.PlagaFotoRepository
import com.biosim.model.CapturarFotoPlagaUiState
import com.biosim.model.FotoMapaUiState
import com.biosim.model.PlagaFoto
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

/**
 * ViewModel para gestionar fotos de inspecciones de plagas con geolocalización.
 * 
 * CORRECCIÓN DEL BUG:
 * - Se usa MutableStateFlow para las fotos en lugar de reasignar el Flow
 * - Se cancela el job anterior al cambiar de inspección
 * - Se asegura que las fotos se carguen inmediatamente al inicializar
 */
class PlagaFotoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BiosimDatabase.obtenerDatabase(application)
    private val plagaFotoRepository = PlagaFotoRepository(database.plagaFotoDao())

    // Cliente de ubicación
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private var locationCallback: LocationCallback? = null

    // ══════════════════════════════════════════════════════════════
    // ESTADO UI - FOTOS (CORREGIDO)
    // ══════════════════════════════════════════════════════════════

    /**
     * CORRECCIÓN: Usar MutableStateFlow fijo en lugar de reasignar el StateFlow.
     * Esto garantiza que la UI siempre esté suscrita al mismo Flow.
     */
    private val _fotos = MutableStateFlow<List<PlagaFoto>>(emptyList())
    val fotos: StateFlow<List<PlagaFoto>> = _fotos.asStateFlow()

    /**
     * Job para cancelar la colección anterior cuando cambia el ID.
     */
    private var fotosCollectionJob: Job? = null

    /**
     * ID de la inspección actualmente cargada para evitar recargas innecesarias.
     */
    private var currentInspeccionId: Int? = null

    // ══════════════════════════════════════════════════════════════
    // ESTADO UI - CAPTURA
    // ══════════════════════════════════════════════════════════════

    private val _uiState = MutableStateFlow(CapturarFotoPlagaUiState())
    val uiState: StateFlow<CapturarFotoPlagaUiState> = _uiState.asStateFlow()

    // ══════════════════════════════════════════════════════════════
    // ESTADO UI - MAPA DE FOTO
    // ══════════════════════════════════════════════════════════════

    private val _fotoMapaState = MutableStateFlow<FotoMapaUiState>(FotoMapaUiState.Loading)
    val fotoMapaState: StateFlow<FotoMapaUiState> = _fotoMapaState.asStateFlow()

    // ══════════════════════════════════════════════════════════════
    // INICIALIZACIÓN (CORREGIDO)
    // ══════════════════════════════════════════════════════════════

    /**
     * Carga las fotos para una inspección específica.
     * 
     * CORRECCIÓN:
     * 1. Verifica si ya está cargado el mismo ID para evitar recargas
     * 2. Cancela el job anterior antes de iniciar uno nuevo
     * 3. Colecta el Flow del repository y actualiza el MutableStateFlow
     * 4. Garantiza que las fotos se carguen inmediatamente
     */
    fun cargarFotos(inspeccionId: Int) {
        // Evitar recarga si ya estamos en la misma inspección
        if (currentInspeccionId == inspeccionId && _fotos.value.isNotEmpty()) {
            return
        }

        currentInspeccionId = inspeccionId
        _uiState.value = _uiState.value.copy(inspeccionId = inspeccionId)

        // Cancelar colección anterior
        fotosCollectionJob?.cancel()

        // Iniciar nueva colección
        fotosCollectionJob = viewModelScope.launch {
            plagaFotoRepository.obtenerPorInspeccion(inspeccionId)
                .collect { listaFotos ->
                    _fotos.value = listaFotos
                    _uiState.value = _uiState.value.copy(fotos = listaFotos)
                }
        }
    }

    /**
     * Fuerza la recarga de fotos (útil después de agregar una nueva).
     */
    fun recargarFotos() {
        currentInspeccionId?.let { id ->
            currentInspeccionId = null // Forzar recarga
            cargarFotos(id)
        }
    }

    /**
     * Inicializa el ViewModel para la pantalla de captura.
     * Similar a cargarFotos pero también configura el estado de captura.
     */
    fun inicializarCaptura(inspeccionId: Int) {
        cargarFotos(inspeccionId)
    }

    // ══════════════════════════════════════════════════════════════
    // PERMISOS
    // ══════════════════════════════════════════════════════════════

    fun actualizarPermisos(camara: Boolean, ubicacion: Boolean) {
        _uiState.value = _uiState.value.copy(
            permisosCamara = camara,
            permisosUbicacion = ubicacion
        )
    }

    fun verificarPermisos(): Pair<Boolean, Boolean> {
        val context = getApplication<Application>()
        val camaraGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        val ubicacionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(
            permisosCamara = camaraGranted,
            permisosUbicacion = ubicacionGranted
        )
        return Pair(camaraGranted, ubicacionGranted)
    }

    // ══════════════════════════════════════════════════════════════
    // CAPTURA DE FOTO CON UBICACIÓN
    // ══════════════════════════════════════════════════════════════

    fun capturarFotoConUbicacion(
        imageCapture: ImageCapture,
        executor: Executor,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val inspeccionId = _uiState.value.inspeccionId
        if (inspeccionId == 0) {
            onError("Inspección no válida")
            return
        }

        _uiState.value = _uiState.value.copy(
            capturando = true,
            errorMessage = null,
            fotoUriTemporal = null,
            latitud = null,
            longitud = null
        )

        val context = getApplication<Application>()
        val photoFile = crearArchivoFoto(context, inspeccionId)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(photoFile)
                    _uiState.value = _uiState.value.copy(
                        capturando = false,
                        fotoUriTemporal = uri.toString()
                    )
                    obtenerUbicacionYGuardar(uri.toString(), onSuccess, onError)
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = _uiState.value.copy(
                        capturando = false,
                        errorMessage = "Error al capturar: ${exception.message}"
                    )
                    onError(exception.message ?: "Error desconocido")
                }
            }
        )
    }

    private fun obtenerUbicacionYGuardar(
        fotoUri: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val context = getApplication<Application>()
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            guardarFotoEnRoom(fotoUri, null, null, onSuccess)
            return
        }

        _uiState.value = _uiState.value.copy(obteniendoUbicacion = true)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(5000)
            .setMaxUpdates(1)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _uiState.value = _uiState.value.copy(
                        obteniendoUbicacion = false,
                        latitud = location.latitude,
                        longitud = location.longitude
                    )
                    guardarFotoEnRoom(fotoUri, location.latitude, location.longitude, onSuccess)
                } ?: run {
                    _uiState.value = _uiState.value.copy(obteniendoUbicacion = false)
                    guardarFotoEnRoom(fotoUri, null, null, onSuccess)
                }
                locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )

            viewModelScope.launch {
                kotlinx.coroutines.delay(10000)
                if (_uiState.value.obteniendoUbicacion) {
                    locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
                    _uiState.value = _uiState.value.copy(obteniendoUbicacion = false)
                    guardarFotoEnRoom(fotoUri, null, null, onSuccess)
                }
            }
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                obteniendoUbicacion = false,
                errorMessage = "Error de permisos: ${e.message}"
            )
            guardarFotoEnRoom(fotoUri, null, null, onSuccess)
        }
    }

    private fun guardarFotoEnRoom(
        fotoUri: String,
        latitud: Double?,
        longitud: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            try {
                plagaFotoRepository.insertar(
                    inspeccionId = _uiState.value.inspeccionId,
                    fotoUri = fotoUri,
                    latitud = latitud,
                    longitud = longitud
                )
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    fotoGuardada = true,
                    fotoUriTemporal = null
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorMessage = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MAPA DE FOTO
    // ══════════════════════════════════════════════════════════════

    fun cargarFotoParaMapa(fotoId: Int) {
        viewModelScope.launch {
            _fotoMapaState.value = FotoMapaUiState.Loading
            try {
                val foto = plagaFotoRepository.obtenerPorId(fotoId)
                if (foto != null && foto.tieneUbicacion) {
                    _fotoMapaState.value = FotoMapaUiState.Success(foto)
                } else if (foto != null) {
                    _fotoMapaState.value = FotoMapaUiState.Error("Esta foto no tiene ubicación")
                } else {
                    _fotoMapaState.value = FotoMapaUiState.Error("Foto no encontrada")
                }
            } catch (e: Exception) {
                _fotoMapaState.value = FotoMapaUiState.Error(e.message ?: "Error")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ELIMINAR FOTO
    // ══════════════════════════════════════════════════════════════

    fun eliminarFoto(foto: PlagaFoto) {
        viewModelScope.launch {
            try {
                val file = File(Uri.parse(foto.fotoUri).path ?: "")
                if (file.exists()) {
                    file.delete()
                }
                plagaFotoRepository.eliminarPorId(foto.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar: ${e.message}"
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════

    private fun crearArchivoFoto(context: Context, inspeccionId: Int): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile("PLAGA_${inspeccionId}_${timestamp}_", ".jpg", storageDir)
    }

    fun resetearEstadoFotoGuardada() {
        _uiState.value = _uiState.value.copy(fotoGuardada = false)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Limpia el estado cuando se sale de la pantalla.
     */
    fun limpiarEstado() {
        currentInspeccionId = null
        _fotos.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        fotosCollectionJob?.cancel()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }
}
