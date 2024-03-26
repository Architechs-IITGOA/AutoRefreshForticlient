package com.iitgoapapaharpic.upfortfrgrnd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log


class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
//            // Check if there is an active network connection
//            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
////            val activeNetwork = cm.activeNetworkInfo
////            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
////            if (isConnected) {
////                // Start your service
////                val serviceIntent = Intent(context, YourService::class.java)
////                context.startService(serviceIntent)
////            }
//
//        }
        Log.d("NetworkCheck", isNetworkAvailable(context).toString())
    }
    private fun isNetworkAvailable(context: Context) =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
    }
}