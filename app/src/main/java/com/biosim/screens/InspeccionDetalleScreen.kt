package com.biosim.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.biosim.data.entity.Efectividad
import com.biosim.model.InspeccionDetalleUiState
import com.biosim.model.PlagaFoto
import com.biosim.model.PlagaInspeccion
import com.biosim.model.PlagaTratamiento
import com.biosim.viewmodel.PlagaFotoViewModel
import com.biosim.viewmodel.PlagaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspeccionDetalleScreen(
    inspeccionId: Int,
    viewModel: PlagaViewModel = viewModel(),
    fotoViewModel: PlagaFotoViewModel = viewModel(),
    onAgregarTratamiento: (Int) -> Unit = {},
    onAgregarFotos: (Int) -> Unit = {},
    onVerFotoEnMapa: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.inspeccionDetalleUiState.collectAsState()
    val tratamientos by viewModel.tratamientosInspeccion.collectAsState()
    
    /**
     * CORRECCIÓN DEL BUG:
     * - Se usa el StateFlow `fotos` en lugar del reasignable `fotosFlow`
     * - El ViewModel ahora mantiene un MutableStateFlow fijo que se actualiza
     * - Esto garantiza que la UI siempre esté suscrita al mismo Flow
     */
    val fotos by fotoViewModel.fotos.collectAsState()

    /**
     * LaunchedEffect con key = inspeccionId:
     * - Se ejecuta cuando la composición se crea
     * - Se re-ejecuta si cambia el inspeccionId
     * - Carga la inspección y las fotos inmediatamente
     */
    LaunchedEffect(inspeccionId) {
        viewModel.cargarInspeccion(inspeccionId)
        fotoViewModel.cargarFotos(inspeccionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Detalle Inspección", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            if (uiState is InspeccionDetalleUiState.Success) {
                FloatingActionButton(
                    onClick = { onAgregarTratamiento(inspeccionId) },
                    containerColor = Color(0xFF0288D1),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Agregar tratamiento")
                }
            }
        },
        containerColor = Color(0xFFFFF3E0)
    ) { padding ->
        when (val state = uiState) {
            is InspeccionDetalleUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE65100))
                }
            }
            is InspeccionDetalleUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("❌ ${state.mensaje}", color = Color(0xFFE53935))
                }
            }
            is InspeccionDetalleUiState.Success -> {
                InspeccionDetalleContent(
                    padding = padding,
                    inspeccion = state.inspeccion,
                    tratamientos = tratamientos,
                    fotos = fotos,
                    onMarcarResuelta = { viewModel.marcarInspeccionResuelta(inspeccionId, !state.inspeccion.resuelta) },
                    onEliminarTratamiento = { viewModel.eliminarTratamiento(it) },
                    onActualizarEfectividad = { id, ef -> viewModel.actualizarEfectividadTratamiento(id, ef) },
                    onAgregarFotos = { onAgregarFotos(inspeccionId) },
                    onEliminarFoto = { fotoViewModel.eliminarFoto(it) },
                    onVerFotoEnMapa = onVerFotoEnMapa
                )
            }
        }
    }
}

@Composable
private fun InspeccionDetalleContent(
    padding: PaddingValues,
    inspeccion: PlagaInspeccion,
    tratamientos: List<PlagaTratamiento>,
    fotos: List<PlagaFoto>,
    onMarcarResuelta: () -> Unit,
    onEliminarTratamiento: (Int) -> Unit,
    onActualizarEfectividad: (Int, Efectividad) -> Unit,
    onAgregarFotos: () -> Unit,
    onEliminarFoto: (PlagaFoto) -> Unit,
    onVerFotoEnMapa: (Int) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con info de la plaga
        item {
            InspeccionHeader(inspeccion)
        }

        // Info detallada
        item {
            InspeccionInfoCard(inspeccion)
        }

        // ══════════════════════════════════════════════════════════════
        // SECCIÓN DE FOTOS CON UBICACIÓN
        // ══════════════════════════════════════════════════════════════
        item {
            FotosInspeccionSection(
                fotos = fotos,
                onAgregarFotos = onAgregarFotos,
                onEliminarFoto = onEliminarFoto,
                onVerEnMapa = onVerFotoEnMapa
            )
        }

        // Botón resolver/reabrir
        item {
            Button(
                onClick = onMarcarResuelta,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (inspeccion.resuelta) Color(0xFFE65100) else Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    if (inspeccion.resuelta) Icons.Default.Add else Icons.Default.CheckCircle,
                    null,
                    Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (inspeccion.resuelta) "Reabrir Inspección" else "Marcar como Resuelta")
            }
        }

        // Tratamientos
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "💊 Tratamientos (${tratamientos.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD)
                )
            }
        }

        if (tratamientos.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💊", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Sin tratamientos aplicados", color = Color(0xFF757575))
                        Text("Toca + para agregar uno", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    }
                }
            }
        } else {
            items(tratamientos, key = { it.id }) { tratamiento ->
                TratamientoCard(
                    tratamiento = tratamiento,
                    onEliminar = { onEliminarTratamiento(tratamiento.id) },
                    onActualizarEfectividad = { onActualizarEfectividad(tratamiento.id, it) }
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

/**
 * Sección de fotos de la inspección con geolocalización.
 * Muestra un carrusel horizontal de fotos con indicadores de GPS.
 */
@Composable
private fun FotosInspeccionSection(
    fotos: List<PlagaFoto>,
    onAgregarFotos: () -> Unit,
    onEliminarFoto: (PlagaFoto) -> Unit,
    onVerEnMapa: (Int) -> Unit
) {
    val fotosConUbicacion = fotos.count { it.tieneUbicacion }
    
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "📷 Fotos (${fotos.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBF360C)
                    )
                    if (fotosConUbicacion > 0) {
                        Text(
                            "📍 $fotosConUbicacion con GPS",
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                OutlinedButton(
                    onClick = onAgregarFotos,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("📷 GPS", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (fotos.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .clickable(onClick = onAgregarFotos),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷📍", fontSize = 32.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Toca para agregar fotos con GPS", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fotos, key = { it.id }) { foto ->
                        FotoThumbnailConMapa(
                            foto = foto,
                            onEliminar = { onEliminarFoto(foto) },
                            onVerEnMapa = { onVerEnMapa(foto.id) }
                        )
                    }
                    // Botón para agregar más
                    item {
                        Box(
                            Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0))
                                .clickable(onClick = onAgregarFotos),
                            Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Add,
                                    null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("📷📍", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Miniatura de foto con indicador de GPS y botón "Ver en mapa".
 */
@Composable
private fun FotoThumbnailConMapa(
    foto: PlagaFoto,
    onEliminar: () -> Unit,
    onVerEnMapa: () -> Unit
) {
    Box(
        Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = foto.fotoUri,
            contentDescription = "Foto de plaga",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Botón eliminar
        Box(
            Modifier.align(Alignment.TopEnd).padding(4.dp)
                .size(24.dp).clip(CircleShape)
                .background(Color(0xFFE53935).copy(0.9f))
                .clickable(onClick = onEliminar),
            Alignment.Center
        ) {
            Icon(
                Icons.Default.Delete,
                null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        // Indicador de GPS + Botón "Ver en mapa"
        if (foto.tieneUbicacion) {
            Box(
                Modifier.align(Alignment.TopStart).padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF4CAF50).copy(0.9f))
                    .clickable(onClick = onVerEnMapa)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("Mapa", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Fecha y estado de ubicación
        Box(
            Modifier.align(Alignment.BottomStart).fillMaxWidth()
                .background(Color.Black.copy(0.6f))
                .padding(4.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    foto.fechaFormateada.takeLast(5),
                    fontSize = 9.sp,
                    color = Color.White
                )
                if (!foto.tieneUbicacion) {
                    Text("Sin GPS", fontSize = 8.sp, color = Color(0xFFFFCC80))
                }
            }
        }
    }
}

@Composable
private fun InspeccionHeader(inspeccion: PlagaInspeccion) {
    Box(
        Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(
            Brush.verticalGradient(
                listOf(
                    Color(0xFFE65100),
                    Color(0xFFFF8A65)
                )
            )
        ),
        Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                Alignment.Center
            ) {
                Text(inspeccion.tipoPlaga.emoji, fontSize = 40.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(inspeccion.nombrePlaga, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(inspeccion.cultivoEmoji, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(inspeccion.cultivoNombre, fontSize = 14.sp, color = Color.White.copy(0.9f))
            }
        }

        // Badge estado
        Box(
            Modifier.align(Alignment.TopEnd).padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (inspeccion.resuelta) Color(0xFF4CAF50) else Color(inspeccion.nivelIncidencia.color))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                if (inspeccion.resuelta) "✅ Resuelta" else "${inspeccion.nivelIncidencia.emoji} ${inspeccion.nivelIncidencia.label}",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InspeccionInfoCard(inspeccion: PlagaInspeccion) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("📋 Información", fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
            Spacer(Modifier.height(12.dp))

            InfoRow("📅 Fecha", inspeccion.fechaFormateada)
            InfoRow("${inspeccion.tipoPlaga.emoji} Tipo", inspeccion.tipoPlaga.label)
            InfoRow("${inspeccion.parteAfectada.emoji} Parte afectada", inspeccion.parteAfectada.label)
            InfoRow("${inspeccion.nivelIncidencia.emoji} Nivel", inspeccion.nivelIncidencia.label)

            inspeccion.observaciones?.let {
                Spacer(Modifier.height(8.dp))
                Text("📝 Observaciones:", fontSize = 12.sp, color = Color(0xFF757575))
                Text(it, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF757575))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TratamientoCard(
    tratamiento: PlagaTratamiento,
    onEliminar: () -> Unit,
    onActualizarEfectividad: (Efectividad) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)),
                        Alignment.Center
                    ) {
                        Text(tratamiento.tipoProducto.emoji, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(tratamiento.producto, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                        Text(tratamiento.tipoProducto.label, fontSize = 12.sp, color = Color(0xFF757575))
                    }
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("💧 Dosis", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Text(tratamiento.dosisFormateada, fontSize = 14.sp)
                }
                Column {
                    Text("${tratamiento.metodoAplicacion.emoji} Método", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Text(tratamiento.metodoAplicacion.label, fontSize = 14.sp)
                }
                Column {
                    Text("📅 Fecha", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Text(tratamiento.fechaFormateada.take(10), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Efectividad
            Text("Efectividad:", fontSize = 12.sp, color = Color(0xFF757575))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Efectividad.entries.forEach { ef ->
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(if (tratamiento.efectividad == ef) Color(ef.color) else Color(ef.color).copy(0.1f))
                            .clickable { onActualizarEfectividad(ef) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            ef.emoji,
                            fontSize = 14.sp,
                            color = if (tratamiento.efectividad == ef) Color.White else Color(ef.color)
                        )
                    }
                }
            }

            tratamiento.observaciones?.let {
                Spacer(Modifier.height(8.dp))
                Text("📝 $it", fontSize = 12.sp, color = Color(0xFF9E9E9E))
            }
        }
    }
}
