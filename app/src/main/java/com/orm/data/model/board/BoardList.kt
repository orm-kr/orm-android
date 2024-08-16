package com.orm.data.model.board

import android.os.Parcelable
import com.orm.data.model.Notification
import com.orm.data.model.Notification.Companion
import com.orm.data.model.recycler.RecyclerViewBoardItem
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class BoardList(
    val boardId : Int,
    val title : String,
    val userId : Int,
    val userNickname : String,
    val commentCount : Int,
    val hit : Int,
    val createdAt : String,
    val lastModifiedAt: String,
): Parcelable {
    companion object {

        fun toRecyclerViewBoardItem(boardList: BoardList): RecyclerViewBoardItem {
            return RecyclerViewBoardItem(
                id = boardList.boardId,
                title = boardList.title,
                userNickname = boardList.userNickname,
                commentCount = boardList.commentCount,
                hit = boardList.hit,
                createdAt = getTimeAgo(boardList.lastModifiedAt)
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