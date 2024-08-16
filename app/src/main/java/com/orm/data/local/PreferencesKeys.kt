package com.orm.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val tokenString = stringPreferencesKey("tokenStringPreferenceKey")
    val userId = stringPreferencesKey("userIdPreferenceKey")
    val nickname = stringPreferencesKey("nicknamePreferenceKey")
    val imageSrc = stringPreferencesKey("imageSrcPreferenceKey")
    val gender = stringPreferencesKey("genderPreferenceKey")
    val age = intPreferencesKey("agePreferenceKey")
    val level = intPreferencesKey("levelPreferenceKey")
    val pushNotificationsEnabled = booleanPreferencesKey("pushNotificationsEnabledPreferenceKey")
}