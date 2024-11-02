package com.example.core.datastore



import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class EcomDataStore(private val context:Context) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("userid")

        private val Context.ecomDataStore by preferencesDataStore("ecom_datastore")
    }


    suspend fun clearUserId() {
        context.ecomDataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    suspend fun saveUserId(uid: String) {
        context.ecomDataStore.edit { preferences ->
            preferences[USER_ID_KEY] = uid
        }

    }

    val userId: Flow<String?> =
        context.ecomDataStore.data.catch { exc -> emit(emptyPreferences()) }
            .map { preferences -> preferences[USER_ID_KEY] }
}