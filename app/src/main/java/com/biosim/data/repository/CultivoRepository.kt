package com.biosim.data.repository

import com.biosim.data.dao.CultivoDao
import com.biosim.data.entity.CultivoEntity
import com.biosim.model.Cultivo
import com.biosim.model.EstadoCultivo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar datos de cultivos.
 * Actúa como capa intermedia entre el DAO y el ViewModel.
 * Convierte entre Entity (Room) y Model (UI).
 */
class CultivoRepository(private val cultivoDao: CultivoDao) {

    /**
     * Obtiene todos los cultivos como Flow de modelos de UI.
     */
    val todosLosCultivos: Flow<List<Cultivo>> = cultivoDao.obtenerTodos().map { entities ->
        entities.map { it.toModel() }
    }

    /**
     * Obtiene un cultivo por ID.
     */
    suspend fun obtenerPorId(id: Int): Cultivo? {
        return cultivoDao.obtenerPorId(id)?.toModel()
    }

    /**
     * Obtiene un cultivo por ID como Flow.
     */
    fun obtenerPorIdFlow(id: Int): Flow<Cultivo?> {
        return cultivoDao.obtenerPorIdFlow(id).map { it?.toModel() }
    }

    /**
     * Busca cultivos por nombre.
     */
    fun buscarPorNombre(query: String): Flow<List<Cultivo>> {
        return cultivoDao.buscarPorNombre(query).map { entities ->
            entities.map { it.toModel() }
        }
    }

    /**
     * Obtiene cultivos por estado.
     */
    fun obtenerPorEstado(estado: EstadoCultivo): Flow<List<Cultivo>> {
        return cultivoDao.obtenerPorEstado(estado.name).map { entities ->
            entities.map { it.toModel() }
        }
    }

    /**
     * Inserta un nuevo cultivo.
     * @return El ID del cultivo insertado.
     */
    suspend fun insertar(cultivo: Cultivo): Long {
        return cultivoDao.insertar(cultivo.toEntity())
    }

    /**
     * Actualiza un cultivo existente.
     */
    suspend fun actualizar(cultivo: Cultivo) {
        cultivoDao.actualizar(cultivo.toEntity())
    }

    /**
     * Elimina un cultivo.
     */
    suspend fun eliminar(cultivo: Cultivo) {
        cultivoDao.eliminar(cultivo.toEntity())
    }

    /**
     * Elimina un cultivo por ID.
     */
    suspend fun eliminarPorId(id: Int) {
        cultivoDao.eliminarPorId(id)
    }

    /**
     * Actualiza el próximo riego.
     */
    suspend fun actualizarProximoRiego(id: Int, proximoRiego: String) {
        cultivoDao.actualizarProximoRiego(id, proximoRiego)
    }

    /**
     * Actualiza el estado de un cultivo.
     */
    suspend fun actualizarEstado(id: Int, estado: EstadoCultivo) {
        cultivoDao.actualizarEstado(id, estado.name)
    }

    /**
     * Cuenta el total de cultivos.
     */
    suspend fun contarTodos(): Int {
        return cultivoDao.contarTodos()
    }
}

// ══════════════════════════════════════════════════════════════
// Extensiones para convertir entre Entity y Model
// ══════════════════════════════════════════════════════════════

/**
 * Convierte CultivoEntity (Room) a Cultivo (UI Model).
 */
fun CultivoEntity.toModel(): Cultivo {
    return Cultivo(
        id = this.id,
        nombre = this.nombre,
        tipo = this.tipo,
        emoji = this.emoji,
        ubicacion = this.ubicacion,
        fechaSiembra = this.fechaSiembra,
        estado = try {
            EstadoCultivo.valueOf(this.estado)
        } catch (e: IllegalArgumentException) {
            EstadoCultivo.CRECIENDO // Estado por defecto si hay error
        },
        diasDesdeSimebra = this.diasDesdeSiembra,
        proximoRiego = this.proximoRiego
    )
}

/**
 * Convierte Cultivo (UI Model) a CultivoEntity (Room).
 */
fun Cultivo.toEntity(): CultivoEntity {
    return CultivoEntity(
        id = this.id,
        nombre = this.nombre,
        tipo = this.tipo,
        emoji = this.emoji,
        ubicacion = this.ubicacion,
        fechaSiembra = this.fechaSiembra,
        estado = this.estado.name,
        diasDesdeSiembra = this.diasDesdeSimebra,
        proximoRiego = this.proximoRiego
    )
}

