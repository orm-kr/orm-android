package com.orm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orm.data.model.Notification
import com.orm.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

    fun getAllNotifications() {
        viewModelScope.launch {
            val notifications = notificationRepository.getAllNotifications()
            _notifications.postValue(notifications)
        }
    }

    fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notification)

            val updateNotifications = _notifications.value?.toMutableList()?.filter {
                it.id != notification.id
            } ?: mutableListOf()
            _notifications.postValue(updateNotifications)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
            _notifications.postValue(emptyList())
        }
    }
}