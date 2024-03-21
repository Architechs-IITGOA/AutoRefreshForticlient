package com.iitgoapapaharpic.upfortfrgrnd


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import android.app.PendingIntent
import android.util.Log

class KeepAliveService : Service() {
    private val client = OkHttpClient()
    private val fortiUrl = "http://10.250.209.251:1000/login?05"
    private var keepAliveJob: Timer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop_service") {
            stopSelf()
            return START_NOT_STICKY
        }

        val username = intent?.getStringExtra("username") ?: ""
        val password = intent?.getStringExtra("password") ?: ""

        if (username.isNotEmpty() && password.isNotEmpty()) {
            startForeground(NOTIFICATION_ID, createNotification())
            loginAndStartKeepAlive(username, password)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        keepAliveJob?.cancel()
        serviceScope.cancel()
    }

    private fun loginAndStartKeepAlive(username: String, password: String) {
        serviceScope.launch {
            while (isActive) {
                try {
                    val magic = extractMagic()
                    val loginResponse = login(magic, username, password)

                    val keepAliveUrl = parseKeepAliveUrl(loginResponse.body?.string() ?: "")
                    Log.d("URL", keepAliveUrl)
                    startKeepAlive(keepAliveUrl)
                } catch (e: Exception) {
                    // Handle login and keep-alive failure
                }
            }
        }
    }

    private fun extractMagic(): String {
        val request = Request.Builder()
            .url(fortiUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val html = response.body?.string()
                return Jsoup.parse(html).select("input[name=magic]").attr("value")
            } else {
                throw IOException("Failed to extract magic token")
            }
        }
    }

//    private fun login(magic: String, username: String, password: String): Response {
//        // Implementation to perform the login
//        return Response.Builder().build()
//    }
    private fun login(magic: String, username: String, password: String): Response {
        val formBody = FormBody.Builder()
            .add("4Tredir", fortiUrl)
            .add("magic", magic)
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(fortiUrl)
            .post(formBody)
            .build()

        return client.newCall(request).execute()
    }

//    private fun parseKeepAliveUrl(html: String): String {
//        // Implementation to parse the keep-alive URL from the HTML response
//        return ""
//    }
    private fun parseKeepAliveUrl(html: String): String {
        val keepAliveUrl = Jsoup.parse(html).select("script").firstOrNull()?.data()
            ?.substringAfter("window.location.replace('")
            ?.substringBefore("');")

        return keepAliveUrl ?: throw IOException("Failed to parse keep-alive URL")
    }


    private fun startKeepAlive(keepAliveUrl: String) {
        keepAliveJob?.cancel()
        keepAliveJob = Timer()
        keepAliveJob?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = Request.Builder()
                            .url(keepAliveUrl)
                            .build()

                        client.newCall(request).execute()
                    } catch (e: Exception) {
//                        Log.e("FortiClientApp", "Error: ${e.message}")
                    }
                }
            }
        }, 0, 12000000000) // 2 hours in milliseconds
    }

    private fun createNotification(): Notification {
        val channelId = "KeepAliveServiceChannel"
        val channelName = "Keep Alive Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, KeepAliveService::class.java)
        stopIntent.action = "stop_service"
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle("Keep Alive Service")
            .setContentText("Keeping the app alive in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)

        return notificationBuilder.build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//
//class KeepAliveService : Service() {
//
//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }
//}