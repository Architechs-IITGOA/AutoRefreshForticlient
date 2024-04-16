package com.iitgoapapaharpic.fortinetconnect

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.iitgoapapaharpic.fortinetconnect.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {




    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)





//        binding.crash.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//
//        addContentView(binding.crash, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))



        binding.addCredentials.setOnClickListener {
            val sharedPreferences = SharedPreferences(applicationContext)
            val credentials = sharedPreferences.getCredentials()

            if (credentials != null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }else{
                // For First Time Launch
                val intent = Intent(this, AskPermissions::class.java)
                startActivity(intent)
            }
        }

        binding.startServiceButton.setOnClickListener {
            val sharedPreferences = SharedPreferences(applicationContext)
            val credentials = sharedPreferences.getCredentials()

            if (credentials != null) {
                val (username, password) = credentials

                launchLoginService(username, password)
            }else{
                Toast.makeText(this, "Please Add credentials first.", Toast.LENGTH_LONG).show()
            }
        }

        binding.stopServiceButton.setOnClickListener {
            stopKeepAliveLoginService()
        }

    }

    private fun launchLoginService(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("Daddy", "launch login service")
                startKeepAliveLoginService(username, password)
            } catch (e: Exception) {
                Log.e("launchLoginService", "Error: ${e.message}")
            }
        }
    }

    private fun startKeepAliveLoginService(username: String, password: String) {
        val serviceIntent = Intent(this, KeepAliveLoginService::class.java)
        serviceIntent.putExtra("username", username)
        serviceIntent.putExtra("password", password)
        Log.d("Daddy", "KeepAliveLoginService")
        startService(serviceIntent)
    }

    private fun stopKeepAliveLoginService() {
        val serviceIntent = Intent(this, KeepAliveLoginService::class.java)
        stopService(serviceIntent)

        Toast.makeText(this, "AutoConnect stopped.", Toast.LENGTH_LONG).show()
    }


}