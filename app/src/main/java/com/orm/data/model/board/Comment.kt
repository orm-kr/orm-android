package com.orm.data.model.board

import android.os.Parcelable
import com.orm.data.model.recycler.RecyclerViewCommentItem
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Comment(
    val commentId: Int,
    val userId: Int,
    val userNickname: String,
    val content: String,
    val createdAt: String,
    val lastModifiedAt: String,
) : Parcelable {
    companion object {

        fun toRecyclerViewCommentItem(comment: Comment): RecyclerViewCommentItem {
            return RecyclerViewCommentItem(
                commentId = comment.commentId,
                userId = comment.userId,
                userNickname = comment.userNickname,
                content = comment.content,
                createdAt = getTimeAgo(comment.lastModifiedAt),
            )
        }

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
}
