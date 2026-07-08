package com.example.studybloom.ui.session

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studybloom.R
import com.example.studybloom.data.entity.StudySession
import com.example.studybloom.viewmodel.StudySessionViewModel
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*
import com.example.studybloom.utils.StreakCalculator
import kotlinx.coroutines.launch

class StudySessionActivity : AppCompatActivity() {

    private val sessionViewModel: StudySessionViewModel by viewModels()

    private lateinit var tvTimer: TextView
    private lateinit var tvTopicName: TextView
    private lateinit var tvSessionInfo: TextView
    private lateinit var chipSubjectName: Chip

    private var topicId: Int = -1
    private var topicName: String = ""
    private var subjectName: String = ""
    private var countDownTimer: CountDownTimer? = null
    private var isTimerFinished = false

    private companion object {
        private const val FOCUS_DURATION_MS = 25 * 60 * 1000L // 25 menit
        private const val TICK_INTERVAL_MS = 1000L // 1 detik
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_session)

        topicId = intent.getIntExtra("TOPIC_ID", -1)
        topicName = intent.getStringExtra("TOPIC_NAME") ?: "Unknown Topic"
        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Unknown Subject"

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        tvTimer = findViewById(R.id.tvTimer)
        tvTopicName = findViewById(R.id.tvTopicName)
        tvSessionInfo = findViewById(R.id.tvSessionInfo)
        chipSubjectName = findViewById(R.id.chipSubjectName)

        chipSubjectName.text = subjectName
        tvTopicName.text = topicName
        tvSessionInfo.text = "Session 1 of 4"

        startFocusTimer()
    }

    private fun startFocusTimer() {
        countDownTimer = object : CountDownTimer(FOCUS_DURATION_MS, TICK_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                if (millisUntilFinished < 60000) {
                    tvTimer.setTextColor(getColor(R.color.error))
                }
            }

            override fun onFinish() {
                isTimerFinished = true
                tvTimer.text = "00:00"
                // Panggil save secara aman
                saveSessionAndRefreshStreak()
            }
        }.start()
    }

    private fun saveSessionAndRefreshStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val session = StudySession(
            topicId = topicId,
            sessionDate = today,
            duration = 25
        )

        // Gunakan lifecycleScope agar urutannya pasti: 
        // 1. Simpan Sesi 
        // 2. Tunggu Selesai
        // 3. Update Streak
        lifecycleScope.launch {
            try {
                sessionViewModel.insert(session)
                StreakCalculator.updateStreak(this@StudySessionActivity)
                Toast.makeText(this@StudySessionActivity, "Session completed! Streak updated.", Toast.LENGTH_SHORT).show()
                goToBreakSession()
            } catch (e: Exception) {
                Toast.makeText(this@StudySessionActivity, "Error saving session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToBreakSession() {
        val intent = Intent(this, BreakSessionActivity::class.java).apply {
            putExtra("TOPIC_ID", topicId)
            putExtra("TOPIC_NAME", topicName)
            putExtra("SUBJECT_NAME", subjectName)
        }
        startActivity(intent)
        finish()
    }

    private fun confirmExit() {
        if (!isTimerFinished) {
            AlertDialog.Builder(this)
                .setTitle("Exit Session?")
                .setMessage("Session will not be saved if you exit now.")
                .setPositiveButton("Exit") { _, _ ->
                    countDownTimer?.cancel()
                    finish()
                }
                .setNegativeButton("Continue", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        confirmExit()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
