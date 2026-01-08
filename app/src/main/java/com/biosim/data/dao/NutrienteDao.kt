package com.biosim.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosim.data.entity.NutrienteAplicacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con aplicaciones de nutrientes.
 */
@Dao
interface NutrienteDao {

    /**
     * Obtiene todas las aplicaciones ordenadas por fecha (más recientes primero).
     */
    @Query("SELECT * FROM nutrientes_aplicaciones ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<NutrienteAplicacionEntity>>

    /**
     * Obtiene aplicaciones de un cultivo específico.
     */
    @Query("SELECT * FROM nutrientes_aplicaciones WHERE cultivoId = :cultivoId ORDER BY fecha DESC")
    fun obtenerPorCultivo(cultivoId: Int): Flow<List<NutrienteAplicacionEntity>>

    /**
     * Obtiene una aplicación por ID.
     */
    @Query("SELECT * FROM nutrientes_aplicaciones WHERE id = :id")
    suspend fun obtenerPorId(id: Int): NutrienteAplicacionEntity?

    /**
     * Obtiene aplicaciones por tipo de nutriente.
     */
    @Query("SELECT * FROM nutrientes_aplicaciones WHERE tipoNutriente = :tipo ORDER BY fecha DESC")
    fun obtenerPorTipo(tipo: String): Flow<List<NutrienteAplicacionEntity>>

    /**
     * Obtiene la última aplicación de un cultivo.
     */
    @Query("SELECT * FROM nutrientes_aplicaciones WHERE cultivoId = :cultivoId ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerUltimaAplicacion(cultivoId: Int): NutrienteAplicacionEntity?

    /**
     * Suma total de nutrientes aplicados a un cultivo (en gramos).
     */
    @Query("SELECT COALESCE(SUM(cantidadGramos), 0) FROM nutrientes_aplicaciones WHERE cultivoId = :cultivoId")
    suspend fun obtenerTotalAplicado(cultivoId: Int): Int

    /**
     * Cuenta aplicaciones por tipo de nutriente.
     */
    @Query("SELECT COUNT(*) FROM nutrientes_aplicaciones WHERE tipoNutriente = :tipo")
    suspend fun contarPorTipo(tipo: String): Int

    /**
     * Inserta una nueva aplicación.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(aplicacion: NutrienteAplicacionEntity): Long

    /**
     * Inserta múltiples aplicaciones.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(aplicaciones: List<NutrienteAplicacionEntity>)

    /**
     * Actualiza una aplicación existente.
     */
    @Update
    suspend fun actualizar(aplicacion: NutrienteAplicacionEntity)

    /**
     * Elimina una aplicación.
     */
    @Delete
    suspend fun eliminar(aplicacion: NutrienteAplicacionEntity)

    /**
     * Elimina una aplicación por ID.
     */
    @Query("DELETE FROM nutrientes_aplicaciones WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    /**
     * Elimina todas las aplicaciones de un cultivo.
     */
    @Query("DELETE FROM nutrientes_aplicaciones WHERE cultivoId = :cultivoId")
    suspend fun eliminarPorCultivo(cultivoId: Int)
}

