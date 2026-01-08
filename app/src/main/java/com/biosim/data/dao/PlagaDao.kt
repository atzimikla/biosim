package com.biosim.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosim.data.entity.PlagaInspeccionEntity
import com.biosim.data.entity.PlagaTratamientoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con inspecciones y tratamientos de plagas.
 */
@Dao
interface PlagaDao {

    // ══════════════════════════════════════════════════════════════
    // INSPECCIONES
    // ══════════════════════════════════════════════════════════════

    @Query("SELECT * FROM plaga_inspecciones ORDER BY fecha DESC")
    fun obtenerTodasInspecciones(): Flow<List<PlagaInspeccionEntity>>

    @Query("SELECT * FROM plaga_inspecciones WHERE cultivoId = :cultivoId ORDER BY fecha DESC")
    fun obtenerInspeccionesPorCultivo(cultivoId: Int): Flow<List<PlagaInspeccionEntity>>

    @Query("SELECT * FROM plaga_inspecciones WHERE id = :id")
    suspend fun obtenerInspeccionPorId(id: Int): PlagaInspeccionEntity?

    @Query("SELECT * FROM plaga_inspecciones WHERE id = :id")
    fun obtenerInspeccionPorIdFlow(id: Int): Flow<PlagaInspeccionEntity?>

    @Query("SELECT * FROM plaga_inspecciones WHERE resuelta = 0 ORDER BY fecha DESC")
    fun obtenerInspeccionesActivas(): Flow<List<PlagaInspeccionEntity>>

    @Query("SELECT COUNT(*) FROM plaga_inspecciones WHERE cultivoId = :cultivoId AND resuelta = 0")
    suspend fun contarInspeccionesActivasPorCultivo(cultivoId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInspeccion(inspeccion: PlagaInspeccionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInspecciones(inspecciones: List<PlagaInspeccionEntity>)

    @Update
    suspend fun actualizarInspeccion(inspeccion: PlagaInspeccionEntity)

    @Query("UPDATE plaga_inspecciones SET resuelta = :resuelta WHERE id = :id")
    suspend fun marcarResuelta(id: Int, resuelta: Boolean)

    @Query("DELETE FROM plaga_inspecciones WHERE id = :id")
    suspend fun eliminarInspeccionPorId(id: Int)

    // ══════════════════════════════════════════════════════════════
    // TRATAMIENTOS
    // ══════════════════════════════════════════════════════════════

    @Query("SELECT * FROM plaga_tratamientos ORDER BY fechaAplicacion DESC")
    fun obtenerTodosTratamientos(): Flow<List<PlagaTratamientoEntity>>

    @Query("SELECT * FROM plaga_tratamientos WHERE inspeccionId = :inspeccionId ORDER BY fechaAplicacion DESC")
    fun obtenerTratamientosPorInspeccion(inspeccionId: Int): Flow<List<PlagaTratamientoEntity>>

    @Query("SELECT * FROM plaga_tratamientos WHERE id = :id")
    suspend fun obtenerTratamientoPorId(id: Int): PlagaTratamientoEntity?

    @Query("SELECT COUNT(*) FROM plaga_tratamientos WHERE inspeccionId = :inspeccionId")
    suspend fun contarTratamientosPorInspeccion(inspeccionId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTratamiento(tratamiento: PlagaTratamientoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTratamientos(tratamientos: List<PlagaTratamientoEntity>)

    @Update
    suspend fun actualizarTratamiento(tratamiento: PlagaTratamientoEntity)

    @Query("UPDATE plaga_tratamientos SET efectividad = :efectividad WHERE id = :id")
    suspend fun actualizarEfectividad(id: Int, efectividad: String)

    @Query("DELETE FROM plaga_tratamientos WHERE id = :id")
    suspend fun eliminarTratamientoPorId(id: Int)

    @Query("DELETE FROM plaga_tratamientos WHERE inspeccionId = :inspeccionId")
    suspend fun eliminarTratamientosPorInspeccion(inspeccionId: Int)
}

