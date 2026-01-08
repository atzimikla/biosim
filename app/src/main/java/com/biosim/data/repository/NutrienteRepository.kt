package com.biosim.data.repository

import com.biosim.data.dao.CultivoDao
import com.biosim.data.dao.NutrienteDao
import com.biosim.data.entity.NutrienteAplicacionEntity
import com.biosim.model.NutrienteAplicacion
import com.biosim.model.toNutrienteEntity
import com.biosim.model.toNutrienteModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repositorio para gestionar datos de aplicaciones de nutrientes.
 */
class NutrienteRepository(
    private val nutrienteDao: NutrienteDao,
    private val cultivoDao: CultivoDao
) {

    /**
     * Obtiene todas las aplicaciones con información del cultivo.
     */
    val todasLasAplicaciones: Flow<List<NutrienteAplicacion>> = combine(
        nutrienteDao.obtenerTodos(),
        cultivoDao.obtenerTodos()
    ) { aplicaciones, cultivos ->
        aplicaciones.map { aplicacion ->
            val cultivo = cultivos.find { it.id == aplicacion.cultivoId }
            aplicacion.toNutrienteModel(
                cultivoNombre = cultivo?.nombre ?: "Desconocido",
                cultivoEmoji = cultivo?.emoji ?: "🌱"
            )
        }
    }

    /**
     * Obtiene aplicaciones de un cultivo específico.
     */
    fun obtenerPorCultivo(cultivoId: Int): Flow<List<NutrienteAplicacion>> {
        return combine(
            nutrienteDao.obtenerPorCultivo(cultivoId),
            cultivoDao.obtenerPorIdFlow(cultivoId)
        ) { aplicaciones, cultivo ->
            aplicaciones.map { aplicacion ->
                aplicacion.toNutrienteModel(
                    cultivoNombre = cultivo?.nombre ?: "Desconocido",
                    cultivoEmoji = cultivo?.emoji ?: "🌱"
                )
            }
        }
    }

    /**
     * Obtiene una aplicación por ID.
     */
    suspend fun obtenerPorId(id: Int): NutrienteAplicacion? {
        val entity = nutrienteDao.obtenerPorId(id) ?: return null
        val cultivo = cultivoDao.obtenerPorId(entity.cultivoId)
        return entity.toNutrienteModel(
            cultivoNombre = cultivo?.nombre ?: "Desconocido",
            cultivoEmoji = cultivo?.emoji ?: "🌱"
        )
    }

    /**
     * Obtiene la última aplicación de un cultivo.
     */
    suspend fun obtenerUltimaAplicacion(cultivoId: Int): NutrienteAplicacion? {
        val entity = nutrienteDao.obtenerUltimaAplicacion(cultivoId) ?: return null
        val cultivo = cultivoDao.obtenerPorId(cultivoId)
        return entity.toNutrienteModel(
            cultivoNombre = cultivo?.nombre ?: "Desconocido",
            cultivoEmoji = cultivo?.emoji ?: "🌱"
        )
    }

    /**
     * Obtiene el total de nutrientes aplicados a un cultivo.
     */
    suspend fun obtenerTotalAplicado(cultivoId: Int): Int {
        return nutrienteDao.obtenerTotalAplicado(cultivoId)
    }

    /**
     * Inserta una nueva aplicación.
     */
    suspend fun insertar(aplicacion: NutrienteAplicacion): Long {
        return nutrienteDao.insertar(aplicacion.toNutrienteEntity())
    }

    /**
     * Inserta una aplicación desde datos básicos.
     */
    suspend fun insertarAplicacion(
        cultivoId: Int,
        fecha: Long,
        tipoNutriente: String,
        cantidadGramos: Int,
        metodoAplicacion: String,
        comentario: String? = null
    ): Long {
        val entity = NutrienteAplicacionEntity(
            cultivoId = cultivoId,
            fecha = fecha,
            tipoNutriente = tipoNutriente,
            cantidadGramos = cantidadGramos,
            metodoAplicacion = metodoAplicacion,
            comentario = comentario
        )
        return nutrienteDao.insertar(entity)
    }

    /**
     * Actualiza una aplicación existente.
     */
    suspend fun actualizar(aplicacion: NutrienteAplicacion) {
        nutrienteDao.actualizar(aplicacion.toNutrienteEntity())
    }

    /**
     * Elimina una aplicación por ID.
     */
    suspend fun eliminarPorId(id: Int) {
        nutrienteDao.eliminarPorId(id)
    }

    /**
     * Elimina todas las aplicaciones de un cultivo.
     */
    suspend fun eliminarPorCultivo(cultivoId: Int) {
        nutrienteDao.eliminarPorCultivo(cultivoId)
    }
}

