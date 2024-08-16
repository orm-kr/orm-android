package com.orm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orm.data.model.Notification

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("SELECT * FROM notification")
    suspend fun getAllNotifications(): List<Notification>

    @Delete
    suspend fun deleteNotification(notification: Notification): Int

    @Query("DELETE FROM notification")
    suspend fun deleteAllNotifications()

}