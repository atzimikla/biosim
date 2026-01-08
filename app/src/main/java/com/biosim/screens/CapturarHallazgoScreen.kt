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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.biosim.data.entity.TipoHallazgo
import com.biosim.model.Cultivo
import com.biosim.viewmodel.HallazgoViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturarHallazgoScreen(
    viewModel: HallazgoViewModel = viewModel(),
    onHallazgoGuardado: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.capturarState.collectAsState()
    val cultivos by viewModel.cultivosDisponibles.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Permisos
    val permisosCamara = remember {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.actualizarPermisos(cameraGranted, locationGranted)
    }

    // Verificar permisos al iniciar
    LaunchedEffect(Unit) {
        val cameraGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!cameraGranted || !locationGranted) {
            permissionLauncher.launch(permisosCamara)
        } else {
            viewModel.actualizarPermisos(cameraGranted, locationGranted)
        }
    }

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            Toast.makeText(context, "✅ Hallazgo guardado", Toast.LENGTH_SHORT).show()
            onHallazgoGuardado()
            viewModel.resetearFormulario()
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
                title = { Text("📷 Capturar Hallazgo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00796B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFE0F2F1)
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vista previa de cámara o foto capturada
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("📷 Foto del Hallazgo", fontWeight = FontWeight.SemiBold, color = Color(0xFF00695C))
                    Spacer(Modifier.height(12.dp))

                    if (uiState.fotoUri != null) {
                        // Mostrar foto capturada
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = uiState.fotoUri,
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Botón para tomar otra foto
                            Box(
                                Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                    .clip(CircleShape).background(Color(0xFF00796B)).clickable {
                                        viewModel.resetearFormulario()
                                    }.padding(12.dp)
                            ) {
                                Text("🔄", fontSize = 16.sp)
                            }
                        }
                        
                        // Mostrar ubicación si ya se obtuvo
                        if (uiState.latitud != null && uiState.longitud != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Ubicación capturada", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                                    Text("%.6f, %.6f".format(uiState.latitud, uiState.longitud), fontSize = 11.sp, color = Color(0xFF757575))
                                }
                            }
                        } else if (uiState.obteniendoUbicacion) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00796B), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Obteniendo ubicación...", fontSize = 12.sp, color = Color(0xFF757575))
                            }
                        }
                    } else if (uiState.permisosCamara) {
                        // Vista previa de cámara
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(12.dp)).background(Color.Black)
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

                            // Botón de captura
                            Box(
                                Modifier.align(Alignment.BottomCenter).padding(16.dp)
                                    .size(72.dp).clip(CircleShape)
                                    .background(Color.White.copy(0.9f))
                                    .border(4.dp, Color(0xFF00796B), CircleShape)
                                    .clickable(enabled = !uiState.capturandoFoto) {
                                        imageCapture?.let { capture ->
                                            viewModel.capturarFoto(
                                                imageCapture = capture,
                                                executor = cameraExecutor,
                                                onSuccess = { },
                                                onError = { error ->
                                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    },
                                Alignment.Center
                            ) {
                                if (uiState.capturandoFoto) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color(0xFF00796B))
                                } else {
                                    Text("📸", fontSize = 28.sp)
                                }
                            }
                        }
                    } else {
                        // Sin permisos
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5)),
                            Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📷", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Permiso de cámara requerido", color = Color(0xFF757575))
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(permisosCamara) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                                ) {
                                    Text("Conceder permisos")
                                }
                            }
                        }
                    }

                    uiState.errorFoto?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, fontSize = 12.sp, color = Color(0xFFE53935))
                    }
                }
            }

            // Cultivo
            FormSectionHallazgo("🌱 Cultivo") {
                CultivoSelectorHallazgo(cultivos, uiState.cultivoSeleccionadoId, { viewModel.seleccionarCultivo(it) }, uiState.errorCultivo)
            }

            // Tipo de hallazgo
            FormSectionHallazgo("🏷️ Tipo de Hallazgo") {
                TipoHallazgoSelector(uiState.tipoHallazgo) { viewModel.seleccionarTipoHallazgo(it) }
            }

            // Descripción
            FormSectionHallazgo("📝 Descripción (Opcional)") {
                OutlinedTextField(
                    value = uiState.descripcion,
                    onValueChange = { viewModel.actualizarDescripcion(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("¿Qué observaste?") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00796B),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Guardar
            Button(
                onClick = { viewModel.guardarHallazgo() },
                enabled = !uiState.guardando && uiState.fotoUri != null && uiState.latitud != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Hallazgo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FormSectionHallazgo(titulo: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF00695C))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CultivoSelectorHallazgo(
    cultivos: List<Cultivo>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = cultivos.find { it.id == selectedId }

    Column {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (error != null) Color(0xFFE53935) else Color(0xFFBDBDBD), RoundedCornerShape(12.dp))
                .clickable { expanded = true }.padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (selected != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selected.emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(selected.nombre, fontSize = 16.sp)
                    }
                } else {
                    Text("Selecciona un cultivo", color = Color(0xFF9E9E9E))
                }
                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF757575))
            }
            DropdownMenu(expanded, { expanded = false }, Modifier.fillMaxWidth(0.9f)) {
                cultivos.forEach { cultivo ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cultivo.emoji, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(cultivo.nombre)
                            }
                        },
                        onClick = { onSelect(cultivo.id); expanded = false }
                    )
                }
            }
        }
        error?.let { Text(it, fontSize = 12.sp, color = Color(0xFFE53935)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TipoHallazgoSelector(selected: TipoHallazgo, onSelect: (TipoHallazgo) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TipoHallazgo.entries.forEach { tipo ->
            FilterChip(
                selected = tipo == selected,
                onClick = { onSelect(tipo) },
                label = { Text("${tipo.emoji} ${tipo.label}", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(tipo.color),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

