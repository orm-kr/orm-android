package com.orm.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.orm.data.model.club.Club

@Dao
interface ClubDao {
    @Query("SELECT * FROM club")
    suspend fun getAllClubs(): List<Club>
}