package com.orm.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.orm.BuildConfig


// datastore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = BuildConfig.DATASTORE_NAME,
)