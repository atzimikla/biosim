package com.biosim.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.model.PlagaInspeccion
import com.biosim.model.PlagasUiState
import com.biosim.viewmodel.PlagaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlagasScreen(
    viewModel: PlagaViewModel = viewModel(),
    onAgregarInspeccion: () -> Unit = {},
    onVerDetalle: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.plagasUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐛 Control de Plagas", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = onAgregarInspeccion,
                containerColor = Color(0xFFE65100),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Nueva inspección")
            }
        },
        containerColor = Color(0xFFFFF3E0)
    ) { padding ->
        when (val state = uiState) {
            is PlagasUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE65100))
                }
            }
            is PlagasUiState.Empty -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐛", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("No hay inspecciones registradas", fontSize = 18.sp, color = Color(0xFF757575))
                        Text("Toca + para crear la primera", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                    }
                }
            }
            is PlagasUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("❌ ${state.mensaje}", color = Color(0xFFE53935))
                }
            }
            is PlagasUiState.Success -> {
                PlagasListContent(
                    padding = padding,
                    inspecciones = state.inspecciones,
                    onVerDetalle = onVerDetalle,
                    onEliminar = { viewModel.eliminarInspeccion(it) },
                    onMarcarResuelta = { id, resuelta -> viewModel.marcarInspeccionResuelta(id, resuelta) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlagasListContent(
    padding: PaddingValues,
    inspecciones: List<PlagaInspeccion>,
    onVerDetalle: (Int) -> Unit,
    onEliminar: (Int) -> Unit,
    onMarcarResuelta: (Int, Boolean) -> Unit
) {
    val activas = inspecciones.filter { !it.resuelta }
    val resueltas = inspecciones.filter { it.resuelta }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats
        item {
            PlagasStatsHeader(inspecciones)
            Spacer(Modifier.height(8.dp))
        }

        // Activas
        if (activas.isNotEmpty()) {
            item {
                Text("⚠️ Activas (${activas.size})", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
            items(activas, key = { it.id }) { inspeccion ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onEliminar(inspeccion.id)
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                Color(0xFFE53935) else Color.Transparent, label = ""
                        )
                        Box(
                            Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 20.dp),
                            Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.White)
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    InspeccionCard(
                        inspeccion = inspeccion,
                        onClick = { onVerDetalle(inspeccion.id) },
                        onMarcarResuelta = { onMarcarResuelta(inspeccion.id, true) }
                    )
                }
            }
        }

        // Resueltas
        if (resueltas.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("✅ Resueltas (${resueltas.size})", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }
            items(resueltas, key = { it.id }) { inspeccion ->
                InspeccionCard(
                    inspeccion = inspeccion,
                    onClick = { onVerDetalle(inspeccion.id) },
                    onMarcarResuelta = { onMarcarResuelta(inspeccion.id, false) }
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun PlagasStatsHeader(inspecciones: List<PlagaInspeccion>) {
    val activas = inspecciones.count { !it.resuelta }
    val criticas = inspecciones.count { !it.resuelta && it.nivelIncidencia.name == "CRITICO" }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE65100))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(inspecciones.size.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Total", fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(activas.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Activas", fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(criticas.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCDD2))
                Text("Críticas", fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
fun InspeccionCard(
    inspeccion: PlagaInspeccion,
    onClick: () -> Unit,
    onMarcarResuelta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inspeccion.resuelta) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                Modifier.size(50.dp).clip(CircleShape).background(
                    Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))
                ),
                Alignment.Center
            ) {
                Text(inspeccion.tipoPlaga.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        inspeccion.nombrePlaga,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBF360C),
                        textDecoration = if (inspeccion.resuelta) TextDecoration.LineThrough else null
                    )
                    Spacer(Modifier.width(8.dp))
                    // Badge nivel
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Color(inspeccion.nivelIncidencia.color).copy(0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            inspeccion.nivelIncidencia.emoji,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(inspeccion.cultivoEmoji, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(inspeccion.cultivoNombre, fontSize = 13.sp, color = Color(0xFF757575))
                }
                
                Text(
                    "📅 ${inspeccion.fechaCorta} • ${inspeccion.parteAfectada.emoji} ${inspeccion.parteAfectada.label}",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
                
                if (inspeccion.cantidadTratamientos > 0) {
                    Text(
                        "💊 ${inspeccion.cantidadTratamientos} tratamiento(s)",
                        fontSize = 11.sp,
                        color = Color(0xFF0288D1)
                    )
                }
            }

            // Botón resolver
            if (!inspeccion.resuelta) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(0.1f))
                        .clickable(onClick = onMarcarResuelta),
                    Alignment.Center
                ) {
                    Text("✓", fontSize = 18.sp, color = Color(0xFF4CAF50))
                }
            }
        }
    }
}

