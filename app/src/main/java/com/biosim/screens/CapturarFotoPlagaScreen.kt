package com.biosim.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.biosim.model.PlagaFoto
import com.biosim.viewmodel.PlagaFotoViewModel
import java.util.concurrent.Executors

/**
 * Pantalla de captura de fotos con geolocalización para inspecciones de plagas.
 * 
 * Características:
 * - Vista previa de CameraX
 * - Captura foto + ubicación GPS automática
 * - Muestra estado de GPS en tiempo real
 * - Grid de fotos capturadas con indicador de ubicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturarFotoPlagaScreen(
    inspeccionId: Int,
    viewModel: PlagaFotoViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Permisos múltiples (cámara + ubicación)
    val permisos = remember {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val camaraGranted = permissions[Manifest.permission.CAMERA] == true
        val ubicacionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.actualizarPermisos(camaraGranted, ubicacionGranted)
    }

    LaunchedEffect(inspeccionId) {
        viewModel.inicializarCaptura(inspeccionId)
        
        val camaraGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val ubicacionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!camaraGranted || !ubicacionGranted) {
            permissionLauncher.launch(permisos)
        } else {
            viewModel.actualizarPermisos(camaraGranted, ubicacionGranted)
        }
    }

    LaunchedEffect(uiState.fotoGuardada) {
        if (uiState.fotoGuardada) {
            val mensaje = if (uiState.latitud != null) "📸📍 Foto guardada con ubicación" else "📸 Foto guardada"
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            viewModel.resetearEstadoFotoGuardada()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("📷 Capturar con GPS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${uiState.fotos.size} foto(s)", fontSize = 12.sp, color = Color.White.copy(0.8f))
                            if (uiState.permisosUbicacion) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp), tint = Color(0xFF4CAF50))
                                Text("GPS activo", fontSize = 12.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE65100),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFFF3E0)
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vista previa de cámara
            Card(
                Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (uiState.permisosCamara) {
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.Black)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()
                                    imageCapture = capture

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            capture
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Estado de obtención de ubicación
                        if (uiState.obteniendoUbicacion) {
                            Box(
                                Modifier.align(Alignment.TopCenter).padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2196F3))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Obteniendo ubicación...", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }

                        // Botón de captura
                        Box(
                            Modifier.align(Alignment.BottomCenter).padding(24.dp)
                                .size(80.dp).clip(CircleShape)
                                .background(Color.White.copy(0.9f))
                                .border(4.dp, Color(0xFFE65100), CircleShape)
                                .clickable(enabled = !uiState.capturando && !uiState.guardando && !uiState.obteniendoUbicacion) {
                                    imageCapture?.let { capture ->
                                        viewModel.capturarFotoConUbicacion(
                                            imageCapture = capture,
                                            executor = cameraExecutor,
                                            onError = { error ->
                                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                },
                            Alignment.Center
                        ) {
                            if (uiState.capturando || uiState.guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = Color(0xFFE65100)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📸", fontSize = 28.sp)
                                    if (uiState.permisosUbicacion) {
                                        Text("📍", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Indicador de GPS
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.permisosUbicacion) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (uiState.permisosUbicacion) "GPS" else "Sin GPS",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Contador de fotos
                        Box(
                            Modifier.align(Alignment.TopStart).padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE65100))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${uiState.fotos.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Sin permisos de cámara
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📷", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Permisos requeridos", color = Color(0xFF757575), fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text("Cámara y ubicación", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { permissionLauncher.launch(permisos) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                            ) {
                                Text("Conceder permisos")
                            }
                        }
                    }
                }
            }

            // Grid de fotos capturadas
            if (uiState.fotos.isNotEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📸 Fotos capturadas",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFBF360C)
                            )
                            val conUbicacion = uiState.fotos.count { it.tieneUbicacion }
                            Text(
                                "$conUbicacion/${uiState.fotos.size} con GPS",
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.height(100.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.fotos, key = { it.id }) { foto ->
                                FotoMiniaturaConUbicacion(
                                    foto = foto,
                                    onEliminar = { viewModel.eliminarFoto(foto) }
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje de ayuda
            Text(
                if (uiState.permisosUbicacion)
                    "📸📍 Cada foto se guardará con su ubicación GPS automáticamente"
                else
                    "📸 Activa el GPS para guardar fotos con ubicación",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Miniatura de foto con indicador de ubicación GPS.
 */
@Composable
private fun FotoMiniaturaConUbicacion(
    foto: PlagaFoto,
    onEliminar: () -> Unit
) {
    Box(
        Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = foto.fotoUri,
            contentDescription = "Foto",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Indicador de ubicación
        if (foto.tieneUbicacion) {
            Box(
                Modifier.align(Alignment.BottomStart).padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF4CAF50).copy(0.9f))
                    .padding(2.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        
        // Botón eliminar
        Box(
            Modifier.align(Alignment.TopEnd).padding(2.dp)
                .size(18.dp).clip(CircleShape)
                .background(Color(0xFFE53935).copy(0.9f))
                .clickable(onClick = onEliminar),
            Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
