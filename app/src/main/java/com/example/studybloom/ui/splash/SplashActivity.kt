package com.example.studybloom.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.studybloom.R
import com.example.studybloom.ui.home.HomeActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay 2 detik sebelum pindah ke HomeActivity
        Handler(Looper.getMainLooper()).postDelayed({
            goToHome()
        }, 2000)
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)

        // Apply fade out animation untuk splash, fade in untuk home
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        finish()
    }

    // Override back button - tidak boleh kembali dari splash
    override fun onBackPressed() {
        // Do nothing - user tidak bisa back dari splash
    }
}