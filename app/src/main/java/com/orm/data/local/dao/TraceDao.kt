package com.orm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orm.data.model.Trace

@Dao
interface TraceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrace(trace: Trace): Long

    @Delete
    suspend fun deleteTrace(trace: Trace): Int

    @Query("SELECT * FROM trace")
    suspend fun getAllTraces(): List<Trace>

    @Query("SELECT * FROM trace WHERE localId = :id")
    suspend fun getTrace(id: Int): Trace

    @Query("DELETE FROM trace")
    suspend fun deleteAllTraces()
}