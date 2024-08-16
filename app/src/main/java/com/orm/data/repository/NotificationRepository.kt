package com.orm.data.repository

import com.orm.data.local.dao.NotificationDao
import com.orm.data.model.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
) {
    suspend fun getAllNotifications(): List<Notification> {
        return withContext(Dispatchers.IO) {
            notificationDao.getAllNotifications()
        }
    }

    suspend fun insertNotification(notification: Notification): Long {
        return withContext(Dispatchers.IO) {
            notificationDao.insertNotification(notification)
        }
    }

    suspend fun deleteNotification(notification: Notification): Int {
        return withContext(Dispatchers.IO) {
            notificationDao.deleteNotification(notification)
        }
    }

    suspend fun deleteAllNotifications() {
        withContext(Dispatchers.IO) {
            notificationDao.deleteAllNotifications()
        }
    }
}