package com.biosim.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosim.data.entity.CultivoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones con cultivos.
 * Define las operaciones de base de datos para la tabla cultivos.
 */
@Dao
interface CultivoDao {

    /**
     * Obtiene todos los cultivos ordenados por fecha de creación (más recientes primero).
     * Retorna un Flow para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM cultivos ORDER BY fechaCreacion DESC")
    fun obtenerTodos(): Flow<List<CultivoEntity>>

    /**
     * Obtiene un cultivo por su ID.
     */
    @Query("SELECT * FROM cultivos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): CultivoEntity?

    /**
     * Obtiene un cultivo por su ID como Flow (para observar cambios).
     */
    @Query("SELECT * FROM cultivos WHERE id = :id")
    fun obtenerPorIdFlow(id: Int): Flow<CultivoEntity?>

    /**
     * Busca cultivos por nombre (búsqueda parcial).
     */
    @Query("SELECT * FROM cultivos WHERE nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
    fun buscarPorNombre(query: String): Flow<List<CultivoEntity>>

    /**
     * Obtiene cultivos filtrados por estado.
     */
    @Query("SELECT * FROM cultivos WHERE estado = :estado ORDER BY fechaCreacion DESC")
    fun obtenerPorEstado(estado: String): Flow<List<CultivoEntity>>

    /**
     * Obtiene cultivos filtrados por ubicación.
     */
    @Query("SELECT * FROM cultivos WHERE ubicacion = :ubicacion ORDER BY nombre ASC")
    fun obtenerPorUbicacion(ubicacion: String): Flow<List<CultivoEntity>>

    /**
     * Cuenta el total de cultivos.
     */
    @Query("SELECT COUNT(*) FROM cultivos")
    suspend fun contarTodos(): Int

    /**
     * Inserta un nuevo cultivo.
     * @return El ID del cultivo insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cultivo: CultivoEntity): Long

    /**
     * Inserta múltiples cultivos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(cultivos: List<CultivoEntity>)

    /**
     * Actualiza un cultivo existente.
     */
    @Update
    suspend fun actualizar(cultivo: CultivoEntity)

    /**
     * Elimina un cultivo.
     */
    @Delete
    suspend fun eliminar(cultivo: CultivoEntity)

    /**
     * Elimina un cultivo por su ID.
     */
    @Query("DELETE FROM cultivos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    /**
     * Elimina todos los cultivos.
     */
    @Query("DELETE FROM cultivos")
    suspend fun eliminarTodos()

    /**
     * Actualiza el próximo riego de un cultivo.
     */
    @Query("UPDATE cultivos SET proximoRiego = :proximoRiego, fechaActualizacion = :timestamp WHERE id = :id")
    suspend fun actualizarProximoRiego(id: Int, proximoRiego: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Actualiza el estado de un cultivo.
     */
    @Query("UPDATE cultivos SET estado = :estado, fechaActualizacion = :timestamp WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Incrementa los días desde siembra de un cultivo.
     */
    @Query("UPDATE cultivos SET diasDesdeSiembra = diasDesdeSiembra + 1, fechaActualizacion = :timestamp WHERE id = :id")
    suspend fun incrementarDias(id: Int, timestamp: Long = System.currentTimeMillis())
}

