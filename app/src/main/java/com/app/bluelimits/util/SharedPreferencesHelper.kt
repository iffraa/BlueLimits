package com.app.bluelimits.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.provider.Settings.Global.putLong
import androidx.core.content.edit
import com.app.bluelimits.model.FamilyMember
import com.google.gson.Gson




//import androidx.preference.PreferenceManager

class SharedPreferencesHelper {

    companion object {

        private const val PREF_TIME = "Pref time"
        private var prefs: SharedPreferences? = null

        @Volatile private var instance: SharedPreferencesHelper? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): SharedPreferencesHelper = instance ?: synchronized(LOCK) {
            instance ?: buildHelper(context).also {
                instance = it
            }
        }

        private fun buildHelper(context: Context) : SharedPreferencesHelper {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return SharedPreferencesHelper()
        }
    }

    fun saveData(data: String, key: String) {
        prefs?.edit(commit = true) {putString(key, data)}
    }

    fun getData(key: String) = prefs?.getString(key, "")

    fun clearPrefs() = prefs?.edit()?.clear()?.commit()


}