package com.iitgoapapaharpic.upfortfrgrnd

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class SharedPreferences(private val context: Context) {
    companion object {
        private const val PREFERENCE_FILE_KEY = "shared_preferences"
        private const val ENCRYPTION_KEY_NAME = "encryption_key"
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    }

    // Store encrypted data
    fun storeCredentials(username: String, password: String) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
//        editor.putString("encrypted_username", encryptData(username))
//        editor.putString("encrypted_password", encryptData(password))
        editor.putString("username", username)
        editor.putString("password", password)
        editor.commit()
    }

    // Retrieve and decrypt data
    fun getCredentials(): Pair<String, String>? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)

        return if (username != null && password != null) {
//            Pair(decryptData(encryptedUsername), decryptData(encryptedPassword))
            Pair(username, password)
        } else {
            null
        }
    }

    // Helper method to get the encryption key from the AndroidKeyStore
    private fun getEncryptionKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(
            ENCRYPTION_KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    // Helper method to encrypt data
    private fun encryptData(plainText: String): String {
        val key = getEncryptionKey()
        val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(iv + encryptedBytes, Base64.NO_WRAP)
    }

    // Helper method to decrypt data
    private fun decryptData(cipherText: String): String {
        val key = getEncryptionKey()
        val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/GCM/NoPadding")

        val encodedBytes = Base64.decode(cipherText, Base64.NO_WRAP)
        val iv = encodedBytes.copyOfRange(0, 12)
        val encryptedBytes = encodedBytes.copyOfRange(12, encodedBytes.size)

        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    
}