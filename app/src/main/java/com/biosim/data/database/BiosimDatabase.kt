package com.biosim.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.biosim.data.dao.CultivoDao
import com.biosim.data.dao.HallazgoDao
import com.biosim.data.dao.NutrienteDao
import com.biosim.data.dao.PlagaDao
import com.biosim.data.dao.PlagaFotoDao
import com.biosim.data.dao.RiegoDao
import com.biosim.data.entity.CultivoEntity
import com.biosim.data.entity.HallazgoEntity
import com.biosim.data.entity.MetodoAplicacion
import com.biosim.data.entity.MetodoAplicacionTratamiento
import com.biosim.data.entity.MetodoRiego
import com.biosim.data.entity.NivelIncidencia
import com.biosim.data.entity.NutrienteAplicacionEntity
import com.biosim.data.entity.ParteAfectada
import com.biosim.data.entity.PlagaFotoEntity
import com.biosim.data.entity.PlagaInspeccionEntity
import com.biosim.data.entity.PlagaTratamientoEntity
import com.biosim.data.entity.RiegoEntity
import com.biosim.data.entity.TipoHallazgo
import com.biosim.data.entity.TipoNutriente
import com.biosim.data.entity.TipoPlaga
import com.biosim.data.entity.TipoProducto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room para la aplicación Biosim.
 * Contiene las tablas de cultivos, riegos, nutrientes, plagas y hallazgos.
 * 
 * IMPORTANTE: Incrementar version cuando cambies el schema.
 * fallbackToDestructiveMigration() recreará la BD en desarrollo.
 */
@Database(
    entities = [
        CultivoEntity::class, 
        RiegoEntity::class, 
        NutrienteAplicacionEntity::class,
        PlagaInspeccionEntity::class,
        PlagaTratamientoEntity::class,
        PlagaFotoEntity::class,
        HallazgoEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class BiosimDatabase : RoomDatabase() {

    /**
     * DAO para operaciones con cultivos.
     */
    abstract fun cultivoDao(): CultivoDao

    /**
     * DAO para operaciones con riegos.
     */
    abstract fun riegoDao(): RiegoDao

    /**
     * DAO para operaciones con nutrientes.
     */
    abstract fun nutrienteDao(): NutrienteDao

    /**
     * DAO para operaciones con plagas (inspecciones y tratamientos).
     */
    abstract fun plagaDao(): PlagaDao

    /**
     * DAO para operaciones con fotos de inspecciones de plagas.
     */
    abstract fun plagaFotoDao(): PlagaFotoDao

    /**
     * DAO para operaciones con hallazgos de cultivo.
     */
    abstract fun hallazgoDao(): HallazgoDao

    companion object {
        @Volatile
        private var INSTANCE: BiosimDatabase? = null

        /**
         * Obtiene la instancia singleton de la base de datos.
         * Crea la base de datos si no existe.
         */
        fun obtenerDatabase(context: Context): BiosimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BiosimDatabase::class.java,
                    "biosim_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback para poblar la base de datos con datos iniciales.
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Poblar con datos iniciales en un coroutine
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        poblarDatosIniciales(
                            database.cultivoDao(),
                            database.riegoDao(),
                            database.nutrienteDao(),
                            database.plagaDao(),
                            database.hallazgoDao()
                        )
                    }
                }
            }
        }

        /**
         * Inserta datos de ejemplo cuando se crea la base de datos por primera vez.
         */
        private suspend fun poblarDatosIniciales(
            cultivoDao: CultivoDao,
            riegoDao: RiegoDao,
            nutrienteDao: NutrienteDao,
            plagaDao: PlagaDao,
            hallazgoDao: HallazgoDao
        ) {
            // Solo poblar si la tabla está vacía
            if (cultivoDao.contarTodos() == 0) {
                val cultivosIniciales = listOf(
                    CultivoEntity(
                        id = 1,
                        nombre = "Tomates Cherry",
                        tipo = "Hortaliza",
                        emoji = "🍅",
                        ubicacion = "Invernadero A",
                        fechaSiembra = "15/11/2024",
                        estado = "PRODUCIENDO",
                        diasDesdeSiembra = 45,
                        proximoRiego = "Hoy 18:00"
                    ),
                    CultivoEntity(
                        id = 2,
                        nombre = "Lechuga Romana",
                        tipo = "Hortaliza",
                        emoji = "🥬",
                        ubicacion = "Huerto Exterior",
                        fechaSiembra = "01/12/2024",
                        estado = "CRECIENDO",
                        diasDesdeSiembra = 8,
                        proximoRiego = "Mañana 08:00"
                    ),
                    CultivoEntity(
                        id = 3,
                        nombre = "Fresas",
                        tipo = "Fruta",
                        emoji = "🍓",
                        ubicacion = "Invernadero B",
                        fechaSiembra = "20/10/2024",
                        estado = "FLORECIENDO",
                        diasDesdeSiembra = 50,
                        proximoRiego = "Hoy 20:00"
                    )
                )
                cultivoDao.insertarTodos(cultivosIniciales)

                // Poblar riegos de ejemplo
                val ahora = System.currentTimeMillis()
                val unDia = 24 * 60 * 60 * 1000L
                
                val riegosIniciales = listOf(
                    RiegoEntity(
                        cultivoId = 1,
                        fecha = ahora - unDia,
                        cantidadMl = 500,
                        metodo = MetodoRiego.GOTEO.name,
                        notas = "Riego matutino"
                    ),
                    RiegoEntity(
                        cultivoId = 1,
                        fecha = ahora - (3 * unDia),
                        cantidadMl = 750,
                        metodo = MetodoRiego.GOTEO.name
                    ),
                    RiegoEntity(
                        cultivoId = 2,
                        fecha = ahora - (2 * unDia),
                        cantidadMl = 300,
                        metodo = MetodoRiego.MANUAL.name,
                        notas = "Lechuga necesita menos agua"
                    ),
                    RiegoEntity(
                        cultivoId = 3,
                        fecha = ahora - unDia,
                        cantidadMl = 400,
                        metodo = MetodoRiego.ASPERSION.name
                    )
                )
                riegoDao.insertarTodos(riegosIniciales)

                // Poblar nutrientes de ejemplo
                val nutrientesIniciales = listOf(
                    NutrienteAplicacionEntity(
                        cultivoId = 1,
                        fecha = ahora - (5 * unDia),
                        tipoNutriente = TipoNutriente.NPK.name,
                        cantidadGramos = 50,
                        metodoAplicacion = MetodoAplicacion.FERTIRRIEGO.name,
                        comentario = "Fertilización semanal"
                    ),
                    NutrienteAplicacionEntity(
                        cultivoId = 1,
                        fecha = ahora - (12 * unDia),
                        tipoNutriente = TipoNutriente.CALCIO.name,
                        cantidadGramos = 25,
                        metodoAplicacion = MetodoAplicacion.FOLIAR.name,
                        comentario = "Prevención de pudrición apical"
                    ),
                    NutrienteAplicacionEntity(
                        cultivoId = 2,
                        fecha = ahora - (3 * unDia),
                        tipoNutriente = TipoNutriente.NITROGENO.name,
                        cantidadGramos = 30,
                        metodoAplicacion = MetodoAplicacion.SUELO.name
                    ),
                    NutrienteAplicacionEntity(
                        cultivoId = 3,
                        fecha = ahora - (7 * unDia),
                        tipoNutriente = TipoNutriente.POTASIO.name,
                        cantidadGramos = 40,
                        metodoAplicacion = MetodoAplicacion.FERTIRRIEGO.name,
                        comentario = "Para mejorar fructificación"
                    )
                )
                nutrienteDao.insertarTodos(nutrientesIniciales)

                // Poblar inspecciones de plagas de ejemplo
                val inspeccionesIniciales = listOf(
                    PlagaInspeccionEntity(
                        id = 1,
                        cultivoId = 1,
                        fecha = ahora - (2 * unDia),
                        tipoPlaga = TipoPlaga.INSECTO.name,
                        nombrePlaga = "Pulgón verde",
                        nivelIncidencia = NivelIncidencia.MEDIO.name,
                        parteAfectada = ParteAfectada.HOJAS.name,
                        observaciones = "Se detectaron colonias en hojas inferiores",
                        resuelta = false
                    ),
                    PlagaInspeccionEntity(
                        id = 2,
                        cultivoId = 3,
                        fecha = ahora - (5 * unDia),
                        tipoPlaga = TipoPlaga.HONGO.name,
                        nombrePlaga = "Botrytis (moho gris)",
                        nivelIncidencia = NivelIncidencia.ALTO.name,
                        parteAfectada = ParteAfectada.FRUTO.name,
                        observaciones = "Humedad alta en el invernadero",
                        resuelta = true
                    ),
                    PlagaInspeccionEntity(
                        id = 3,
                        cultivoId = 2,
                        fecha = ahora - unDia,
                        tipoPlaga = TipoPlaga.INSECTO.name,
                        nombrePlaga = "Trips",
                        nivelIncidencia = NivelIncidencia.BAJO.name,
                        parteAfectada = ParteAfectada.HOJAS.name,
                        observaciones = "Pocas manchas plateadas detectadas"
                    )
                )
                plagaDao.insertarInspecciones(inspeccionesIniciales)

                // Poblar tratamientos de ejemplo
                val tratamientosIniciales = listOf(
                    PlagaTratamientoEntity(
                        inspeccionId = 1,
                        producto = "Aceite de Neem",
                        tipoProducto = TipoProducto.ORGANICO.name,
                        dosisMl = 50,
                        metodoAplicacion = MetodoAplicacionTratamiento.FOLIAR.name,
                        fechaAplicacion = ahora - unDia,
                        observaciones = "Primera aplicación"
                    ),
                    PlagaTratamientoEntity(
                        inspeccionId = 2,
                        producto = "Trichoderma harzianum",
                        tipoProducto = TipoProducto.BIOLOGICO.name,
                        dosisMl = 100,
                        metodoAplicacion = MetodoAplicacionTratamiento.FOLIAR.name,
                        fechaAplicacion = ahora - (4 * unDia),
                        observaciones = "Control biológico",
                        efectividad = "EFECTIVO"
                    ),
                    PlagaTratamientoEntity(
                        inspeccionId = 2,
                        producto = "Fungicida cúprico",
                        tipoProducto = TipoProducto.QUIMICO.name,
                        dosisMl = 75,
                        metodoAplicacion = MetodoAplicacionTratamiento.FOLIAR.name,
                        fechaAplicacion = ahora - (3 * unDia),
                        efectividad = "EFECTIVO"
                    )
                )
                plagaDao.insertarTratamientos(tratamientosIniciales)

                // Nota: Los hallazgos se crean desde la app con fotos reales
                // No se pueblan datos de ejemplo porque requieren fotoUri válida
            }
        }
    }
}
