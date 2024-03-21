package com.iitgoapapaharpic.upfortfrgrnd


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iitgoapapaharpic.upfortfrgrnd.databinding.ActivityMainBinding
//import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.util.Timer

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val fortiUrl = "http://10.250.209.251:1000/login?05"

//    private var keepAliveJob: Timer? = null
//    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginAndStartKeepAlive(username, password)
            } else {
                // Show an error message or handle the case when username or password is empty
            }
        }

        binding.stopServiceButton.setOnClickListener {
            stopKeepAliveService()
        }
    }

    private fun loginAndStartKeepAlive(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val magic = extractMagic()
                val loginResponse = login(magic, username, password)
                val keepAliveUrl = parseKeepAliveUrl(loginResponse.body?.string() ?: "")
                startKeepAliveService(username, password)
            } catch (e: Exception) {
                Log.e("FortiClientApp", "Error: ${e.message}")
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

    private fun parseKeepAliveUrl(html: String): String {
        val keepAliveUrl = Jsoup.parse(html).select("script").firstOrNull()?.data()
            ?.substringAfter("window.location.replace('")
            ?.substringBefore("');")

        return keepAliveUrl ?: throw IOException("Failed to parse keep-alive URL")
    }

    private fun startKeepAliveService(username: String, password: String) {
        val serviceIntent = Intent(this, KeepAliveService::class.java)
        serviceIntent.putExtra("username", username)
        serviceIntent.putExtra("password", password)
        startService(serviceIntent)
    }

    private fun stopKeepAliveService() {
        val serviceIntent = Intent(this, KeepAliveService::class.java)
        stopService(serviceIntent)
    }
}

//
//import android.os.Bundle
//import com.google.android.material.snackbar.Snackbar
//import androidx.appcompat.app.AppCompatActivity
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.navigateUp
//import androidx.navigation.ui.setupActionBarWithNavController
//import android.view.Menu
//import android.view.MenuItem
//import com.iitgoapapaharpic.upfortfrgrnd.databinding.ActivityMainBinding
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        setSupportActionBar(binding.toolbar)
//
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//
//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
//}