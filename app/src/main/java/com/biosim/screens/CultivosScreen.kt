package com.biosim.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.model.Cultivo
import com.biosim.model.EstadoCultivo
import com.biosim.ui.theme.BiosimTheme
import com.biosim.viewmodel.CultivosViewModel

/**
 * Pantalla que muestra la lista de cultivos.
 *
 * @param viewModel ViewModel que maneja el estado de los cultivos
 * @param onNavigateToDetalle Lambda para navegar al detalle de un cultivo
 * @param onBack Lambda para volver atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivosScreen(
    viewModel: CultivosViewModel = viewModel(),
    onNavigateToDetalle: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val cultivos by viewModel.cultivos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌿 Mis Cultivos",
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
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Agregar nuevo cultivo */ },
                containerColor = Color(0xFF43A047),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar cultivo"
                )
            }
        },
        containerColor = Color(0xFFF1F8E9)
    ) { paddingValues ->
        
        if (isLoading) {
            // Estado de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF43A047))
            }
        } else if (cultivos.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌱", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes cultivos aún",
                        fontSize = 18.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "Toca + para agregar uno",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        } else {
            // Lista de cultivos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = cultivos,
                    key = { it.id }
                ) { cultivo ->
                    CultivoCard(
                        cultivo = cultivo,
                        onClick = { onNavigateToDetalle(cultivo.id) }
                    )
                }
                
                // Espacio extra al final para el FAB
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

/**
 * Card que muestra la información de un cultivo.
 */
@Composable
fun CultivoCard(
    cultivo: Cultivo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji del cultivo con fondo circular
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9),
                                Color(0xFFC8E6C9)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cultivo.emoji,
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del cultivo
            Column(modifier = Modifier.weight(1f)) {
                // Nombre y estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cultivo.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    
                    // Badge de estado
                    EstadoBadge(estado = cultivo.estado)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Ubicación
                Text(
                    text = "📍 ${cultivo.ubicacion}",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Días y próximo riego
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🗓️ ${cultivo.diasDesdeSimebra} días",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = "💧 ${cultivo.proximoRiego}",
                        fontSize = 12.sp,
                        color = Color(0xFF0288D1)
                    )
                }
            }
        }
    }
}

/**
 * Badge que muestra el estado del cultivo.
 */
@Composable
fun EstadoBadge(estado: EstadoCultivo) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(estado.color).copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(estado.color)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CultivosScreenPreview() {
    BiosimTheme {
        CultivosScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun CultivoCardPreview() {
    BiosimTheme {
        CultivoCard(
            cultivo = Cultivo(
                id = 1,
                nombre = "Tomates Cherry",
                tipo = "Hortaliza",
                emoji = "🍅",
                ubicacion = "Invernadero A",
                fechaSiembra = "15/11/2024",
                estado = EstadoCultivo.PRODUCIENDO,
                diasDesdeSimebra = 45,
                proximoRiego = "Hoy 18:00"
            ),
            onClick = {}
        )
    }
}

