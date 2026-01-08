package com.biosim.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biosim.R
import com.biosim.ui.theme.BiosimTheme

/**
 * Pantalla del Menú Principal.
 * Muestra opciones de navegación a las diferentes secciones de la app.
 *
 * @param onNavigate Lambda que recibe la ruta de destino para navegar
 */
@Composable
fun MenuPrincipalScreen(
    onNavigate: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20), // Verde oscuro arriba
                        Color(0xFF2E7D32), // Verde medio
                        Color(0xFF388E3C)  // Verde claro abajo
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ══════════════════════════════════════════
        // Header / Título
        // ══════════════════════════════════════════
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "🌱",
            fontSize = 64.sp
        )
        
        Text(
            text = "Biosim",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "Gestión Inteligente de Cultivos",
            fontSize = 14.sp,
            color = Color(0xFFB9F6CA) // Verde claro
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // ══════════════════════════════════════════
        // Botones de Navegación
        // ══════════════════════════════════════════
        
        MenuButton(
            emoji = "🌿",
            texto = "Mis Cultivos",
            descripcion = "Gestiona tus plantas y cosechas",
            colorFondo = Color(0xFF43A047),
            onClick = { onNavigate("cultivos") }
        )
        
        MenuButton(
            emoji = "💧",
            texto = "Sistema de Riego",
            descripcion = "Controla el riego de tus cultivos",
            colorFondo = Color(0xFF039BE5),
            onClick = { onNavigate("riego") }
        )
        
        MenuButton(
            emoji = "🧪",
            texto = "Nutrientes",
            descripcion = "Fertilizantes y suplementos",
            colorFondo = Color(0xFF8E24AA),
            onClick = { onNavigate("nutrientes") }
        )
        
        MenuButton(
            emoji = "🐛",
            texto = "Control de Plagas",
            descripcion = "Monitorea y previene plagas",
            colorFondo = Color(0xFFE65100),
            onClick = { onNavigate("plagas") }
        )
        
        MenuButton(
            emoji = "📷",
            texto = "Cámara",
            descripcion = "Captura y analiza tus plantas",
            colorFondo = Color(0xFF455A64),
            onClick = { onNavigate("camera") }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Botón grande del menú con emoji, texto y descripción.
 */
@Composable
private fun MenuButton(
    emoji: String,
    texto: String,
    descripcion: String,
    colorFondo: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorFondo
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Fila con emoji y texto
            Text(
                text = "$emoji  $texto",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Descripción
            Text(
                text = descripcion,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFE8F5E9) // Blanco verdoso
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuPrincipalScreenPreview() {
    BiosimTheme {
        MenuPrincipalScreen()
    }
}

