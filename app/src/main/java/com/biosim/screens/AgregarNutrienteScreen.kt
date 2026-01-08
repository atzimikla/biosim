package com.biosim.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.data.entity.MetodoAplicacion
import com.biosim.data.entity.TipoNutriente
import com.biosim.model.Cultivo
import com.biosim.viewmodel.NutrienteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para agregar una nueva aplicación de nutriente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarNutrienteScreen(
    viewModel: NutrienteViewModel = viewModel(),
    onNutrienteGuardado: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.agregarNutrienteState.collectAsState()
    val cultivosDisponibles by viewModel.cultivosDisponibles.collectAsState()

    // Navegar atrás cuando se guarda exitosamente
    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onNutrienteGuardado()
            viewModel.resetearFormulario()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🧪 Agregar Nutriente",
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
        containerColor = Color(0xFFF3E5F5)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ════════════════════════════════════════════════
            // Selector de Cultivo
            // ════════════════════════════════════════════════
            NutrienteFormSection(titulo = "🌱 Cultivo") {
                NutrienteCultivoSelector(
                    cultivos = cultivosDisponibles,
                    selectedId = uiState.cultivoSeleccionadoId,
                    onCultivoSelected = { viewModel.seleccionarCultivo(it) },
                    error = uiState.errorCultivo
                )
            }

            // ════════════════════════════════════════════════
            // Tipo de Nutriente
            // ════════════════════════════════════════════════
            NutrienteFormSection(titulo = "🧪 Tipo de Nutriente") {
                TipoNutrienteSelector(
                    tipoSeleccionado = uiState.tipoSeleccionado,
                    onTipoSelected = { viewModel.seleccionarTipoNutriente(it) }
                )
            }

            // ════════════════════════════════════════════════
            // Fecha de Aplicación
            // ════════════════════════════════════════════════
            NutrienteFormSection(titulo = "📅 Fecha de Aplicación") {
                NutrienteFechaDisplay(fecha = uiState.fecha)
            }

            // ════════════════════════════════════════════════
            // Cantidad
            // ════════════════════════════════════════════════
            NutrienteFormSection(titulo = "⚖️ Cantidad") {
                NutrienteCantidadInput(
                    cantidad = uiState.cantidadGramos,
                    onCantidadChange = { viewModel.actualizarCantidad(it) },
                    error = uiState.errorCantidad
                )
            }

            // ════════════════════════════════════════════════
            // Método de Aplicación
            // ════════════════════════════════════════════════
            NutrienteFormSection(titulo = "🔧 Método de Aplicación") {
                MetodoAplicacionSelector(
                    metodoSeleccionado = uiState.metodoSeleccionado,
                    onMetodoSelected = { viewModel.seleccionarMetodo(it) }
                )
            }

            // ════════════════════════════════════════════════
            // Comentario (Opcional)
            // ════════════════════════════════════════════════
            NutrienteFormSection(titulo = "📝 Comentario (Opcional)") {
                OutlinedTextField(
                    value = uiState.comentario,
                    onValueChange = { viewModel.actualizarComentario(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Observaciones de la aplicación...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7B1FA2),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════
            // Botón Guardar
            // ════════════════════════════════════════════════
            Button(
                onClick = { viewModel.guardarAplicacion() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B1FA2)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Aplicación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Sección del formulario con título.
 */
@Composable
private fun NutrienteFormSection(
    titulo: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Selector de cultivo con dropdown.
 */
@Composable
private fun NutrienteCultivoSelector(
    cultivos: List<Cultivo>,
    selectedId: Int?,
    onCultivoSelected: (Int) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCultivo = cultivos.find { it.id == selectedId }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (error != null) Color(0xFFE53935) else Color(0xFFBDBDBD),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { expanded = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedCultivo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedCultivo.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedCultivo.nombre,
                            fontSize = 16.sp,
                            color = Color(0xFF212121)
                        )
                    }
                } else {
                    Text(
                        text = "Selecciona un cultivo",
                        fontSize = 16.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF757575)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                cultivos.forEach { cultivo ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = cultivo.emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = cultivo.nombre,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = cultivo.ubicacion,
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onCultivoSelected(cultivo.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color(0xFFE53935)
            )
        }
    }
}

/**
 * Selector de tipo de nutriente.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TipoNutrienteSelector(
    tipoSeleccionado: TipoNutriente,
    onTipoSelected: (TipoNutriente) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipoNutriente.entries.forEach { tipo ->
            FilterChip(
                selected = tipo == tipoSeleccionado,
                onClick = { onTipoSelected(tipo) },
                label = {
                    Column {
                        Text("${tipo.emoji} ${tipo.label}", fontSize = 12.sp)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7B1FA2),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
    
    // Descripción del nutriente seleccionado
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF3E5F5))
            .padding(12.dp)
    ) {
        Text(
            text = "ℹ️ ${tipoSeleccionado.descripcion}",
            fontSize = 12.sp,
            color = Color(0xFF6A1B9A)
        )
    }
}

/**
 * Display de fecha.
 */
@Composable
private fun NutrienteFechaDisplay(fecha: Long) {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("es", "ES"))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3E5F5))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sdf.format(Date(fecha)),
                fontSize = 14.sp,
                color = Color(0xFF6A1B9A)
            )
            Text(
                text = "📅",
                fontSize = 20.sp
            )
        }
    }
}

/**
 * Input de cantidad en gramos.
 */
@Composable
private fun NutrienteCantidadInput(
    cantidad: String,
    onCantidadChange: (String) -> Unit,
    error: String?
) {
    Column {
        OutlinedTextField(
            value = cantidad,
            onValueChange = { 
                if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                    onCantidadChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: 50") },
            suffix = { Text("gramos", color = Color(0xFF757575)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7B1FA2),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                errorBorderColor = Color(0xFFE53935)
            )
        )

        // Sugerencias rápidas
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("25", "50", "100", "250").forEach { sugerencia ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3E5F5))
                        .clickable { onCantidadChange(sugerencia) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${sugerencia}g",
                        fontSize = 12.sp,
                        color = Color(0xFF6A1B9A)
                    )
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color(0xFFE53935)
            )
        }
    }
}

/**
 * Selector de método de aplicación.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetodoAplicacionSelector(
    metodoSeleccionado: MetodoAplicacion,
    onMetodoSelected: (MetodoAplicacion) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetodoAplicacion.entries.forEach { metodo ->
            FilterChip(
                selected = metodo == metodoSeleccionado,
                onClick = { onMetodoSelected(metodo) },
                label = {
                    Text("${metodo.emoji} ${metodo.label}")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF7B1FA2),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

