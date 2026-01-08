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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.biosim.model.FotoMapaUiState
import com.biosim.model.PlagaFoto
import com.biosim.viewmodel.PlagaFotoViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Pantalla para mostrar la ubicación de una foto de plaga en Google Maps.
 * 
 * PUNTO CRÍTICO - Renderizado del mapa con Compose:
 * 
 * Se usa la librería google-maps-compose que proporciona Composables nativos:
 * - GoogleMap: Contenedor principal del mapa
 * - rememberCameraPositionState: Estado de la cámara (posición, zoom)
 * - Marker + MarkerState: Marcador en la posición de la foto
 * 
 * El mapa se centra automáticamente en las coordenadas de la foto con zoom 17
 * para mostrar suficiente detalle del área.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotoMapaScreen(
    fotoId: Int,
    viewModel: PlagaFotoViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.fotoMapaState.collectAsState()

    LaunchedEffect(fotoId) {
        viewModel.cargarFotoParaMapa(fotoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📍 Ubicación de la Foto", fontWeight = FontWeight.Bold) },
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
        when (val state = uiState) {
            is FotoMapaUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE65100))
                }
            }
            is FotoMapaUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("❌ ${state.mensaje}", color = Color(0xFFE53935))
                    }
                }
            }
            is FotoMapaUiState.Success -> {
                FotoMapaContent(
                    padding = padding,
                    foto = state.foto
                )
            }
        }
    }
}

@Composable
private fun FotoMapaContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    foto: PlagaFoto
) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Miniatura de la foto
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = foto.fotoUri,
                    contentDescription = "Foto de plaga",
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Text("📷 Foto de Inspección", fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
                    Spacer(Modifier.height(4.dp))
                    Text("📅 ${foto.fechaFormateada}", fontSize = 12.sp, color = Color(0xFF757575))
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Coordenadas", fontSize = 10.sp, color = Color(0xFF4CAF50))
                            Text(
                                foto.coordenadasFormateadas,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        // Mapa con marcador
        Card(
            Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ubicación en el Mapa",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFBF360C)
                    )
                }
                
                Spacer(Modifier.height(12.dp))

                /**
                 * PUNTO CRÍTICO - Google Maps Compose:
                 * 
                 * 1. LatLng: Clase de Google Maps para coordenadas
                 * 2. rememberCameraPositionState: Mantiene el estado de la cámara
                 * 3. CameraPosition.fromLatLngZoom: Centra el mapa en las coordenadas
                 * 4. GoogleMap: Composable que renderiza el mapa
                 * 5. Marker: Marcador en la ubicación de la foto
                 */
                val position = LatLng(foto.latitud!!, foto.longitud!!)
                val cameraPositionState = rememberCameraPositionState {
                    this.position = CameraPosition.fromLatLngZoom(position, 17f)
                }

                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = position),
                            title = "📷 Foto de Plaga",
                            snippet = "Capturada: ${foto.fechaFormateada}"
                        )
                    }
                }
            }
        }

        // Info adicional
        Text(
            "Lat: ${foto.latitud}  •  Lng: ${foto.longitud}",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

