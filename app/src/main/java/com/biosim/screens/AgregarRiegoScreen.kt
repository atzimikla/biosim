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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.data.entity.MetodoRiego
import com.biosim.model.Cultivo
import com.biosim.ui.theme.BiosimTheme
import com.biosim.viewmodel.RiegoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para agregar un nuevo riego.
 * 
 * ¿Por qué usar state hoisting?
 * - El ViewModel maneja el estado (single source of truth).
 * - La UI solo muestra datos y envía eventos.
 * - Facilita testing y preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarRiegoScreen(
    viewModel: RiegoViewModel = viewModel(),
    onRiegoGuardado: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.agregarRiegoState.collectAsState()
    val cultivosDisponibles by viewModel.cultivosDisponibles.collectAsState()

    // Navegar atrás cuando se guarda exitosamente
    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onRiegoGuardado()
            viewModel.resetearFormulario()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "💧 Registrar Riego",
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
        containerColor = Color(0xFFE3F2FD)
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
            FormSection(titulo = "🌱 Cultivo") {
                CultivoSelector(
                    cultivos = cultivosDisponibles,
                    selectedId = uiState.cultivoSeleccionadoId,
                    onCultivoSelected = { viewModel.seleccionarCultivo(it) },
                    error = uiState.errorCultivo
                )
            }

            // ════════════════════════════════════════════════
            // Fecha del Riego
            // ════════════════════════════════════════════════
            FormSection(titulo = "📅 Fecha del Riego") {
                FechaDisplay(
                    fecha = uiState.fecha,
                    onFechaChange = { viewModel.actualizarFecha(it) }
                )
            }

            // ════════════════════════════════════════════════
            // Cantidad de Agua
            // ════════════════════════════════════════════════
            FormSection(titulo = "💧 Cantidad de Agua") {
                CantidadInput(
                    cantidad = uiState.cantidadMl,
                    onCantidadChange = { viewModel.actualizarCantidad(it) },
                    error = uiState.errorCantidad
                )
            }

            // ════════════════════════════════════════════════
            // Método de Riego
            // ════════════════════════════════════════════════
            FormSection(titulo = "🔧 Método de Riego") {
                MetodoSelector(
                    metodoSeleccionado = uiState.metodoSeleccionado,
                    onMetodoSelected = { viewModel.seleccionarMetodo(it) }
                )
            }

            // ════════════════════════════════════════════════
            // Notas (Opcional)
            // ════════════════════════════════════════════════
            FormSection(titulo = "📝 Notas (Opcional)") {
                OutlinedTextField(
                    value = uiState.notas,
                    onValueChange = { viewModel.actualizarNotas(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Observaciones del riego...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0288D1),
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
                onClick = { viewModel.guardarRiego() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0288D1)
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
                        text = "Guardar Riego",
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
private fun FormSection(
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
                color = Color(0xFF0277BD)
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
private fun CultivoSelector(
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

        // Mensaje de error
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
 * Display de fecha (simplificado, usa fecha actual).
 */
@Composable
private fun FechaDisplay(
    fecha: Long,
    onFechaChange: (Long) -> Unit
) {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("es", "ES"))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE3F2FD))
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
                color = Color(0xFF0277BD)
            )
            Text(
                text = "📅",
                fontSize = 20.sp
            )
        }
    }
    
    // Nota: Para un DatePicker real, usarías DatePickerDialog
    // Lo simplifico por ahora usando la fecha actual
}

/**
 * Input de cantidad con sufijo ml/L.
 */
@Composable
private fun CantidadInput(
    cantidad: String,
    onCantidadChange: (String) -> Unit,
    error: String?
) {
    Column {
        OutlinedTextField(
            value = cantidad,
            onValueChange = { 
                // Solo permitir números
                if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                    onCantidadChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: 500") },
            suffix = { Text("ml", color = Color(0xFF757575)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0288D1),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                errorBorderColor = Color(0xFFE53935)
            )
        )

        // Sugerencias rápidas
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("250", "500", "750", "1000").forEach { sugerencia ->
                SugerenciaChip(
                    text = if (sugerencia == "1000") "1L" else "${sugerencia}ml",
                    onClick = { onCantidadChange(sugerencia) }
                )
            }
        }

        // Mensaje de error
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

@Composable
private fun SugerenciaChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE3F2FD))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF0277BD)
        )
    }
}

/**
 * Selector de método de riego con chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetodoSelector(
    metodoSeleccionado: MetodoRiego,
    onMetodoSelected: (MetodoRiego) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetodoRiego.entries.forEach { metodo ->
            FilterChip(
                selected = metodo == metodoSeleccionado,
                onClick = { onMetodoSelected(metodo) },
                label = {
                    Text("${metodo.emoji} ${metodo.label}")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0288D1),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AgregarRiegoScreenPreview() {
    BiosimTheme {
        // Preview simplificado
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("💧 Registrar Riego") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0288D1),
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFFE3F2FD)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormSection(titulo = "🌱 Cultivo") {
                    Text("Selector de cultivo aquí")
                }
                FormSection(titulo = "💧 Cantidad") {
                    Text("Input de cantidad aquí")
                }
            }
        }
    }
}

