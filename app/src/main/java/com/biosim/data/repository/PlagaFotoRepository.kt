package com.biosim.data.repository

import com.biosim.data.dao.PlagaFotoDao
import com.biosim.data.entity.PlagaFotoEntity
import com.biosim.model.PlagaFoto
import com.biosim.model.toEntity
import com.biosim.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar fotos de inspecciones de plagas con geolocalización.
 * 
 * Decisión: Se usa internal storage (getExternalFilesDir) para las fotos
 * porque no requiere permisos adicionales y las fotos se eliminan
 * automáticamente al desinstalar la app.
 */
class PlagaFotoRepository(
    private val plagaFotoDao: PlagaFotoDao
) {

    fun obtenerPorInspeccion(inspeccionId: Int): Flow<List<PlagaFoto>> {
        return plagaFotoDao.obtenerPorInspeccion(inspeccionId).map { fotos ->
            fotos.map { it.toModel() }
        }
    }

    suspend fun obtenerPorId(id: Int): PlagaFoto? {
        return plagaFotoDao.obtenerPorId(id)?.toModel()
    }

    fun obtenerPorIdFlow(id: Int): Flow<PlagaFoto?> {
        return plagaFotoDao.obtenerPorIdFlow(id).map { it?.toModel() }
    }

    fun contarPorInspeccionFlow(inspeccionId: Int): Flow<Int> {
        return plagaFotoDao.contarPorInspeccionFlow(inspeccionId)
    }

    suspend fun contarPorInspeccion(inspeccionId: Int): Int {
        return plagaFotoDao.contarPorInspeccion(inspeccionId)
    }

    suspend fun contarConUbicacion(inspeccionId: Int): Int {
        return plagaFotoDao.contarConUbicacion(inspeccionId)
    }

    /**
     * Inserta una foto con geolocalización opcional.
     * 
     * @param latitud Latitud GPS (null si no disponible)
     * @param longitud Longitud GPS (null si no disponible)
     */
    suspend fun insertar(
        inspeccionId: Int,
        fotoUri: String,
        descripcion: String? = null,
        latitud: Double? = null,
        longitud: Double? = null
    ): Long {
        val entity = PlagaFotoEntity(
            inspeccionId = inspeccionId,
            fotoUri = fotoUri,
            fecha = System.currentTimeMillis(),
            descripcion = descripcion,
            latitud = latitud,
            longitud = longitud
        )
        return plagaFotoDao.insertar(entity)
    }

    suspend fun actualizar(foto: PlagaFoto) {
        plagaFotoDao.actualizar(foto.toEntity())
    }

    suspend fun eliminarPorId(id: Int) {
        plagaFotoDao.eliminarPorId(id)
    }

    suspend fun eliminarPorInspeccion(inspeccionId: Int) {
        plagaFotoDao.eliminarPorInspeccion(inspeccionId)
    }
}

