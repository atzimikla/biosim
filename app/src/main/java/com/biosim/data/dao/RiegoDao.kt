package com.biosim.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosim.data.entity.RiegoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con riegos.
 * 
 * ¿Por qué usar Flow en las consultas de lectura?
 * - Reactividad: la UI se actualiza automáticamente cuando cambian los datos.
 * - No necesitas llamar manualmente a "refrescar".
 * - Room emite nuevos valores cada vez que la tabla cambia.
 * 
 * ¿Por qué suspend en las operaciones de escritura?
 * - Las operaciones de BD deben ejecutarse fuera del hilo principal.
 * - suspend permite usar coroutines de forma limpia.
 */
@Dao
interface RiegoDao {

    /**
     * Obtiene todos los riegos ordenados por fecha (más recientes primero).
     */
    @Query("SELECT * FROM riegos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<RiegoEntity>>

    /**
     * Obtiene riegos de un cultivo específico.
     */
    @Query("SELECT * FROM riegos WHERE cultivoId = :cultivoId ORDER BY fecha DESC")
    fun obtenerPorCultivo(cultivoId: Int): Flow<List<RiegoEntity>>

    /**
     * Obtiene un riego por ID.
     */
    @Query("SELECT * FROM riegos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): RiegoEntity?

    /**
     * Obtiene riegos en un rango de fechas.
     */
    @Query("SELECT * FROM riegos WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun obtenerPorRangoFechas(desde: Long, hasta: Long): Flow<List<RiegoEntity>>

    /**
     * Obtiene el último riego de un cultivo.
     */
    @Query("SELECT * FROM riegos WHERE cultivoId = :cultivoId ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerUltimoRiego(cultivoId: Int): RiegoEntity?

    /**
     * Suma total de agua usada en un cultivo.
     */
    @Query("SELECT COALESCE(SUM(cantidadMl), 0) FROM riegos WHERE cultivoId = :cultivoId")
    suspend fun obtenerTotalAgua(cultivoId: Int): Int

    /**
     * Cuenta riegos por método.
     */
    @Query("SELECT COUNT(*) FROM riegos WHERE metodo = :metodo")
    suspend fun contarPorMetodo(metodo: String): Int

    /**
     * Inserta un nuevo riego.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(riego: RiegoEntity): Long

    /**
     * Inserta múltiples riegos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(riegos: List<RiegoEntity>)

    /**
     * Actualiza un riego existente.
     */
    @Update
    suspend fun actualizar(riego: RiegoEntity)

    /**
     * Elimina un riego.
     */
    @Delete
    suspend fun eliminar(riego: RiegoEntity)

    /**
     * Elimina un riego por ID.
     */
    @Query("DELETE FROM riegos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    /**
     * Elimina todos los riegos de un cultivo.
     */
    @Query("DELETE FROM riegos WHERE cultivoId = :cultivoId")
    suspend fun eliminarPorCultivo(cultivoId: Int)
}

