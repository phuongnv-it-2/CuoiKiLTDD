package com.project24itb156.gglens.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val DISPLAY_NAME_KEY = stringPreferencesKey("user_display_name")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY] != null
        }

    suspend fun saveSession(token: String, userId: Int, email: String, displayName: String?) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[EMAIL_KEY] = email
            if (displayName != null) {
                preferences[DISPLAY_NAME_KEY] = displayName
            }
        }
    }

    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY]
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
