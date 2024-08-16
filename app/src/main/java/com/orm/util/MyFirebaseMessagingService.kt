package com.orm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.Constants
import com.google.firebase.messaging.Constants.MessageNotificationKeys
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orm.R
import com.orm.data.model.Notification
import com.orm.data.model.board.BoardList
import com.orm.data.model.club.Club
import com.orm.data.repository.BoardRepository
import com.orm.data.repository.ClubRepository
import com.orm.data.repository.NotificationRepository
import com.orm.data.repository.UserRepository
import com.orm.ui.board.BoardActivity
import com.orm.ui.board.BoardDetailActivity
import com.orm.ui.club.ClubActivity
import com.orm.ui.club.ClubDetailActivity
import com.orm.ui.club.ClubMemberActivity
import com.orm.ui.trace.TraceMeasureActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Firebase", "fcm token...... $token")
    }

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var clubRepository: ClubRepository

    @Inject
    lateinit var boardRepository: BoardRepository

    private lateinit var title: String
    private lateinit var message: String

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("Firebase", "message received...... ${remoteMessage.notification.toString()}")
        Log.d("notiTest", "msg ${remoteMessage.toString()}")
        Log.d("notiTest", "msg ${remoteMessage.data.toString()}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userRepository.getUserInfo()

                if (user?.pushNotificationsEnabled == true) {
                    showNotification(remoteMessage)
                } else {
                    Log.d("Firebase", "Push notifications are disabled for this user.")
                }

                val notification = Notification.toNotificationData(remoteMessage, title, message)
                notificationRepository.insertNotification(notification)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MyFirebaseMessagingService", "Error handling message", e)
            }
        }
    }

    private fun showNotification(notification: RemoteMessage) {
        CoroutineScope(Dispatchers.Main).launch {
            var intent: Intent? = Intent()
            val noti = Notification.toNotificationData(notification, title, message)
            Log.d("notiTest", noti.alertType)

            if (!LocationIntentService.isServiceRunning) {
                when (noti.alertType) {
                    "APPLICATION" -> {
                        intent =
                            Intent(this@MyFirebaseMessagingService, ClubMemberActivity::class.java)
                        val club = getClub(noti.clubId)
                        intent.putExtra("club", club)
                    }

                    "ACCEPTANCE" -> {
                        intent =
                            Intent(this@MyFirebaseMessagingService, ClubDetailActivity::class.java)
                        val club = getClub(noti.clubId)
                        intent.putExtra("club", club)
                    }

                    "REJECT" -> {
                        intent = Intent(this@MyFirebaseMessagingService, ClubActivity::class.java)
                    }

                    "EXPEL" -> {
                        intent = Intent(this@MyFirebaseMessagingService, ClubActivity::class.java)
                    }

                    "NEW_BOARD" -> {
                        intent = Intent(this@MyFirebaseMessagingService, BoardActivity::class.java)
                        val club = getClub(noti.clubId)
                        intent.putExtra("club", club)
                    }

                    "NEW_COMMENT" -> {
                        intent =
                            Intent(this@MyFirebaseMessagingService, BoardDetailActivity::class.java)
                        val boardList = getBoardList(noti.boardId!!)
                        val club = getClub(noti.clubId)
                        intent.putExtra("club", club)
                        intent.putExtra("boardList", boardList)
                    }
                }
            } else {
                intent = Intent(this@MyFirebaseMessagingService, TraceMeasureActivity::class.java)
            }

            intent!!.putExtra("back", true)
            val pIntent = PendingIntent.getActivity(
                this@MyFirebaseMessagingService,
                0,  // Request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = "orm"

            val notificationBuilder =
                NotificationCompat.Builder(this@MyFirebaseMessagingService, channelId)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.mipmap.ic_launcher_orm)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)

            getSystemService(NotificationManager::class.java).run {
                val channel =
                    NotificationChannel(channelId, "알림", NotificationManager.IMPORTANCE_HIGH)
                createNotificationChannel(channel)
                notify(Date().time.toInt(), notificationBuilder.build())
            }
        }
    }

    override fun handleIntent(intent: Intent?) {
        val remoteMessage = intent?.extras?.let { extras ->
            RemoteMessage.Builder("MyFirebaseMessagingService")
                .setMessageId(extras.getString("google.message_id")!!)
                .setData(extras.keySet().associateWith { extras.getString(it) ?: "" })
                .build()
        }

        remoteMessage?.notification?.let { notification ->
            title = notification.title.toString()
            message = notification.body.toString()
            Log.d("notiTest", "title ${title} message ${message}")
        }

        val new = intent?.apply {
            val temp = extras?.apply {
                remove(MessageNotificationKeys.ENABLE_NOTIFICATION)
                remove(keyWithOldPrefix(MessageNotificationKeys.ENABLE_NOTIFICATION))
            }
            replaceExtras(temp)
        }
        super.handleIntent(new)
    }

    suspend fun getBoardList(boardId: Int): BoardList {
        return withContext(Dispatchers.IO) {
            Log.d("notiTest", "!!!!! ${boardId}")
            val board = boardRepository.getBoards(boardId)
            BoardList(
                boardId = board!!.boardId,
                userId = board.userId,
                title = board.title,
                hit = board.hit,
                commentCount = board.commentCount,
                userNickname = board.userNickname,
                createdAt = board.createdAt,
                lastModifiedAt = board.lastModifiedAt,
            )
        }
    }

    suspend fun getClub(clubId: Int): Club {
        return withContext(Dispatchers.IO) {
            clubRepository.getClubById(clubId)!!
        }
    }

    private fun keyWithOldPrefix(key: String): String {
        if (!key.startsWith(MessageNotificationKeys.NOTIFICATION_PREFIX)) {
            return key
        }

        return key.replace(
            MessageNotificationKeys.NOTIFICATION_PREFIX,
            MessageNotificationKeys.NOTIFICATION_PREFIX_OLD
        )
    }
}