package com.biosim.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.data.entity.MetodoAplicacionTratamiento
import com.biosim.data.entity.TipoProducto
import com.biosim.viewmodel.PlagaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarTratamientoScreen(
    inspeccionId: Int,
    viewModel: PlagaViewModel = viewModel(),
    onTratamientoGuardado: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.agregarTratamientoState.collectAsState()

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onTratamientoGuardado()
            viewModel.resetearFormularioTratamiento()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💊 Nuevo Tratamiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Producto
            TratamientoFormSection("💊 Producto") {
                OutlinedTextField(
                    value = uiState.producto,
                    onValueChange = { viewModel.actualizarProducto(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Aceite de Neem, Bacillus thuringiensis...") },
                    isError = uiState.errorProducto != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0288D1),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
                uiState.errorProducto?.let {
                    Text(it, fontSize = 12.sp, color = Color(0xFFE53935))
                }
            }

            // Tipo de producto
            TratamientoFormSection("🏷️ Tipo de Producto") {
                TipoProductoSelector(uiState.tipoProducto) { viewModel.seleccionarTipoProducto(it) }
            }

            // Dosis
            TratamientoFormSection("💧 Dosis") {
                Column {
                    OutlinedTextField(
                        value = uiState.dosisMl,
                        onValueChange = { 
                            if (it.all { c -> c.isDigit() } || it.isEmpty()) {
                                viewModel.actualizarDosisTratamiento(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: 100") },
                        suffix = { Text("ml", color = Color(0xFF757575)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.errorDosis != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0288D1),
                            unfocusedBorderColor = Color(0xFFBDBDBD)
                        )
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("50", "100", "250", "500").forEach { sug ->
                            Box(
                                Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE3F2FD))
                                    .clickable { viewModel.actualizarDosisTratamiento(sug) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("${sug}ml", fontSize = 12.sp, color = Color(0xFF0277BD))
                            }
                        }
                    }
                    
                    uiState.errorDosis?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, fontSize = 12.sp, color = Color(0xFFE53935))
                    }
                }
            }

            // Método de aplicación
            TratamientoFormSection("🔧 Método de Aplicación") {
                MetodoTratamientoSelector(uiState.metodoAplicacion) { viewModel.seleccionarMetodoTratamiento(it) }
            }

            // Fecha
            TratamientoFormSection("📅 Fecha de Aplicación") {
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("es", "ES"))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE3F2FD)).padding(16.dp)
                ) {
                    Text(sdf.format(Date(uiState.fechaAplicacion)), fontSize = 14.sp, color = Color(0xFF0277BD))
                }
            }

            // Observaciones
            TratamientoFormSection("📝 Observaciones (Opcional)") {
                OutlinedTextField(
                    value = uiState.observaciones,
                    onValueChange = { viewModel.actualizarObservacionesTratamiento(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Notas del tratamiento...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0288D1),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Guardar
            Button(
                onClick = { viewModel.guardarTratamiento(inspeccionId) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Tratamiento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TratamientoFormSection(titulo: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0277BD))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TipoProductoSelector(selected: TipoProducto, onSelect: (TipoProducto) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TipoProducto.entries.forEach { tipo ->
            FilterChip(
                selected = tipo == selected,
                onClick = { onSelect(tipo) },
                label = { Text("${tipo.emoji} ${tipo.label}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0288D1),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetodoTratamientoSelector(selected: MetodoAplicacionTratamiento, onSelect: (MetodoAplicacionTratamiento) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MetodoAplicacionTratamiento.entries.forEach { metodo ->
            FilterChip(
                selected = metodo == selected,
                onClick = { onSelect(metodo) },
                label = { Text("${metodo.emoji} ${metodo.label}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0288D1),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

