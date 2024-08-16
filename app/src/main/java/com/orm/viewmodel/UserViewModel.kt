package com.orm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.orm.data.model.User
import com.orm.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> get() = _token

    private val _isTokenLoading = MutableLiveData<Boolean>()
    val isTokenLoading: LiveData<Boolean> get() = _isTokenLoading

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var _isDeleteUser = MutableLiveData<Boolean>()
    val isDeleteUser: LiveData<Boolean> get() = _isDeleteUser

    private var isTokenSent = false

    init {
        getAccessToken()
        getUserInfo()
    }

    fun loginKakao(code: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.loginKakao(code)
                _user.postValue(user)
                Log.d("UserViewModel", "User: $user")
                getAccessToken()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Login failed: ${e.message}", e)
            }
        }
    }

    fun loginAuto() {
        Log.d("UserViewModel", "loginAuto")
        viewModelScope.launch {
            try {
                val user = userRepository.loginAuto()
                _user.postValue(user)
                getAccessToken()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Login failed: ${e.message}", e)
            }
        }
    }

    fun getAccessToken() {
        Log.d("UserViewModel", "getAccessToken")
        viewModelScope.launch {
            try {
                _isTokenLoading.postValue(true)
                val token: String = userRepository.getAccessToken()
                _token.postValue(token)
                _isTokenLoading.postValue(false)
            } catch (e: Exception) {
                Log.e("UserViewModel", "getAccessToken failed: ${e.message}", e)
                _token.postValue("")
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val user = userRepository.getUserInfo()
            _user.postValue(user)
            _isLoading.postValue(false)
        }
    }

    fun updateUserInfo(user: User) {
        viewModelScope.launch {
            userRepository.saveUserInfo(user)
            _user.postValue(user)
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            _isDeleteUser.postValue(false)
            userRepository.deleteUserInfo()
            userRepository.deleteAccessToken()
            _isDeleteUser.postValue(true)
        }
    }

    fun registerFirebaseToken(firebaseToken: String) {
        viewModelScope.launch {
            Log.d("UserViewModel", "registerFirebaseToken")
            userRepository.registerFirebaseToken(firebaseToken)
        }
    }

    fun getFirebaseToken() {
        if (isTokenSent) return

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FirebaseMessaging", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            Log.d("firebase token", task.result)

            registerFirebaseToken(task.result.toString())

            isTokenSent = true
        })
    }

}