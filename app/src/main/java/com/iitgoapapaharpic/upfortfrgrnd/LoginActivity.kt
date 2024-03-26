package com.iitgoapapaharpic.upfortfrgrnd

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iitgoapapaharpic.upfortfrgrnd.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val sharedPreferences = SharedPreferences(applicationContext)
                sharedPreferences.storeCredentials(username, password)
                Toast.makeText(this, "Credentials saved successfully.", Toast.LENGTH_LONG).show()


                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Username or password is empty.", Toast.LENGTH_LONG).show()
            }
        }
    }


}