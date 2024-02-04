package com.oussama.portfolio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oussama.portfolio.BaseApplication
import kotlinx.coroutines.flow.first

private const val PREFERENCES_NAME = "my_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class DataStoreRepository {

    suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        BaseApplication.INSTANCE.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        BaseApplication.INSTANCE.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun getString(key: String): String? {
        val preferencesKey = stringPreferencesKey(key)
        val preferences = BaseApplication.INSTANCE.dataStore.data.first()
        return preferences[preferencesKey]
    }

    suspend fun getInt(key: String): Int? {
        val preferencesKey = intPreferencesKey(key)
        val preferences = BaseApplication.INSTANCE.dataStore.data.first()
        return preferences[preferencesKey]
    }
}