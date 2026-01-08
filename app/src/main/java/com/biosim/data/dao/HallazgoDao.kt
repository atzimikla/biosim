package com.biosim.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosim.data.entity.HallazgoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con hallazgos de cultivo.
 */
@Dao
interface HallazgoDao {

    @Query("SELECT * FROM hallazgos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<HallazgoEntity>>

    @Query("SELECT * FROM hallazgos WHERE cultivoId = :cultivoId ORDER BY fecha DESC")
    fun obtenerPorCultivo(cultivoId: Int): Flow<List<HallazgoEntity>>

    @Query("SELECT * FROM hallazgos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): HallazgoEntity?

    @Query("SELECT * FROM hallazgos WHERE id = :id")
    fun obtenerPorIdFlow(id: Int): Flow<HallazgoEntity?>

    @Query("SELECT COUNT(*) FROM hallazgos WHERE cultivoId = :cultivoId")
    suspend fun contarPorCultivo(cultivoId: Int): Int

    @Query("SELECT * FROM hallazgos WHERE tipoHallazgo = :tipo ORDER BY fecha DESC")
    fun obtenerPorTipo(tipo: String): Flow<List<HallazgoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(hallazgo: HallazgoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(hallazgos: List<HallazgoEntity>)

    @Update
    suspend fun actualizar(hallazgo: HallazgoEntity)

    @Query("DELETE FROM hallazgos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("DELETE FROM hallazgos WHERE cultivoId = :cultivoId")
    suspend fun eliminarPorCultivo(cultivoId: Int)
}

