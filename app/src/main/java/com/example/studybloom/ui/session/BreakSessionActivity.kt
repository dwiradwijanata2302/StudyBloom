package com.example.studybloom.ui.session

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studybloom.R
import com.example.studybloom.ui.topic.TopicActivity

class BreakSessionActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnContinue: Button
    private lateinit var btnEnd: Button

    private var topicId: Int = -1
    private var topicName: String = ""
    private var subjectName: String = ""
    private var countDownTimer: CountDownTimer? = null
    private var isTimerFinished = false

    private companion object {
        private const val BREAK_DURATION_MS = 5 * 60 * 1000L // 5 menit
        private const val TICK_INTERVAL_MS = 1000L // 1 detik
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_session)

        // Ambil data dari intent
        topicId = intent.getIntExtra("TOPIC_ID", -1)
        topicName = intent.getStringExtra("TOPIC_NAME") ?: "Unknown Topic"
        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Unknown Subject"

        // Bind views
        tvTimer = findViewById(R.id.tvTimer)
        btnContinue = findViewById(R.id.btnContinueSession)
        btnEnd = findViewById(R.id.btnEndSession)

        // Start break timer
        startBreakTimer()

        // Setup button listeners
        btnContinue.setOnClickListener {
            cancelTimer()
            goToContinueSession()
        }

        btnEnd.setOnClickListener {
            cancelTimer()
            goBackToTopic()
        }
    }

    private fun startBreakTimer() {
        countDownTimer = object : CountDownTimer(BREAK_DURATION_MS, TICK_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                isTimerFinished = true
                tvTimer.text = "00:00"
                Toast.makeText(this@BreakSessionActivity, "Break ended! Ready for another?", Toast.LENGTH_SHORT).show()

                // Button tetap bisa diklik
                btnContinue.isEnabled = true
                btnEnd.isEnabled = true
            }
        }.start()
    }

    private fun goToContinueSession() {
        Toast.makeText(this, "Starting session 2...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, StudySessionActivity::class.java)
        intent.putExtra("TOPIC_ID", topicId)
        intent.putExtra("TOPIC_NAME", topicName)
        intent.putExtra("SUBJECT_NAME", subjectName)
        startActivity(intent)
        finish()
    }

    private fun goBackToTopic() {
        Toast.makeText(this, "Good job! Keep up the streak.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, TopicActivity::class.java)
        intent.putExtra("SUBJECT_ID", -1) // bisa ditambah nanti
        startActivity(intent)
        finish()
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
    }

    override fun onBackPressed() {
        if (!isTimerFinished) {
            AlertDialog.Builder(this)
                .setTitle("Exit Break?")
                .setMessage("Return to activity?")
                .setPositiveButton("Yes") { _, _ ->
                    cancelTimer()
                    finish()
                }
                .setNegativeButton("Continue Break", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}