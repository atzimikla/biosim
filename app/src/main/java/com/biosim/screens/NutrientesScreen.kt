package com.biosim.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.data.entity.MetodoAplicacion
import com.biosim.data.entity.TipoNutriente
import com.biosim.model.NutrienteAplicacion
import com.biosim.model.NutrientesUiState
import com.biosim.ui.theme.BiosimTheme
import com.biosim.viewmodel.NutrienteViewModel

/**
 * Pantalla de lista de aplicaciones de nutrientes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutrientesScreen(
    viewModel: NutrienteViewModel = viewModel(),
    onAgregarNutriente: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.nutrientesUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🧪 Nutrientes",
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
                    containerColor = Color(0xFF7B1FA2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarNutriente,
                containerColor = Color(0xFF7B1FA2),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar aplicación"
                )
            }
        },
        containerColor = Color(0xFFF3E5F5) // Fondo morado muy claro
    ) { paddingValues ->

        when (val state = uiState) {
            is NutrientesUiState.Loading -> {
                NutrientesLoadingContent(paddingValues)
            }
            
            is NutrientesUiState.Empty -> {
                NutrientesEmptyContent(paddingValues, onAgregarNutriente)
            }
            
            is NutrientesUiState.Error -> {
                NutrientesErrorContent(paddingValues, state.mensaje)
            }
            
            is NutrientesUiState.Success -> {
                NutrientesListContent(
                    paddingValues = paddingValues,
                    aplicaciones = state.aplicaciones,
                    onEliminar = { viewModel.eliminarAplicacion(it) }
                )
            }
        }
    }
}

@Composable
private fun NutrientesLoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF7B1FA2))
    }
}

@Composable
private fun NutrientesEmptyContent(
    paddingValues: PaddingValues,
    onAgregarNutriente: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🧪", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay aplicaciones de nutrientes",
                fontSize = 18.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = "Toca + para registrar la primera",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun NutrientesErrorContent(
    paddingValues: PaddingValues,
    mensaje: String
) {
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
                text = mensaje,
                fontSize = 16.sp,
                color = Color(0xFFE53935)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutrientesListContent(
    paddingValues: PaddingValues,
    aplicaciones: List<NutrienteAplicacion>,
    onEliminar: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con estadísticas
        item {
            NutrientesEstadisticasHeader(aplicaciones = aplicaciones)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(
            items = aplicaciones,
            key = { it.id }
        ) { aplicacion ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onEliminar(aplicacion.id)
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                            else -> Color.Transparent
                        },
                        label = "swipe_color"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.White
                        )
                    }
                },
                enableDismissFromStartToEnd = false
            ) {
                NutrienteCard(aplicacion = aplicacion)
            }
        }

        // Espacio para el FAB
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

/**
 * Header con estadísticas resumidas.
 */
@Composable
private fun NutrientesEstadisticasHeader(aplicaciones: List<NutrienteAplicacion>) {
    val totalGramos = aplicaciones.sumOf { it.cantidadGramos }
    val totalAplicaciones = aplicaciones.size
    val tipoMasUsado = aplicaciones
        .groupingBy { it.tipoNutriente }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7B1FA2)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NutrienteEstadisticaItem(
                valor = if (totalGramos >= 1000) "%.1f kg".format(totalGramos / 1000f) else "$totalGramos g",
                etiqueta = "Total aplicado"
            )
            NutrienteEstadisticaItem(
                valor = totalAplicaciones.toString(),
                etiqueta = "Aplicaciones"
            )
            NutrienteEstadisticaItem(
                valor = tipoMasUsado?.emoji ?: "-",
                etiqueta = "Más usado"
            )
        }
    }
}

@Composable
private fun NutrienteEstadisticaItem(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = etiqueta,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

/**
 * Card para mostrar una aplicación de nutriente.
 */
@Composable
fun NutrienteCard(aplicacion: NutrienteAplicacion) {
    Card(
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
            // Icono del tipo de nutriente
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF3E5F5),
                                Color(0xFFE1BEE7)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aplicacion.tipoNutriente.emoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                // Tipo de nutriente
                Text(
                    text = aplicacion.tipoNutriente.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )

                // Cultivo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = aplicacion.cultivoEmoji,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = aplicacion.cultivoNombre,
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }

                // Fecha y método
                Text(
                    text = "📅 ${aplicacion.fechaFormateada} • ${aplicacion.metodoAplicacion.emoji} ${aplicacion.metodoAplicacion.label}",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )

                // Comentario si existe
                aplicacion.comentario?.let { comentario ->
                    Text(
                        text = "📝 $comentario",
                        fontSize = 11.sp,
                        color = Color(0xFFBDBDBD),
                        maxLines = 1
                    )
                }
            }

            // Cantidad
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = aplicacion.cantidadFormateada,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NutrienteCardPreview() {
    BiosimTheme {
        NutrienteCard(
            aplicacion = NutrienteAplicacion(
                id = 1,
                cultivoId = 1,
                cultivoNombre = "Tomates Cherry",
                cultivoEmoji = "🍅",
                fecha = System.currentTimeMillis(),
                tipoNutriente = TipoNutriente.NPK,
                cantidadGramos = 50,
                metodoAplicacion = MetodoAplicacion.FERTIRRIEGO,
                comentario = "Fertilización semanal"
            )
        )
    }
}

