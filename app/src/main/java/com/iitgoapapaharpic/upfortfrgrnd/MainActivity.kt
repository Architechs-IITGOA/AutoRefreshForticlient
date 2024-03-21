package com.iitgoapapaharpic.upfortfrgrnd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iitgoapapaharpic.upfortfrgrnd.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                launchLoginService(username, password)
            } else {
                Log.d("Empty","Username or Password is empty")
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
    }
}