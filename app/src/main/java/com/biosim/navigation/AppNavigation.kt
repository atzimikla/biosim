package com.biosim.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.biosim.screens.AgregarInspeccionScreen
import com.biosim.screens.AgregarNutrienteScreen
import com.biosim.screens.AgregarRiegoScreen
import com.biosim.screens.AgregarTratamientoScreen
import com.biosim.screens.CapturarFotoPlagaScreen
import com.biosim.screens.CapturarHallazgoScreen
import com.biosim.screens.FotoMapaScreen
import com.biosim.screens.CultivoDetalleScreen
import com.biosim.screens.CultivosScreen
import com.biosim.screens.HallazgoDetalleScreen
import com.biosim.screens.HallazgosScreen
import com.biosim.screens.InspeccionDetalleScreen
import com.biosim.screens.MenuPrincipalScreen
import com.biosim.screens.NutrientesScreen
import com.biosim.screens.PlagasScreen
import com.biosim.screens.RiegosScreen

/**
 * Rutas de navegación de la aplicación.
 * Usa este object para referenciar las rutas de forma segura.
 */
object AppRoutes {
    const val MENU = "menu"
    const val CULTIVOS = "cultivos"
    const val CULTIVO_DETALLE = "cultivo_detalle/{id}"
    const val RIEGO = "riego"
    const val AGREGAR_RIEGO = "agregar_riego"
    const val NUTRIENTES = "nutrientes"
    const val AGREGAR_NUTRIENTE = "agregar_nutriente"
    const val PLAGAS = "plagas"
    const val AGREGAR_INSPECCION = "agregar_inspeccion"
    const val INSPECCION_DETALLE = "inspeccion_detalle/{id}"
    const val AGREGAR_TRATAMIENTO = "agregar_tratamiento/{inspeccionId}"
    const val CAPTURAR_FOTOS_PLAGA = "capturar_fotos_plaga/{inspeccionId}"
    const val FOTO_MAPA = "foto_mapa/{fotoId}"
    const val HALLAZGOS = "hallazgos"
    const val CAPTURAR_HALLAZGO = "capturar_hallazgo"
    const val HALLAZGO_DETALLE = "hallazgo_detalle/{id}"
    const val CAMERA = "camera"
    
    // Helpers para construir rutas con parámetros
    fun cultivoDetalle(id: Int) = "cultivo_detalle/$id"
    fun inspeccionDetalle(id: Int) = "inspeccion_detalle/$id"
    fun agregarTratamiento(inspeccionId: Int) = "agregar_tratamiento/$inspeccionId"
    fun capturarFotosPlaga(inspeccionId: Int) = "capturar_fotos_plaga/$inspeccionId"
    fun fotoMapa(fotoId: Int) = "foto_mapa/$fotoId"
    fun hallazgoDetalle(id: Int) = "hallazgo_detalle/$id"
}

/**
 * NavHost principal de la aplicación.
 * Llama esta función desde setContent {} en MainActivity.
 *
 * @param modifier Modifier opcional para personalizar el NavHost
 * @param navController Controlador de navegación (se crea automáticamente si no se provee)
 * @param startDestination Ruta inicial (por defecto "menu")
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.MENU
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ══════════════════════════════════════════════════════════
        // Ruta: "menu" → Pantalla principal del menú
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.MENU) {
            MenuPrincipalScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "cultivos" → Lista de cultivos
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.CULTIVOS) {
            CultivosScreen(
                onNavigateToDetalle = { cultivoId ->
                    navController.navigate(AppRoutes.cultivoDetalle(cultivoId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "cultivo_detalle/{id}" → Detalle de un cultivo
        // ══════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.CULTIVO_DETALLE,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val cultivoId = backStackEntry.arguments?.getInt("id") ?: 0
            CultivoDetalleScreen(
                cultivoId = cultivoId,
                onBack = { navController.popBackStack() },
                onEdit = { /* TODO: Navegar a edición */ },
                onDelete = { 
                    /* TODO: Eliminar y volver */
                    navController.popBackStack()
                }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "riego" → Lista de riegos
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.RIEGO) {
            RiegosScreen(
                onAgregarRiego = { navController.navigate(AppRoutes.AGREGAR_RIEGO) },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "agregar_riego" → Formulario para agregar riego
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.AGREGAR_RIEGO) {
            AgregarRiegoScreen(
                onRiegoGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "nutrientes" → Lista de aplicaciones de nutrientes
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.NUTRIENTES) {
            NutrientesScreen(
                onAgregarNutriente = { navController.navigate(AppRoutes.AGREGAR_NUTRIENTE) },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "agregar_nutriente" → Formulario para agregar nutriente
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.AGREGAR_NUTRIENTE) {
            AgregarNutrienteScreen(
                onNutrienteGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "plagas" → Lista de inspecciones de plagas
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.PLAGAS) {
            PlagasScreen(
                onAgregarInspeccion = { navController.navigate(AppRoutes.AGREGAR_INSPECCION) },
                onVerDetalle = { inspeccionId ->
                    navController.navigate(AppRoutes.inspeccionDetalle(inspeccionId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "agregar_inspeccion" → Formulario para nueva inspección
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.AGREGAR_INSPECCION) {
            AgregarInspeccionScreen(
                onInspeccionGuardada = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "inspeccion_detalle/{id}" → Detalle de inspección
        // ══════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.INSPECCION_DETALLE,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val inspeccionId = backStackEntry.arguments?.getInt("id") ?: 0
            InspeccionDetalleScreen(
                inspeccionId = inspeccionId,
                onAgregarTratamiento = { id ->
                    navController.navigate(AppRoutes.agregarTratamiento(id))
                },
                onAgregarFotos = { id ->
                    navController.navigate(AppRoutes.capturarFotosPlaga(id))
                },
                onVerFotoEnMapa = { fotoId ->
                    navController.navigate(AppRoutes.fotoMapa(fotoId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "agregar_tratamiento/{inspeccionId}" → Nuevo tratamiento
        // ══════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.AGREGAR_TRATAMIENTO,
            arguments = listOf(
                navArgument("inspeccionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val inspeccionId = backStackEntry.arguments?.getInt("inspeccionId") ?: 0
            AgregarTratamientoScreen(
                inspeccionId = inspeccionId,
                onTratamientoGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "capturar_fotos_plaga/{inspeccionId}" → Captura de fotos con GPS
        // ══════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.CAPTURAR_FOTOS_PLAGA,
            arguments = listOf(
                navArgument("inspeccionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val inspeccionId = backStackEntry.arguments?.getInt("inspeccionId") ?: 0
            CapturarFotoPlagaScreen(
                inspeccionId = inspeccionId,
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "foto_mapa/{fotoId}" → Ver foto en mapa
        // ══════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.FOTO_MAPA,
            arguments = listOf(
                navArgument("fotoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fotoId = backStackEntry.arguments?.getInt("fotoId") ?: 0
            FotoMapaScreen(
                fotoId = fotoId,
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "hallazgos" → Lista de hallazgos del cultivo
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.HALLAZGOS) {
            HallazgosScreen(
                onCapturarHallazgo = { navController.navigate(AppRoutes.CAPTURAR_HALLAZGO) },
                onVerDetalle = { hallazgoId ->
                    navController.navigate(AppRoutes.hallazgoDetalle(hallazgoId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "capturar_hallazgo" → Captura de foto + ubicación
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.CAPTURAR_HALLAZGO) {
            CapturarHallazgoScreen(
                onHallazgoGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "hallazgo_detalle/{id}" → Detalle con mapa
        // ══════════════════════════════════════════════════════════
        composable(
            route = AppRoutes.HALLAZGO_DETALLE,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val hallazgoId = backStackEntry.arguments?.getInt("id") ?: 0
            HallazgoDetalleScreen(
                hallazgoId = hallazgoId,
                onEliminar = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════
        // Ruta: "camera" → Pantalla de cámara (antigua, redirige a hallazgos)
        // ══════════════════════════════════════════════════════════
        composable(route = AppRoutes.CAMERA) {
            // Redirigir a la nueva funcionalidad de hallazgos
            HallazgosScreen(
                onCapturarHallazgo = { navController.navigate(AppRoutes.CAPTURAR_HALLAZGO) },
                onVerDetalle = { hallazgoId ->
                    navController.navigate(AppRoutes.hallazgoDetalle(hallazgoId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Pantalla placeholder temporal para las secciones no implementadas.
 * Reemplaza cada una con su pantalla real cuando la desarrolles.
 */
@Composable
fun PlaceholderScreen(
    titulo: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Próximamente...",
            fontSize = 16.sp,
            color = Color(0xFF757575)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF43A047)
            )
        ) {
            Text(
                text = "← Volver al Menú",
                fontSize = 16.sp
            )
        }
    }
}
