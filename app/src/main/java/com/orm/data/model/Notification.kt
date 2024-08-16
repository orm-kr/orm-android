package com.orm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.messaging.RemoteMessage
import com.orm.data.model.recycler.RecyclerViewNotificationItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "notification")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clubId: Int,
    val clubName: String? = null,
    val userId: Int? = null,
    val userName: String? = null,
    val clubImageSrc: String?,
    val alertType: String,
    val title: String,
    val message: String,
    val time: LocalDateTime,
    val boardId: Int? = null,
) {
    companion object {
        fun toNotificationData(remoteMessage: RemoteMessage, title: String, message: String): Notification {
            val notificationData = remoteMessage.data
            return Notification(
                clubId = notificationData["clubId"]!!.toInt(),
                userId = notificationData["userId"]?.toInt(),
                title = title,
                message = message,
                clubName = notificationData["clubName"],
                userName = notificationData["userName"],
                alertType = notificationData["alertType"]!!,
                clubImageSrc = notificationData["clubImageSrc"] ?: "",
                time = LocalDateTime.now(),
                boardId = notificationData["boardId"]?.toInt(),
            )
        }
        fun toRecyclerViewNotificationItem(notification: Notification): RecyclerViewNotificationItem{
            return RecyclerViewNotificationItem(
                imageSrc = notification.clubImageSrc ?: "",
                title = notification.title,
                subTitle = notification.message,
                date = getTimeAgo(notification.time),
            )
        }
        fun getTimeAgo(notificationTime: LocalDateTime): String {
            val now = LocalDateTime.now()
            val duration = Duration.between(notificationTime, now)

            return when {
                duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
                duration.toHours() < 24 -> "${duration.toHours()}시간 전"
                duration.toDays() < 7 -> "${duration.toDays()}일 전"
                else -> notificationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
        }
    }
}
