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

class KeepAliveLoginService : Service() {
    private val client = OkHttpClient()
    private var keepAliveJob: Timer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val FORTI_LOGIN_URL = "http://10.250.209.251:1000/login?05"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop_service") {
            stopSelf()
            return START_NOT_STICKY
        }

        val username = intent?.getStringExtra("username") ?: ""
        val password = intent?.getStringExtra("password") ?: ""

        startForeground(NOTIFICATION_ID, createNotification())
        loginAndStartKeepAlive(username, password)

        return START_STICKY
    }

    private fun loginAndStartKeepAlive(username: String, password: String) {
        serviceScope.launch {
            if(isActive) {
                keepAliveJob?.cancel()
                keepAliveJob = Timer()
                keepAliveJob?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val magic = extractMagic()
                                val loginResponse = login(magic, username, password)
                                Log.d("loginResponse", loginResponse.message)
                                // Yet to handle Failed Login Response -------------------
                                val keepAliveUrl =
                                    parseKeepAliveUrl(loginResponse.body?.string() ?: "")

                                Log.d("URL", keepAliveUrl)
                            } catch (e: Exception) {
                                Log.d("Login Failed", e.message.toString())
                            }
                        }
                    }
                }, 0, 10 * 1000) // Set this to run every 2 hours (For Testing set to 10 seconds)
            }
        }
    }

    private fun extractMagic(): String {
        val request = Request.Builder()
            .url(FORTI_LOGIN_URL)
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

    private fun login(magic: String, username: String, password: String): Response {
        val formBody = FormBody.Builder()
            .add("4Tredir", FORTI_LOGIN_URL)
            .add("magic", magic)
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(FORTI_LOGIN_URL)
            .post(formBody)
            .build()

        return client.newCall(request).execute()
    }

    private fun parseKeepAliveUrl(html: String): String {
        val keepAliveUrl = Jsoup.parse(html).select("script").firstOrNull()?.data()
            ?.substringAfter("window.location.replace('")
            ?.substringBefore("');")

        return keepAliveUrl ?: throw IOException("Failed to parse keep-alive URL")
    }

    private fun createNotification(): Notification {
        val channelId = "KeepAliveLoginServiceChannel"
        val channelName = "Keep Alive Login Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, KeepAliveLoginService::class.java)
        stopIntent.action = "stop_service"
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle("Auto Login Turned On")
            .setContentText("Keep this notification to keep this service on")
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)

        return notificationBuilder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        keepAliveJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
