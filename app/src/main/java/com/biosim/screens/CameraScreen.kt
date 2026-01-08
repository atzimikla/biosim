package com.biosim.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.model.EstadoCaptura
import com.biosim.model.FotoCaptura
import com.biosim.viewmodel.CameraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Cámara con CameraX.
 * Permite capturar fotos y obtener la ubicación GPS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(),
    onBack: () -> Unit = {},
    onFotoCapturada: (FotoCaptura) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val estadoCaptura by viewModel.estadoCaptura.collectAsState()
    val estadoPermisos by viewModel.estadoPermisos.collectAsState()

    // ImageCapture para tomar fotos
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Launcher para solicitar permisos
    val permisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        viewModel.actualizarPermisos(
            camara = permisos[Manifest.permission.CAMERA] == true,
            ubicacion = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
        )
    }

    // Verificar permisos al iniciar
    LaunchedEffect(Unit) {
        viewModel.verificarPermisos()
    }

    // Notificar cuando se captura foto exitosamente
    LaunchedEffect(estadoCaptura) {
        if (estadoCaptura is EstadoCaptura.Exito) {
            onFotoCapturada((estadoCaptura as EstadoCaptura.Exito).foto)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📷 Cámara",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF455A64),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Si no tiene permisos, mostrar pantalla de solicitud
                !estadoPermisos.camaraPermitida -> {
                    PantallaPermisos(
                        onSolicitarPermisos = {
                            permisosLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    )
                }

                // Si tiene permisos, mostrar cámara
                else -> {
                    // Preview de cámara
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onImageCaptureReady = { capture ->
                            imageCapture = capture
                        }
                    )

                    // Overlay con controles
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Indicador de ubicación
                        UbicacionIndicador(
                            tieneUbicacion = estadoPermisos.ubicacionPermitida
                        )

                        // Controles inferiores
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Estado de captura
                            when (val estado = estadoCaptura) {
                                is EstadoCaptura.Idle -> {
                                    // Botón de captura
                                    BotonCaptura(
                                        onClick = {
                                            imageCapture?.let { viewModel.capturarFoto(it) }
                                        },
                                        enabled = imageCapture != null
                                    )
                                }

                                is EstadoCaptura.Capturando,
                                is EstadoCaptura.ObteniendoUbicacion -> {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (estado is EstadoCaptura.Capturando)
                                            "Capturando..." else "Obteniendo ubicación...",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                is EstadoCaptura.Exito -> {
                                    FotoCapturaExito(
                                        foto = estado.foto,
                                        onContinuar = { viewModel.resetearEstado() }
                                    )
                                }

                                is EstadoCaptura.Error -> {
                                    ErrorCaptura(
                                        mensaje = estado.mensaje,
                                        onReintentar = { viewModel.resetearEstado() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview de la cámara usando CameraX.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                onImageCaptureReady(imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            // Cleanup si es necesario
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * Pantalla para solicitar permisos.
 */
@Composable
fun PantallaPermisos(
    onSolicitarPermisos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF37474F))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📷",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permisos Necesarios",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Para capturar fotos de tus cultivos con ubicación GPS, necesitamos acceso a la cámara y ubicación.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSolicitarPermisos,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF43A047)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Conceder Permisos",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Indicador de estado de ubicación.
 */
@Composable
fun UbicacionIndicador(tieneUbicacion: Boolean) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tieneUbicacion)
                Color(0xFF43A047).copy(alpha = 0.9f)
            else
                Color(0xFFFF5722).copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (tieneUbicacion) "GPS Activo" else "Sin GPS",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Botón circular para capturar foto.
 */
@Composable
fun BotonCaptura(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF455A64),
            disabledContainerColor = Color.Gray
        )
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF455A64))
        )
    }
}

/**
 * Card que muestra foto capturada exitosamente.
 */
@Composable
fun FotoCapturaExito(
    foto: FotoCaptura,
    onContinuar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF43A047)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¡Foto Capturada!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de la foto
            Text(
                text = "📍 ${foto.coordenadasFormateadas()}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp
            )
            
            Text(
                text = "📅 ${formatearFecha(foto.fecha)}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onContinuar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF43A047)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tomar Otra")
            }
        }
    }
}

/**
 * Card de error al capturar.
 */
@Composable
fun ErrorCaptura(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE53935)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Error",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensaje,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onReintentar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFE53935)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reintentar")
            }
        }
    }
}

/**
 * Formatea una fecha en milisegundos a string legible.
 */
private fun formatearFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

