package com.iitgoapapaharpic.fortinetconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iitgoapapaharpic.fortinetconnect.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addCredentials.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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

        startService(serviceIntent)
    }

    private fun stopKeepAliveLoginService() {
        val serviceIntent = Intent(this, KeepAliveLoginService::class.java)
        stopService(serviceIntent)

        Toast.makeText(this, "AutoConnect stopped.", Toast.LENGTH_LONG).show()
    }
}