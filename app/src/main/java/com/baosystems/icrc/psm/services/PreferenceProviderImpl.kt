package com.baosystems.icrc.psm.services

import android.content.Context
import android.content.SharedPreferences
import com.baosystems.icrc.psm.commons.Constants

class PreferenceProviderImpl(context: Context): PreferenceProvider {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.SHARED_PREFS, Context.MODE_PRIVATE)

    override fun sharedPreferences(): SharedPreferences {
        return sharedPreferences
    }

    override fun saveUserCredentials(serverUrl: String, userName: String, password: String) {
        with(sharedPreferences.edit()) {
            putString(Constants.SERVER_URL, serverUrl)
            putString(Constants.USERNAME, userName)

            if (password.isNotEmpty())
                putString(Constants.PASSWORD, password)

            apply()
        }
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    override fun setValue(key: String, value: Any?) {
        value?.let {
            when(it) {
                is String -> {
                    sharedPreferences.edit().putString(key, it).apply()
                }
                is Boolean -> {
                    sharedPreferences.edit().putBoolean(key, it).apply()
                }
                is Int -> {
                    sharedPreferences.edit().putInt(key, it).apply()
                }
                is Long -> {
                    sharedPreferences.edit().putLong(key, it).apply()
                }
                is Float -> {
                    sharedPreferences.edit().putFloat(key, it).apply()
                }
                is Set<*> -> {
                    sharedPreferences.edit().putStringSet(key, it as Set<String>).apply()
                }
                else -> return
            }
        } ?: run {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    override fun removeValue(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun contains(vararg keys: String): Boolean {
        return keys.all {
            sharedPreferences.contains(it)
        }
    }

    override fun getString(key: String, default: String?): String? {
        return sharedPreferences.getString(key, default)
    }

    override fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    override fun getLong(key: String, default: Long): Long {
        return sharedPreferences.getLong(key, default)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun getFloat(key: String, default: Float): Float {
        return sharedPreferences.getFloat(key, default)
    }
}