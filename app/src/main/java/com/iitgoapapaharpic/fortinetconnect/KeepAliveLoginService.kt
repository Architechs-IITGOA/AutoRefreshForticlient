package com.iitgoapapaharpic.fortinetconnect

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
import java.util.*
import android.app.PendingIntent
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

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

        Log.d("Daddy", "in start command")

        val username = intent?.getStringExtra("username") ?: ""
        val password = intent?.getStringExtra("password") ?: ""

        Log.d("Daddy", username)
        Log.d("Daddy", password)

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
                        Log.d("Daddy", "in run")
                        CoroutineScope(Dispatchers.IO).launch {
                            val magic = extractMagic()

                            Log.d("Daddy", "after magic")
                            Log.d("Daddy", magic.toString())
                            if(magic === null){
                                showToast("Not connected to IIT Goa WiFi!")
                                stopSelf()
                                keepAliveJob?.cancel()
                                serviceScope.cancel()
                            }else{
                                val loginResponse = login(magic, username, password)

                                Log.d("Daddy", "login resp")

                                if(loginResponse == null){
                                    showToast("Please Try again!")
                                    stopSelf()
                                    keepAliveJob?.cancel()
                                    serviceScope.cancel()
                                }else{
                                    val keepAliveUrl =
                                        parseKeepAliveUrl(loginResponse.body?.string() ?: "")
                                    Log.d("URLisHere", keepAliveUrl.toString())
                                    Log.d("Daddy", keepAliveUrl.toString())

                                    Log.d("Daddy", "daddy ka url")

                                    if(keepAliveUrl == null){
                                        showToast("Please enter correct credentials")
                                        stopSelf()

                                        keepAliveJob?.cancel()
                                        serviceScope.cancel()
                                    }
                                }
                            }
                        }
                    }
                }, 0,
                    1000 * 60 * 60 * 2
                ) // Set this to run every 2 hours (For Testing set to 30 seconds)
            }
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractMagic(): String? {
        val request = Request.Builder()
            .url(FORTI_LOGIN_URL)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {

                    val html = response.body?.string()
                    return Jsoup.parse(html).select("input[name=magic]").attr("value")
                } else {
                    return null
                }
            }
        }catch (e: Exception){
            return null
        }
    }

    private fun login(magic: String, username: String, password: String): Response? {
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

        try {
            return client.newCall(request).execute()
        }catch (e: Exception){
            return null
        }
    }

    private fun parseKeepAliveUrl(html: String): String? {
        val keepAliveUrl = Jsoup.parse(html).select("script").firstOrNull()?.data()
            ?.substringAfter("\"")?.substringBefore("\"")

        return keepAliveUrl
    }

    private fun createNotification(): Notification {
        val channelId = "KeepAliveLoginServiceChannel"
        val channelName = "Keep Alive Login Service"

        Log.d("Daddy", "in create notif")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            Log.d("Daddy", "in sdk > O")
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, KeepAliveLoginService::class.java)
        stopIntent.action = "stop_service"
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        Log.d("Daddy", "b4 notification builder")

        val notificationBuilder = Notification.Builder(this, channelId)
            .setContentTitle("AutoConnect Turned On")
            .setContentText("You no more need relogin to Fortinet.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_stop, "Stop AutoConnect", stopPendingIntent)

        Log.d("Daddy", notificationBuilder.toString())
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
