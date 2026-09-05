package com.example.kotlinkyber

import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "MLKEM_DEMO"

    // Native methods
    private external fun nativeGenerateKeyPair(): ByteArray?
    private external fun nativeEncapsulate(pk: ByteArray): ByteArray?
    private external fun nativeDecapsulate(ct: ByteArray, sk: ByteArray): ByteArray?

    // Test Data
    private var recipientPublicKey: ByteArray? = null
    private var recipientSecretKey: ByteArray? = null

    companion object {
        init {
            System.loadLibrary("mlkem-native")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGenerateKeys = findViewById<Button>(R.id.btnGenerateKeys)
        val btnEncrypt = findViewById<Button>(R.id.btnEncrypt)
        val btnDecrypt = findViewById<Button>(R.id.btnDecrypt)

        val tvPublicKey = findViewById<TextView>(R.id.tvPublicKey)
        val tvCiphertext = findViewById<TextView>(R.id.tvCiphertext)
        val tvSharedSecret = findViewById<TextView>(R.id.tvSharedSecret)

        // 1. Generate Recipient Keys
        btnGenerateKeys.setOnClickListener {
            val result = nativeGenerateKeyPair()
            if (result != null) {
                // ML-KEM-1024 Sizes: PK=1568, SK=3168
                val pkSize = 1568
                val skSize = 3168
                
                recipientPublicKey = result.copyOfRange(0, pkSize)
                recipientSecretKey = result.copyOfRange(pkSize, pkSize + skSize)

                tvPublicKey.text = Base64.encodeToString(recipientPublicKey, Base64.NO_WRAP)
                Toast.makeText(this, "Recipient Keypair Generated (ML-KEM-1024)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Key generation failed!", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Encapsulate (Encrypt)
        btnEncrypt.setOnClickListener {
            val pk = recipientPublicKey
            if (pk != null) {
                val result = nativeEncapsulate(pk)
                if (result != null) {
                    // Result contains [Ciphertext (1568) | Shared Secret (32)]
                    val ctSize = 1568
                    val ct = result.copyOfRange(0, ctSize)
                    val ss = result.copyOfRange(ctSize, ctSize + 32)

                    tvCiphertext.text = Base64.encodeToString(ct, Base64.NO_WRAP)
                    tvSharedSecret.text = bytesToHex(ss)
                    Toast.makeText(this, "Encapsulation Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Encapsulation failed!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please generate recipient keys first!", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Decapsulate (Decrypt)
        btnDecrypt.setOnClickListener {
            val ctBase64 = tvCiphertext.text.toString()
            val sk = recipientSecretKey

            if (sk != null && ctBase64.isNotEmpty()) {
                val ctBytes = Base64.decode(ctBase64, Base64.NO_WRAP)
                val ss = nativeDecapsulate(ctBytes, sk)

                if (ss != null) {
                    tvSharedSecret.text = "Decrypted SS: ${bytesToHex(ss)}"
                    Toast                    .makeText(this, "Decapsulation Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Decapsulation failed!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Need ciphertext and Secret Key!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }
}
