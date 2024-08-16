package com.orm.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.orm.data.api.UserService
import com.orm.data.local.PreferencesKeys
import com.orm.data.model.User
import com.orm.util.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userService: UserService,
    @ApplicationContext private val context: Context,
) {
    suspend fun loginKakao(code: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val response = userService.loginKakao(code).execute()
                if (response.isSuccessful) {
                    saveAccessToken(response.headers().get("accessToken").toString())
                    response.body() ?: throw Exception("Login failed")
                } else {
                    throw Exception("Login failed ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error during Kakao login", e)
                null
            }
        }
    }

    suspend fun loginAuto(): User? {
        return withContext(Dispatchers.IO) {
            try {
                val response = userService.loginAuto().execute()
                if (response.isSuccessful) {
                    val body = response.body() ?: throw Exception("Login failed")

                    saveAccessToken(response.headers()["accessToken"].toString())
                    saveUserInfo(
                        User(
                            userId = body.userId,
                            imageSrc = body.imageSrc,
                            nickname = body.nickname,
                        )
                    )
                    body
                } else {
                    throw Exception(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error during auto login", e)
                null
            }
        }
    }

    suspend fun registerFirebaseToken(firebaseToken: String) {
        return withContext(Dispatchers.IO) {
            try {
                val response = userService.registerFirebaseToken(firebaseToken).execute()
                if (response.isSuccessful) {
                    Log.d("UserRepository", "registerFirebaseToken: $firebaseToken")
                } else {
                    throw Exception(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                Log.d("UserRepository", "Error during auto login", e)
            }
        }
    }

    private suspend fun saveAccessToken(accessToken: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.tokenString] = accessToken
            }
            Log.d("UserRepository", "saveAccessToken: $accessToken")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving access token", e)
        }
    }

    suspend fun getAccessToken(): String {
        return try {
            val accessToken: Flow<String> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.tokenString] ?: ""
            }
            Log.d("UserRepository", "getAccessToken: ${accessToken.first()}")
            accessToken.first()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting access token", e)
            ""
        }
    }

    suspend fun deleteAccessToken() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.tokenString)
                Log.d("UserRepository", preferences[PreferencesKeys.tokenString].toString())
            }
            Log.d("UserRepository", "deleteAccessToken")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting access token", e)
        }
    }

    suspend fun saveUserInfo(user: User) {
        try {
            Log.d("UserRepository", "saveUserInfo: $user")
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.userId] = user.userId
                preferences[PreferencesKeys.imageSrc] = user.imageSrc
                preferences[PreferencesKeys.nickname] = user.nickname

                if (!(user.gender == "male" && user.age == 23 && user.level == 1 && user.pushNotificationsEnabled)) {
                    preferences[PreferencesKeys.gender] = user.gender
                    preferences[PreferencesKeys.age] = user.age
                    preferences[PreferencesKeys.level] = user.level
                    preferences[PreferencesKeys.pushNotificationsEnabled] =
                        user.pushNotificationsEnabled
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving user info", e)
        }
    }

    suspend fun getUserInfo(): User? {
        return try {
            val userId: Flow<String> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.userId] ?: ""
            }

            val imageSrc: Flow<String> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.imageSrc] ?: ""
            }

            val nickname: Flow<String> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.nickname] ?: ""

            }

            val gender: Flow<String> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.gender] ?: "male"
            }

            val age: Flow<Int> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.age] ?: 23
            }

            val level: Flow<Int> = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.level] ?: 1
            }

            val pushNotificationsEnabled: Flow<Boolean> =
                context.dataStore.data.map { preferences ->
                    preferences[PreferencesKeys.pushNotificationsEnabled] ?: true
                }

            User(
                userId.first(),
                imageSrc.first(),
                nickname.first(),
                gender.first(),
                age.first(),
                level.first(),
                pushNotificationsEnabled.first()
            )
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user info", e)
            null
        }
    }

    suspend fun deleteUserInfo() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.userId)
                preferences.remove(PreferencesKeys.imageSrc)
                preferences.remove(PreferencesKeys.nickname)
                preferences.remove(PreferencesKeys.gender)
                preferences.remove(PreferencesKeys.age)
                preferences.remove(PreferencesKeys.level)
                preferences.remove(PreferencesKeys.pushNotificationsEnabled)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user info", e)
        }

        try {
            withContext(Dispatchers.IO) {
                userService.deleteUser().execute()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error during user deletion", e)
        }
    }
}
