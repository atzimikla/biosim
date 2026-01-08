package com.biosim.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biosim.model.EstadoCaptura
import com.biosim.model.EstadoPermisos
import com.biosim.model.FotoCaptura
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

/**
 * ViewModel para la pantalla de Cámara.
 * Maneja la captura de fotos con CameraX y obtención de ubicación.
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    
    // Cliente de ubicación
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Estado de la captura
    private val _estadoCaptura = MutableStateFlow<EstadoCaptura>(EstadoCaptura.Idle)
    val estadoCaptura: StateFlow<EstadoCaptura> = _estadoCaptura.asStateFlow()

    // Estado de permisos
    private val _estadoPermisos = MutableStateFlow(EstadoPermisos())
    val estadoPermisos: StateFlow<EstadoPermisos> = _estadoPermisos.asStateFlow()

    // Lista de fotos capturadas (en memoria, sin Room por ahora)
    private val _fotosCapturadas = MutableStateFlow<List<FotoCaptura>>(emptyList())
    val fotosCapturadas: StateFlow<List<FotoCaptura>> = _fotosCapturadas.asStateFlow()

    // Última ubicación conocida
    private var ultimaLatitud: Double? = null
    private var ultimaLongitud: Double? = null

    // Executor para captura de imagen
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Callback de ubicación
    private var locationCallback: LocationCallback? = null

    init {
        verificarPermisos()
    }

    /**
     * Verifica el estado actual de los permisos.
     */
    fun verificarPermisos() {
        val camaraPermitida = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val ubicacionPermitida = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _estadoPermisos.value = EstadoPermisos(
            camaraPermitida = camaraPermitida,
            ubicacionPermitida = ubicacionPermitida
        )

        // Si tiene permiso de ubicación, iniciar actualizaciones
        if (ubicacionPermitida) {
            iniciarActualizacionesUbicacion()
        }
    }

    /**
     * Actualiza el estado de permisos después de que el usuario responde.
     */
    fun actualizarPermisos(camara: Boolean, ubicacion: Boolean) {
        _estadoPermisos.value = EstadoPermisos(
            camaraPermitida = camara,
            ubicacionPermitida = ubicacion
        )
        
        if (ubicacion) {
            iniciarActualizacionesUbicacion()
        }
    }

    /**
     * Inicia las actualizaciones de ubicación en segundo plano.
     */
    @SuppressLint("MissingPermission")
    private fun iniciarActualizacionesUbicacion() {
        if (!_estadoPermisos.value.ubicacionPermitida) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Actualizar cada 10 segundos
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    ultimaLatitud = location.latitude
                    ultimaLongitud = location.longitude
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        // También intentar obtener última ubicación conocida
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                ultimaLatitud = it.latitude
                ultimaLongitud = it.longitude
            }
        }
    }

    /**
     * Captura una foto usando ImageCapture de CameraX.
     */
    fun capturarFoto(imageCapture: ImageCapture) {
        viewModelScope.launch {
            _estadoCaptura.value = EstadoCaptura.Capturando

            // Crear archivo para guardar la foto
            val photoFile = crearArchivoFoto()

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        viewModelScope.launch {
                            _estadoCaptura.value = EstadoCaptura.ObteniendoUbicacion
                            
                            // Obtener ubicación actual
                            obtenerUbicacionActual { lat, lon ->
                                val fotoCaptura = FotoCaptura(
                                    path = photoFile.absolutePath,
                                    latitud = lat,
                                    longitud = lon,
                                    fecha = System.currentTimeMillis()
                                )

                                // Agregar a la lista de fotos
                                _fotosCapturadas.value = _fotosCapturadas.value + fotoCaptura

                                _estadoCaptura.value = EstadoCaptura.Exito(fotoCaptura)
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        viewModelScope.launch {
                            _estadoCaptura.value = EstadoCaptura.Error(
                                "Error al capturar foto: ${exception.message}"
                            )
                        }
                    }
                }
            )
        }
    }

    /**
     * Obtiene la ubicación actual de forma asíncrona.
     */
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual(callback: (Double?, Double?) -> Unit) {
        if (!_estadoPermisos.value.ubicacionPermitida) {
            callback(null, null)
            return
        }

        // Si ya tenemos ubicación reciente, usarla
        if (ultimaLatitud != null && ultimaLongitud != null) {
            callback(ultimaLatitud, ultimaLongitud)
            return
        }

        // Si no, intentar obtener una nueva
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback(location.latitude, location.longitude)
                } else {
                    callback(null, null)
                }
            }
            .addOnFailureListener {
                callback(null, null)
            }
    }

    /**
     * Crea un archivo único para guardar la foto.
     */
    private fun crearArchivoFoto(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        
        val storageDir = context.getExternalFilesDir(null)
            ?: context.filesDir
        
        val photosDir = File(storageDir, "fotos_cultivos").apply {
            if (!exists()) mkdirs()
        }

        return File(photosDir, "BIOSIM_${timestamp}.jpg")
    }

    /**
     * Resetea el estado de captura a Idle.
     */
    fun resetearEstado() {
        _estadoCaptura.value = EstadoCaptura.Idle
    }

    /**
     * Elimina una foto de la lista y del almacenamiento.
     */
    fun eliminarFoto(foto: FotoCaptura) {
        viewModelScope.launch {
            // Eliminar archivo
            val file = File(foto.path)
            if (file.exists()) {
                file.delete()
            }
            
            // Eliminar de la lista
            _fotosCapturadas.value = _fotosCapturadas.value.filter { it.id != foto.id }
        }
    }

    /**
     * Obtiene la última foto capturada.
     */
    fun obtenerUltimaFoto(): FotoCaptura? {
        return _fotosCapturadas.value.lastOrNull()
    }

    override fun onCleared() {
        super.onCleared()
        // Detener actualizaciones de ubicación
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        // Cerrar executor
        cameraExecutor.shutdown()
    }
}

