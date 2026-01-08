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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biosim.data.entity.NivelIncidencia
import com.biosim.data.entity.ParteAfectada
import com.biosim.data.entity.TipoPlaga
import com.biosim.model.Cultivo
import com.biosim.viewmodel.PlagaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarInspeccionScreen(
    viewModel: PlagaViewModel = viewModel(),
    onInspeccionGuardada: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.agregarInspeccionState.collectAsState()
    val cultivos by viewModel.cultivosDisponibles.collectAsState()

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onInspeccionGuardada()
            viewModel.resetearFormularioInspeccion()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Nueva Inspección", fontWeight = FontWeight.Bold) },
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
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cultivo
            InspeccionFormSection("🌱 Cultivo") {
                CultivoSelectorPlaga(cultivos, uiState.cultivoSeleccionadoId, { viewModel.seleccionarCultivoInspeccion(it) }, uiState.errorCultivo)
            }

            // Tipo de plaga
            InspeccionFormSection("🐛 Tipo de Plaga") {
                TipoPlagaSelector(uiState.tipoPlaga) { viewModel.seleccionarTipoPlaga(it) }
            }

            // Nombre de la plaga
            InspeccionFormSection("📝 Nombre de la Plaga") {
                OutlinedTextField(
                    value = uiState.nombrePlaga,
                    onValueChange = { viewModel.actualizarNombrePlaga(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Pulgón verde, Mildiu...") },
                    isError = uiState.errorNombre != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE65100),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
                uiState.errorNombre?.let {
                    Text(it, fontSize = 12.sp, color = Color(0xFFE53935))
                }
            }

            // Nivel de incidencia
            InspeccionFormSection("⚠️ Nivel de Incidencia") {
                NivelIncidenciaSelector(uiState.nivelIncidencia) { viewModel.seleccionarNivelIncidencia(it) }
            }

            // Parte afectada
            InspeccionFormSection("🍃 Parte Afectada") {
                ParteAfectadaSelector(uiState.parteAfectada) { viewModel.seleccionarParteAfectada(it) }
            }

            // Fecha
            InspeccionFormSection("📅 Fecha de Inspección") {
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("es", "ES"))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF3E0)).padding(16.dp)
                ) {
                    Text(sdf.format(Date(uiState.fecha)), fontSize = 14.sp, color = Color(0xFFBF360C))
                }
            }

            // Observaciones
            InspeccionFormSection("📋 Observaciones (Opcional)") {
                OutlinedTextField(
                    value = uiState.observaciones,
                    onValueChange = { viewModel.actualizarObservacionesInspeccion(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Detalles adicionales...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE65100),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Guardar
            Button(
                onClick = { viewModel.guardarInspeccion() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Inspección", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InspeccionFormSection(titulo: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFBF360C))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CultivoSelectorPlaga(
    cultivos: List<Cultivo>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = cultivos.find { it.id == selectedId }

    Column {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (error != null) Color(0xFFE53935) else Color(0xFFBDBDBD), RoundedCornerShape(12.dp))
                .clickable { expanded = true }.padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (selected != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selected.emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(selected.nombre, fontSize = 16.sp)
                    }
                } else {
                    Text("Selecciona un cultivo", color = Color(0xFF9E9E9E))
                }
                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF757575))
            }
            DropdownMenu(expanded, { expanded = false }, Modifier.fillMaxWidth(0.9f)) {
                cultivos.forEach { cultivo ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cultivo.emoji, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(cultivo.nombre)
                            }
                        },
                        onClick = { onSelect(cultivo.id); expanded = false }
                    )
                }
            }
        }
        error?.let { Text(it, fontSize = 12.sp, color = Color(0xFFE53935)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TipoPlagaSelector(selected: TipoPlaga, onSelect: (TipoPlaga) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TipoPlaga.entries.forEach { tipo ->
            FilterChip(
                selected = tipo == selected,
                onClick = { onSelect(tipo) },
                label = { Text("${tipo.emoji} ${tipo.label}", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE65100),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NivelIncidenciaSelector(selected: NivelIncidencia, onSelect: (NivelIncidencia) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NivelIncidencia.entries.forEach { nivel ->
            FilterChip(
                selected = nivel == selected,
                onClick = { onSelect(nivel) },
                label = { Text("${nivel.emoji} ${nivel.label}", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(nivel.color),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParteAfectadaSelector(selected: ParteAfectada, onSelect: (ParteAfectada) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ParteAfectada.entries.forEach { parte ->
            FilterChip(
                selected = parte == selected,
                onClick = { onSelect(parte) },
                label = { Text("${parte.emoji} ${parte.label}", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE65100),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

