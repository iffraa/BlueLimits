package com.app.bluelimits.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.app.bluelimits.model.VisitorDetail


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

    fun saveList(visitorDetails: ArrayList<VisitorDetail>, key: String, context: Context) {
        var tinyDB : TinyDB = TinyDB(context)
        val visitorObjects = ArrayList<Any>()

        for (a in visitorDetails) {
            visitorObjects.add(a as Any)
        }

        tinyDB.putListObject(key, visitorObjects)


    }

    fun getList(context: Context, key: String): java.util.ArrayList<VisitorDetail>? {
        var tinyDB : TinyDB = TinyDB(context)
        return tinyDB.getListObject(key, VisitorDetail.javaClass) as java.util.ArrayList<VisitorDetail>

    }
}