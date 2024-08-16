package com.orm.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    fun isNetworkError(context: Context): Boolean {
        if (!isNetworkAvailable(context)) {
            MaterialAlertDialogBuilder(context)
                .setTitle("네트워크 연결 오류")
                .setMessage("인터넷 연결을 확인해주세요.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }.show()

            return true
        }
        return false
    }
}