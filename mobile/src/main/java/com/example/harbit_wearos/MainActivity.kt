package com.example.harbit_wearos

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var gyroTextView: android.widget.TextView
    private val MSG_PATH = "/gyro"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        gyroTextView = findViewById(R.id.gyroTextView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Listen for messages from the Wear OS device
        com.google.android.gms.wearable.Wearable.getMessageClient(this)
            .addListener(object : com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener {
                override fun onMessageReceived(event: com.google.android.gms.wearable.MessageEvent) {
                    if (event.path == MSG_PATH) {
                        val payload = event.data
                        // Each sample: [long timestamp][float x][float y][float z]
                        val buf = java.nio.ByteBuffer.wrap(payload)
                        val sb = StringBuilder()
                        while (buf.remaining() >= 8 + 3 * 4) {
                            val timestamp = buf.long
                            val x = buf.float
                            val y = buf.float
                            val z = buf.float
                            sb.append("timestamp: $timestamp\nx: $x\ny: $y\nz: $z\n\n")
                        }
                        runOnUiThread {
                            gyroTextView.text = sb.toString()
                        }
                    }
                }
            })
    }
}