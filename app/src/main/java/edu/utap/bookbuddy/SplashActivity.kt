package edu.utap.bookbuddy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Optional: remove action bar for clean look
        setTheme(R.style.Theme_BookBuddy_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay before launching MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Prevent back navigation to splash
        }, 1500) // Show for 1.5 seconds
    }
}