package com.biosim.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosim.data.entity.PlagaFotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con fotos de inspecciones de plagas.
 */
@Dao
interface PlagaFotoDao {

    @Query("SELECT * FROM plaga_fotos WHERE inspeccionId = :inspeccionId ORDER BY fecha DESC")
    fun obtenerPorInspeccion(inspeccionId: Int): Flow<List<PlagaFotoEntity>>

    @Query("SELECT * FROM plaga_fotos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): PlagaFotoEntity?

    @Query("SELECT * FROM plaga_fotos WHERE id = :id")
    fun obtenerPorIdFlow(id: Int): Flow<PlagaFotoEntity?>

    @Query("SELECT COUNT(*) FROM plaga_fotos WHERE inspeccionId = :inspeccionId")
    suspend fun contarPorInspeccion(inspeccionId: Int): Int

    @Query("SELECT COUNT(*) FROM plaga_fotos WHERE inspeccionId = :inspeccionId")
    fun contarPorInspeccionFlow(inspeccionId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM plaga_fotos WHERE inspeccionId = :inspeccionId AND latitud IS NOT NULL")
    suspend fun contarConUbicacion(inspeccionId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(foto: PlagaFotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(fotos: List<PlagaFotoEntity>)

    @Update
    suspend fun actualizar(foto: PlagaFotoEntity)

    @Query("DELETE FROM plaga_fotos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("DELETE FROM plaga_fotos WHERE inspeccionId = :inspeccionId")
    suspend fun eliminarPorInspeccion(inspeccionId: Int)
}

