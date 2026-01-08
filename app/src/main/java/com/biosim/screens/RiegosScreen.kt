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
import com.biosim.data.entity.MetodoRiego
import com.biosim.model.Riego
import com.biosim.model.RiegosUiState
import com.biosim.ui.theme.BiosimTheme
import com.biosim.viewmodel.RiegoViewModel

/**
 * Pantalla de lista de riegos.
 * 
 * ¿Por qué recibir callbacks en lugar de navController?
 * - Desacoplamiento: la pantalla no sabe de navegación.
 * - Testing: más fácil de probar sin dependencias de Navigation.
 * - Reutilización: podrías usar esta pantalla en diferentes contextos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiegosScreen(
    viewModel: RiegoViewModel = viewModel(),
    onAgregarRiego: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.riegosUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "💧 Sistema de Riego",
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
                    containerColor = Color(0xFF0288D1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarRiego,
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registrar riego"
                )
            }
        },
        containerColor = Color(0xFFE3F2FD) // Fondo azul muy claro
    ) { paddingValues ->

        when (val state = uiState) {
            is RiegosUiState.Loading -> {
                LoadingContent(paddingValues)
            }
            
            is RiegosUiState.Empty -> {
                EmptyContent(paddingValues, onAgregarRiego)
            }
            
            is RiegosUiState.Error -> {
                ErrorContent(paddingValues, state.mensaje)
            }
            
            is RiegosUiState.Success -> {
                RiegosListContent(
                    paddingValues = paddingValues,
                    riegos = state.riegos,
                    onEliminar = { viewModel.eliminarRiego(it) }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF0288D1))
    }
}

@Composable
private fun EmptyContent(
    paddingValues: PaddingValues,
    onAgregarRiego: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💧", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay riegos registrados",
                fontSize = 18.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = "Toca + para registrar el primero",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun ErrorContent(
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
private fun RiegosListContent(
    paddingValues: PaddingValues,
    riegos: List<Riego>,
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
            EstadisticasHeader(riegos = riegos)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(
            items = riegos,
            key = { it.id }
        ) { riego ->
            // Swipe to dismiss para eliminar
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onEliminar(riego.id)
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
                RiegoCard(riego = riego)
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
private fun EstadisticasHeader(riegos: List<Riego>) {
    val totalAgua = riegos.sumOf { it.cantidadMl }
    val totalRiegos = riegos.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0288D1)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaItem(
                valor = if (totalAgua >= 1000) "%.1f L".format(totalAgua / 1000f) else "$totalAgua ml",
                etiqueta = "Total agua"
            )
            EstadisticaItem(
                valor = totalRiegos.toString(),
                etiqueta = "Riegos"
            )
            EstadisticaItem(
                valor = riegos.firstOrNull()?.fechaCorta ?: "-",
                etiqueta = "Último riego"
            )
        }
    }
}

@Composable
private fun EstadisticaItem(valor: String, etiqueta: String) {
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
 * Card moderna para mostrar un riego.
 */
@Composable
fun RiegoCard(riego: Riego) {
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
            // Icono del método de riego
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE3F2FD),
                                Color(0xFFBBDEFB)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = riego.metodo.emoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                // Cultivo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = riego.cultivoEmoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = riego.cultivoNombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fecha y método
                Text(
                    text = "📅 ${riego.fechaFormateada}",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )

                // Notas si existen
                riego.notas?.let { notas ->
                    Text(
                        text = "📝 $notas",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E),
                        maxLines = 1
                    )
                }
            }

            // Cantidad y método
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = riego.cantidadFormateada,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0277BD)
                )
                Text(
                    text = riego.metodo.label,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RiegoCardPreview() {
    BiosimTheme {
        RiegoCard(
            riego = Riego(
                id = 1,
                cultivoId = 1,
                cultivoNombre = "Tomates Cherry",
                cultivoEmoji = "🍅",
                fecha = System.currentTimeMillis(),
                cantidadMl = 500,
                metodo = MetodoRiego.GOTEO,
                notas = "Riego matutino"
            )
        )
    }
}

