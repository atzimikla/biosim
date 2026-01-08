package com.biosim.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.biosim.model.Hallazgo
import com.biosim.model.HallazgoDetalleUiState
import com.biosim.viewmodel.HallazgoViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallazgoDetalleScreen(
    hallazgoId: Int,
    viewModel: HallazgoViewModel = viewModel(),
    onEliminar: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.hallazgoDetalleState.collectAsState()

    LaunchedEffect(hallazgoId) {
        viewModel.cargarHallazgo(hallazgoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📍 Detalle del Hallazgo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (uiState is HallazgoDetalleUiState.Success) {
                        IconButton(onClick = {
                            viewModel.eliminarHallazgo(hallazgoId)
                            onEliminar()
                        }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color.White)
                        }
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
        when (val state = uiState) {
            is HallazgoDetalleUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00796B))
                }
            }
            is HallazgoDetalleUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("❌ ${state.mensaje}", color = Color(0xFFE53935))
                }
            }
            is HallazgoDetalleUiState.Success -> {
                HallazgoDetalleContent(
                    padding = padding,
                    hallazgo = state.hallazgo
                )
            }
        }
    }
}

@Composable
private fun HallazgoDetalleContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    hallazgo: Hallazgo
) {
    Column(
        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Foto
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            AsyncImage(
                model = hallazgo.fotoUri,
                contentDescription = "Foto del hallazgo",
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                contentScale = ContentScale.Crop
            )
        }

        // Info del hallazgo
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                // Tipo de hallazgo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape)
                            .background(Color(hallazgo.tipoHallazgo.color).copy(0.15f)),
                        Alignment.Center
                    ) {
                        Text(hallazgo.tipoHallazgo.emoji, fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(hallazgo.tipoHallazgo.label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(hallazgo.tipoHallazgo.color))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(hallazgo.cultivoEmoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(hallazgo.cultivoNombre, fontSize = 14.sp, color = Color(0xFF757575))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Fecha
                InfoRowHallazgo("📅 Fecha", hallazgo.fechaFormateada)

                // Coordenadas
                InfoRowHallazgo("📍 Coordenadas", hallazgo.coordenadasFormateadas)

                // Descripción
                hallazgo.descripcion?.let {
                    Spacer(Modifier.height(12.dp))
                    Text("📝 Descripción", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(it, fontSize = 14.sp)
                }
            }
        }

        // Mapa
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF00796B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ubicación en el Mapa", fontWeight = FontWeight.SemiBold, color = Color(0xFF00695C))
                }
                Spacer(Modifier.height(12.dp))

                val position = LatLng(hallazgo.latitud, hallazgo.longitud)
                val cameraPositionState = rememberCameraPositionState {
                    this.position = CameraPosition.fromLatLngZoom(position, 17f)
                }

                Box(
                    Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = position),
                            title = hallazgo.tipoHallazgo.label,
                            snippet = "${hallazgo.cultivoEmoji} ${hallazgo.cultivoNombre}"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Lat: ${hallazgo.latitud}, Lng: ${hallazgo.longitud}",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRowHallazgo(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF757575))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

