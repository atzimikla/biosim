package com.biosim.data.repository

import com.biosim.data.dao.CultivoDao
import com.biosim.data.dao.HallazgoDao
import com.biosim.data.entity.HallazgoEntity
import com.biosim.model.Hallazgo
import com.biosim.model.toEntity
import com.biosim.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repositorio para gestionar hallazgos de cultivo.
 */
class HallazgoRepository(
    private val hallazgoDao: HallazgoDao,
    private val cultivoDao: CultivoDao
) {

    val todosLosHallazgos: Flow<List<Hallazgo>> = combine(
        hallazgoDao.obtenerTodos(),
        cultivoDao.obtenerTodos()
    ) { hallazgos, cultivos ->
        hallazgos.map { hallazgo ->
            val cultivo = cultivos.find { it.id == hallazgo.cultivoId }
            hallazgo.toModel(
                cultivoNombre = cultivo?.nombre ?: "Desconocido",
                cultivoEmoji = cultivo?.emoji ?: "🌱"
            )
        }
    }

    fun obtenerPorCultivo(cultivoId: Int): Flow<List<Hallazgo>> {
        return combine(
            hallazgoDao.obtenerPorCultivo(cultivoId),
            cultivoDao.obtenerPorIdFlow(cultivoId)
        ) { hallazgos, cultivo ->
            hallazgos.map { hallazgo ->
                hallazgo.toModel(
                    cultivoNombre = cultivo?.nombre ?: "Desconocido",
                    cultivoEmoji = cultivo?.emoji ?: "🌱"
                )
            }
        }
    }

    suspend fun obtenerPorId(id: Int): Hallazgo? {
        val entity = hallazgoDao.obtenerPorId(id) ?: return null
        val cultivo = cultivoDao.obtenerPorId(entity.cultivoId)
        return entity.toModel(
            cultivoNombre = cultivo?.nombre ?: "Desconocido",
            cultivoEmoji = cultivo?.emoji ?: "🌱"
        )
    }

    fun obtenerPorIdFlow(id: Int): Flow<Hallazgo?> {
        return combine(
            hallazgoDao.obtenerPorIdFlow(id),
            cultivoDao.obtenerTodos()
        ) { hallazgo, cultivos ->
            hallazgo?.let {
                val cultivo = cultivos.find { c -> c.id == it.cultivoId }
                it.toModel(
                    cultivoNombre = cultivo?.nombre ?: "Desconocido",
                    cultivoEmoji = cultivo?.emoji ?: "🌱"
                )
            }
        }
    }

    suspend fun insertar(
        cultivoId: Int,
        fecha: Long,
        fotoUri: String,
        latitud: Double,
        longitud: Double,
        descripcion: String? = null,
        tipoHallazgo: String = "OBSERVACION"
    ): Long {
        val entity = HallazgoEntity(
            cultivoId = cultivoId,
            fecha = fecha,
            fotoUri = fotoUri,
            latitud = latitud,
            longitud = longitud,
            descripcion = descripcion,
            tipoHallazgo = tipoHallazgo
        )
        return hallazgoDao.insertar(entity)
    }

    suspend fun actualizar(hallazgo: Hallazgo) {
        hallazgoDao.actualizar(hallazgo.toEntity())
    }

    suspend fun eliminarPorId(id: Int) {
        hallazgoDao.eliminarPorId(id)
    }

    suspend fun contarPorCultivo(cultivoId: Int): Int {
        return hallazgoDao.contarPorCultivo(cultivoId)
    }
}

