package com.biosim.data.repository

import com.biosim.data.dao.CultivoDao
import com.biosim.data.dao.PlagaDao
import com.biosim.data.entity.PlagaInspeccionEntity
import com.biosim.data.entity.PlagaTratamientoEntity
import com.biosim.model.PlagaInspeccion
import com.biosim.model.PlagaTratamiento
import com.biosim.model.toInspeccionEntity
import com.biosim.model.toInspeccionModel
import com.biosim.model.toTratamientoEntity
import com.biosim.model.toTratamientoModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar inspecciones y tratamientos de plagas.
 */
class PlagaRepository(
    private val plagaDao: PlagaDao,
    private val cultivoDao: CultivoDao
) {

    // ══════════════════════════════════════════════════════════════
    // INSPECCIONES
    // ══════════════════════════════════════════════════════════════

    val todasLasInspecciones: Flow<List<PlagaInspeccion>> = combine(
        plagaDao.obtenerTodasInspecciones(),
        cultivoDao.obtenerTodos()
    ) { inspecciones, cultivos ->
        inspecciones.map { inspeccion ->
            val cultivo = cultivos.find { it.id == inspeccion.cultivoId }
            val cantidadTratamientos = plagaDao.contarTratamientosPorInspeccion(inspeccion.id)
            inspeccion.toInspeccionModel(
                cultivoNombre = cultivo?.nombre ?: "Desconocido",
                cultivoEmoji = cultivo?.emoji ?: "🌱",
                cantidadTratamientos = cantidadTratamientos
            )
        }
    }

    val inspeccionesActivas: Flow<List<PlagaInspeccion>> = combine(
        plagaDao.obtenerInspeccionesActivas(),
        cultivoDao.obtenerTodos()
    ) { inspecciones, cultivos ->
        inspecciones.map { inspeccion ->
            val cultivo = cultivos.find { it.id == inspeccion.cultivoId }
            val cantidadTratamientos = plagaDao.contarTratamientosPorInspeccion(inspeccion.id)
            inspeccion.toInspeccionModel(
                cultivoNombre = cultivo?.nombre ?: "Desconocido",
                cultivoEmoji = cultivo?.emoji ?: "🌱",
                cantidadTratamientos = cantidadTratamientos
            )
        }
    }

    fun obtenerInspeccionesPorCultivo(cultivoId: Int): Flow<List<PlagaInspeccion>> {
        return combine(
            plagaDao.obtenerInspeccionesPorCultivo(cultivoId),
            cultivoDao.obtenerPorIdFlow(cultivoId)
        ) { inspecciones, cultivo ->
            inspecciones.map { inspeccion ->
                val cantidadTratamientos = plagaDao.contarTratamientosPorInspeccion(inspeccion.id)
                inspeccion.toInspeccionModel(
                    cultivoNombre = cultivo?.nombre ?: "Desconocido",
                    cultivoEmoji = cultivo?.emoji ?: "🌱",
                    cantidadTratamientos = cantidadTratamientos
                )
            }
        }
    }

    suspend fun obtenerInspeccionPorId(id: Int): PlagaInspeccion? {
        val entity = plagaDao.obtenerInspeccionPorId(id) ?: return null
        val cultivo = cultivoDao.obtenerPorId(entity.cultivoId)
        val cantidadTratamientos = plagaDao.contarTratamientosPorInspeccion(id)
        return entity.toInspeccionModel(
            cultivoNombre = cultivo?.nombre ?: "Desconocido",
            cultivoEmoji = cultivo?.emoji ?: "🌱",
            cantidadTratamientos = cantidadTratamientos
        )
    }

    fun obtenerInspeccionPorIdFlow(id: Int): Flow<PlagaInspeccion?> {
        return combine(
            plagaDao.obtenerInspeccionPorIdFlow(id),
            cultivoDao.obtenerTodos()
        ) { inspeccion, cultivos ->
            inspeccion?.let {
                val cultivo = cultivos.find { c -> c.id == it.cultivoId }
                val cantidadTratamientos = plagaDao.contarTratamientosPorInspeccion(it.id)
                it.toInspeccionModel(
                    cultivoNombre = cultivo?.nombre ?: "Desconocido",
                    cultivoEmoji = cultivo?.emoji ?: "🌱",
                    cantidadTratamientos = cantidadTratamientos
                )
            }
        }
    }

    suspend fun insertarInspeccion(
        cultivoId: Int,
        fecha: Long,
        tipoPlaga: String,
        nombrePlaga: String,
        nivelIncidencia: String,
        parteAfectada: String,
        observaciones: String? = null
    ): Long {
        val entity = PlagaInspeccionEntity(
            cultivoId = cultivoId,
            fecha = fecha,
            tipoPlaga = tipoPlaga,
            nombrePlaga = nombrePlaga,
            nivelIncidencia = nivelIncidencia,
            parteAfectada = parteAfectada,
            observaciones = observaciones
        )
        return plagaDao.insertarInspeccion(entity)
    }

    suspend fun actualizarInspeccion(inspeccion: PlagaInspeccion) {
        plagaDao.actualizarInspeccion(inspeccion.toInspeccionEntity())
    }

    suspend fun marcarInspeccionResuelta(id: Int, resuelta: Boolean) {
        plagaDao.marcarResuelta(id, resuelta)
    }

    suspend fun eliminarInspeccionPorId(id: Int) {
        plagaDao.eliminarInspeccionPorId(id)
    }

    // ══════════════════════════════════════════════════════════════
    // TRATAMIENTOS
    // ══════════════════════════════════════════════════════════════

    fun obtenerTratamientosPorInspeccion(inspeccionId: Int): Flow<List<PlagaTratamiento>> {
        return plagaDao.obtenerTratamientosPorInspeccion(inspeccionId).map { tratamientos ->
            tratamientos.map { it.toTratamientoModel() }
        }
    }

    suspend fun obtenerTratamientoPorId(id: Int): PlagaTratamiento? {
        return plagaDao.obtenerTratamientoPorId(id)?.toTratamientoModel()
    }

    suspend fun insertarTratamiento(
        inspeccionId: Int,
        producto: String,
        tipoProducto: String,
        dosisMl: Int,
        metodoAplicacion: String,
        fechaAplicacion: Long,
        observaciones: String? = null
    ): Long {
        val entity = PlagaTratamientoEntity(
            inspeccionId = inspeccionId,
            producto = producto,
            tipoProducto = tipoProducto,
            dosisMl = dosisMl,
            metodoAplicacion = metodoAplicacion,
            fechaAplicacion = fechaAplicacion,
            observaciones = observaciones
        )
        return plagaDao.insertarTratamiento(entity)
    }

    suspend fun actualizarTratamiento(tratamiento: PlagaTratamiento) {
        plagaDao.actualizarTratamiento(tratamiento.toTratamientoEntity())
    }

    suspend fun actualizarEfectividad(id: Int, efectividad: String) {
        plagaDao.actualizarEfectividad(id, efectividad)
    }

    suspend fun eliminarTratamientoPorId(id: Int) {
        plagaDao.eliminarTratamientoPorId(id)
    }

    suspend fun contarTratamientosPorInspeccion(inspeccionId: Int): Int {
        return plagaDao.contarTratamientosPorInspeccion(inspeccionId)
    }
}

