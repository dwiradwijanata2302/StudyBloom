package com.example.studybloom.ui.session

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studybloom.R
import com.example.studybloom.data.entity.StudySession
import com.example.studybloom.viewmodel.StudySessionViewModel
import com.google.android.material.chip.Chip
import com.example.studybloom.utils.StreakCalculator
import java.text.SimpleDateFormat
import java.util.*

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

        // Ambil data dari intent
        topicId = intent.getIntExtra("TOPIC_ID", -1)
        topicName = intent.getStringExtra("TOPIC_NAME") ?: "Unknown Topic"
        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Unknown Subject"

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            confirmExit()
        }

        // Bind views
        tvTimer = findViewById(R.id.tvTimer)
        tvTopicName = findViewById(R.id.tvTopicName)
        tvSessionInfo = findViewById(R.id.tvSessionInfo)
        chipSubjectName = findViewById(R.id.chipSubjectName)

        // Setup UI
        chipSubjectName.text = subjectName
        tvTopicName.text = topicName
        tvSessionInfo.text = "Session 1 of 4" // Bisa diubah dinamis nanti

        // Start timer
        startFocusTimer()
    }

    private fun startFocusTimer() {
        countDownTimer = object : CountDownTimer(FOCUS_DURATION_MS, TICK_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)

                // Ubah warna ke merah saat < 1 menit
                if (millisUntilFinished < 60000) {
                    tvTimer.setTextColor(getColor(R.color.error))
                }
            }

            override fun onFinish() {
                isTimerFinished = true
                tvTimer.text = "00:00"

                // Simpan session ke database
                saveSession()

                // Pindah ke break
                goToBreakSession()
            }
        }.start()
    }

    private fun saveSession() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val session = StudySession(
            topicId = topicId,
            sessionDate = today,
            duration = 25
        )

        try {
            sessionViewModel.insert(session)

            // UPDATE STREAK setelah session saved
            StreakCalculator.updateStreak(this)

            Toast.makeText(this, "Session completed! Take a break.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save session. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToBreakSession() {
        val intent = Intent(this, BreakSessionActivity::class.java)
        intent.putExtra("TOPIC_ID", topicId)
        intent.putExtra("TOPIC_NAME", topicName)
        intent.putExtra("SUBJECT_NAME", subjectName)
        startActivity(intent)
        finish()
    }

    private fun confirmExit() {
        if (!isTimerFinished) {
            AlertDialog.Builder(this)
                .setTitle("Exit Session?")
                .setMessage("Session will not be saved if you exit now.")
                .setPositiveButton("Exit") { _, _ ->
                    cancelTimer()
                    finish()
                }
                .setNegativeButton("Continue", null)
                .show()
        } else {
            finish()
        }
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        Toast.makeText(this, "Session cancelled. No progress saved.", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        confirmExit()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}