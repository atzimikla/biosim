package com.biosim.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
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
 * Pantalla de detalle de un cultivo.
 * Muestra toda la información del cultivo seleccionado.
 *
 * @param cultivoId ID del cultivo a mostrar
 * @param viewModel ViewModel compartido para obtener datos
 * @param onBack Lambda para volver atrás
 * @param onEdit Lambda para editar el cultivo
 * @param onDelete Lambda para eliminar el cultivo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoDetalleScreen(
    cultivoId: Int,
    viewModel: CultivosViewModel = viewModel(),
    onBack: () -> Unit = {},
    onEdit: (Int) -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    val cultivos by viewModel.cultivos.collectAsState()
    val cultivo = cultivos.find { it.id == cultivoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = cultivo?.nombre ?: "Detalle",
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
                actions = {
                    IconButton(onClick = { cultivo?.let { onEdit(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                    IconButton(onClick = { cultivo?.let { onDelete(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF1F8E9)
    ) { paddingValues ->

        if (cultivo == null) {
            // Cultivo no encontrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "❌", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cultivo no encontrado",
                        fontSize = 18.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else {
            // Contenido del detalle
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header con emoji grande
                DetalleHeader(cultivo = cultivo)

                // Cards de información
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card de estado actual
                    EstadoCard(cultivo = cultivo)

                    // Card de información general
                    InfoGeneralCard(cultivo = cultivo)

                    // Card de riego
                    RiegoCard(cultivo = cultivo)

                    // Botones de acción
                    AccionesRapidas(
                        onRegistrarRiego = { /* TODO */ },
                        onAgregarNota = { /* TODO */ },
                        onVerHistorial = { /* TODO */ }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Header con emoji grande y gradiente.
 */
@Composable
private fun DetalleHeader(cultivo: Cultivo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E7D32),
                        Color(0xFF66BB6A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Emoji grande
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cultivo.emoji,
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tipo de cultivo
            Text(
                text = cultivo.tipo,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Card que muestra el estado actual y progreso.
 */
@Composable
private fun EstadoCard(cultivo: Cultivo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado Actual",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                EstadoBadge(estado = cultivo.estado)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso del ciclo
            Text(
                text = "Progreso del ciclo",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val progreso = (cultivo.diasDesdeSimebra / 90f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(cultivo.estado.color),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Día ${cultivo.diasDesdeSimebra}",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                Text(
                    text = "~90 días",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

/**
 * Card con información general del cultivo.
 */
@Composable
private fun InfoGeneralCard(cultivo: Cultivo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📋 Información General",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(emoji = "📍", label = "Ubicación", value = cultivo.ubicacion)
            InfoRow(emoji = "📅", label = "Fecha de siembra", value = cultivo.fechaSiembra)
            InfoRow(emoji = "🏷️", label = "Tipo", value = cultivo.tipo)
            InfoRow(emoji = "🗓️", label = "Días desde siembra", value = "${cultivo.diasDesdeSimebra} días")
        }
    }
}

/**
 * Fila de información con emoji, label y valor.
 */
@Composable
private fun InfoRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
        }
    }
}

/**
 * Card con información de riego.
 */
@Composable
private fun RiegoCard(cultivo: Cultivo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💧 Próximo Riego",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD)
                )
                Text(
                    text = cultivo.proximoRiego,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF01579B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiegoStat(label = "Frecuencia", value = "Cada 2 días")
                RiegoStat(label = "Cantidad", value = "500 ml")
                RiegoStat(label = "Último", value = "Ayer")
            }
        }
    }
}

/**
 * Estadística de riego individual.
 */
@Composable
private fun RiegoStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0277BD)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF757575)
        )
    }
}

/**
 * Botones de acciones rápidas.
 */
@Composable
private fun AccionesRapidas(
    onRegistrarRiego: () -> Unit,
    onAgregarNota: () -> Unit,
    onVerHistorial: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "⚡ Acciones Rápidas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón principal
            Button(
                onClick = onRegistrarRiego,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF039BE5)
                )
            ) {
                Text(
                    text = "💧 Registrar Riego",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones secundarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAgregarNota,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "📝 Nota",
                        fontSize = 13.sp
                    )
                }
                OutlinedButton(
                    onClick = onVerHistorial,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "📊 Historial",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CultivoDetalleScreenPreview() {
    BiosimTheme {
        // Para preview, usamos un cultivo mock directamente
        CultivoDetallePreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CultivoDetallePreviewContent() {
    val cultivoMock = Cultivo(
        id = 1,
        nombre = "Tomates Cherry",
        tipo = "Hortaliza",
        emoji = "🍅",
        ubicacion = "Invernadero A",
        fechaSiembra = "15/11/2024",
        estado = EstadoCultivo.PRODUCIENDO,
        diasDesdeSimebra = 45,
        proximoRiego = "Hoy 18:00"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cultivoMock.nombre, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF1F8E9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            DetalleHeader(cultivo = cultivoMock)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EstadoCard(cultivo = cultivoMock)
                InfoGeneralCard(cultivo = cultivoMock)
                RiegoCard(cultivo = cultivoMock)
                AccionesRapidas({}, {}, {})
            }
        }
    }
}

