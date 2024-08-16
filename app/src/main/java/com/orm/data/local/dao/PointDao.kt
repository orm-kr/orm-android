package com.orm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orm.data.model.Point

@Dao
interface PointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: Point): Long

    @Query("SELECT * FROM point WHERE trailId = :trailId")
    suspend fun getPointsByTrailId(trailId: Int): List<Point>
}