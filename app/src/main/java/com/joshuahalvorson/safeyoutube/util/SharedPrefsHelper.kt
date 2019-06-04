package com.joshuahalvorson.safeyoutube.util

import android.content.SharedPreferences

class SharedPrefsHelper(private val sharedPrefs: SharedPreferences?) {
    companion object{
        const val PREFERENCE_FILE_KEY = "safeyoutube_shared_prefs"
        const val ACCOUNT_KEY = "account_password"
        const val AGE_RANGE_KEY = "age_range"
        const val DARK_MODE_KEY = "dark_mode"
        const val ACCOUNT_PLAYLISTS_KEY = "account_playlists"
        const val ACCOUNT_NAME_KEY = "account_name"
        const val CURRENT_PLAYLIST_KEY = "current_playlist"
    }

    fun put(key: String, value: String?) {
        sharedPrefs?.edit()?.putString(key, value)?.apply()
    }

    fun put(key: String, value: Int) {
        sharedPrefs?.edit()?.putInt(key, value)?.apply()
    }

    fun get(key: String, defaultValue: String?): String? {
        return sharedPrefs?.getString(key, defaultValue)
    }

    fun get(key: String, defaultValue: Int): Int? {
        return sharedPrefs?.getInt(key, defaultValue)
    }

    fun remove(key: String){
        sharedPrefs?.edit()?.remove(key)?.apply()
    }
}