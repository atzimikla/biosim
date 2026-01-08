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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.biosim.model.Hallazgo
import com.biosim.model.HallazgosUiState
import com.biosim.viewmodel.HallazgoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallazgosScreen(
    viewModel: HallazgoViewModel = viewModel(),
    onCapturarHallazgo: () -> Unit = {},
    onVerDetalle: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.hallazgosUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📍 Hallazgos del Cultivo", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCapturarHallazgo,
                containerColor = Color(0xFF00796B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Nuevo hallazgo")
            }
        },
        containerColor = Color(0xFFE0F2F1)
    ) { padding ->
        when (val state = uiState) {
            is HallazgosUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00796B))
                }
            }
            is HallazgosUiState.Empty -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("No hay hallazgos registrados", fontSize = 18.sp, color = Color(0xFF757575))
                        Text("Toca + para capturar el primero", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                    }
                }
            }
            is HallazgosUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("❌ ${state.mensaje}", color = Color(0xFFE53935))
                }
            }
            is HallazgosUiState.Success -> {
                HallazgosListContent(
                    padding = padding,
                    hallazgos = state.hallazgos,
                    onVerDetalle = onVerDetalle,
                    onEliminar = { viewModel.eliminarHallazgo(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HallazgosListContent(
    padding: PaddingValues,
    hallazgos: List<Hallazgo>,
    onVerDetalle: (Int) -> Unit,
    onEliminar: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats
        item {
            HallazgosStatsHeader(hallazgos)
            Spacer(Modifier.height(8.dp))
        }

        items(hallazgos, key = { it.id }) { hallazgo ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onEliminar(hallazgo.id)
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
                HallazgoCard(
                    hallazgo = hallazgo,
                    onClick = { onVerDetalle(hallazgo.id) }
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun HallazgosStatsHeader(hallazgos: List<Hallazgo>) {
    val problemas = hallazgos.count { it.tipoHallazgo.name == "PROBLEMA" }
    val observaciones = hallazgos.count { it.tipoHallazgo.name == "OBSERVACION" }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00796B))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(hallazgos.size.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Total", fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(observaciones.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Observaciones", fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(problemas.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCDD2))
                Text("Problemas", fontSize = 12.sp, color = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
fun HallazgoCard(
    hallazgo: Hallazgo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniatura de la foto
            AsyncImage(
                model = hallazgo.fotoUri,
                contentDescription = "Foto del hallazgo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E0E0)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Tipo de hallazgo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Color(hallazgo.tipoHallazgo.color).copy(0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${hallazgo.tipoHallazgo.emoji} ${hallazgo.tipoHallazgo.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(hallazgo.tipoHallazgo.color)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Cultivo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(hallazgo.cultivoEmoji, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(hallazgo.cultivoNombre, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                // Descripción
                hallazgo.descripcion?.let {
                    Text(
                        it,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Fecha y ubicación
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📅 ${hallazgo.fechaCorta}", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                    Text(
                        hallazgo.coordenadasFormateadas,
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

