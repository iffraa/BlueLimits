package com.app.bluelimits.util

import android.content.Context
import android.net.ConnectivityManager

/**
 * Utility methods for dealing with connectivity
 */
object ConnectivityUtils {
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
}