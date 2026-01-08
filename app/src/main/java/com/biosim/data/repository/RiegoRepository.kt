package com.biosim.data.repository

import com.biosim.data.dao.CultivoDao
import com.biosim.data.dao.RiegoDao
import com.biosim.data.entity.RiegoEntity
import com.biosim.model.Riego
import com.biosim.model.toEntity
import com.biosim.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar datos de riegos.
 * 
 * ¿Por qué usar Repository Pattern?
 * - Abstracción: el ViewModel no sabe de dónde vienen los datos (Room, API, etc.).
 * - Single Source of Truth: centraliza la lógica de acceso a datos.
 * - Testing: fácil de mockear para tests unitarios.
 * - Reutilización: múltiples ViewModels pueden usar el mismo repositorio.
 */
class RiegoRepository(
    private val riegoDao: RiegoDao,
    private val cultivoDao: CultivoDao
) {

    /**
     * Obtiene todos los riegos con información del cultivo.
     * 
     * ¿Por qué combine?
     * - Necesitamos datos de dos tablas (riegos + cultivos).
     * - combine espera ambos Flows y emite cuando cualquiera cambia.
     */
    val todosLosRiegos: Flow<List<Riego>> = combine(
        riegoDao.obtenerTodos(),
        cultivoDao.obtenerTodos()
    ) { riegos, cultivos ->
        riegos.map { riego ->
            val cultivo = cultivos.find { it.id == riego.cultivoId }
            riego.toModel(
                cultivoNombre = cultivo?.nombre ?: "Desconocido",
                cultivoEmoji = cultivo?.emoji ?: "🌱"
            )
        }
    }

    /**
     * Obtiene riegos de un cultivo específico.
     */
    fun obtenerPorCultivo(cultivoId: Int): Flow<List<Riego>> {
        return combine(
            riegoDao.obtenerPorCultivo(cultivoId),
            cultivoDao.obtenerPorIdFlow(cultivoId)
        ) { riegos, cultivo ->
            riegos.map { riego ->
                riego.toModel(
                    cultivoNombre = cultivo?.nombre ?: "Desconocido",
                    cultivoEmoji = cultivo?.emoji ?: "🌱"
                )
            }
        }
    }

    /**
     * Obtiene un riego por ID.
     */
    suspend fun obtenerPorId(id: Int): Riego? {
        val entity = riegoDao.obtenerPorId(id) ?: return null
        val cultivo = cultivoDao.obtenerPorId(entity.cultivoId)
        return entity.toModel(
            cultivoNombre = cultivo?.nombre ?: "Desconocido",
            cultivoEmoji = cultivo?.emoji ?: "🌱"
        )
    }

    /**
     * Obtiene el último riego de un cultivo.
     */
    suspend fun obtenerUltimoRiego(cultivoId: Int): Riego? {
        val entity = riegoDao.obtenerUltimoRiego(cultivoId) ?: return null
        val cultivo = cultivoDao.obtenerPorId(cultivoId)
        return entity.toModel(
            cultivoNombre = cultivo?.nombre ?: "Desconocido",
            cultivoEmoji = cultivo?.emoji ?: "🌱"
        )
    }

    /**
     * Obtiene el total de agua usada en un cultivo.
     */
    suspend fun obtenerTotalAgua(cultivoId: Int): Int {
        return riegoDao.obtenerTotalAgua(cultivoId)
    }

    /**
     * Inserta un nuevo riego.
     */
    suspend fun insertar(riego: Riego): Long {
        return riegoDao.insertar(riego.toEntity())
    }

    /**
     * Inserta un riego desde datos básicos.
     */
    suspend fun insertarRiego(
        cultivoId: Int,
        fecha: Long,
        cantidadMl: Int,
        metodo: String,
        notas: String? = null
    ): Long {
        val entity = RiegoEntity(
            cultivoId = cultivoId,
            fecha = fecha,
            cantidadMl = cantidadMl,
            metodo = metodo,
            notas = notas
        )
        return riegoDao.insertar(entity)
    }

    /**
     * Actualiza un riego existente.
     */
    suspend fun actualizar(riego: Riego) {
        riegoDao.actualizar(riego.toEntity())
    }

    /**
     * Elimina un riego por ID.
     */
    suspend fun eliminarPorId(id: Int) {
        riegoDao.eliminarPorId(id)
    }

    /**
     * Elimina todos los riegos de un cultivo.
     */
    suspend fun eliminarPorCultivo(cultivoId: Int) {
        riegoDao.eliminarPorCultivo(cultivoId)
    }
}

