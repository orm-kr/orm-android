package com.orm.data.model.board

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Board(
    val boardId: Int,
    val title: String,
    val content: String,
    val userId: Int,
    val userNickname: String,
    val commentCount: Int,
    val comments: List<Comment>,
    val hit: Int,
    val createdAt: String,
    val lastModifiedAt: String,
    val imgSrcs: List<BoardImgSrc>
) : Parcelable {
    companion object {
        fun getTimeAgo(lastModifiedAt: String): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val createdDateTime = LocalDateTime.parse(lastModifiedAt, formatter)
            val now = LocalDateTime.now()
            val duration = Duration.between(createdDateTime, now)

            return when {
                duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
                duration.toHours() < 24 -> "${duration.toHours()}시간 전"
                duration.toDays() < 7 -> "${duration.toDays()}일 전"
                else -> lastModifiedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
        }

    }

    fun getTruncatedCreatedAt(): String {
        return getTimeAgo(lastModifiedAt)
    }
}