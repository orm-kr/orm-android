package com.orm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orm.data.model.Point
import com.orm.data.model.Trail

@Dao
interface TrailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createTrail(trail: Trail): Long

    @Query("SELECT * FROM trail WHERE id = :id")
    suspend fun getTrail(id: Int): Trail

    @Query("DELETE FROM trail")
    suspend fun deleteAllTrails()
}