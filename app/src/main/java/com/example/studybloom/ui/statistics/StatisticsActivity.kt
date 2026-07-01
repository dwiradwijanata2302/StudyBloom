package com.example.studybloom.ui.statistics

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studybloom.R
import com.example.studybloom.viewmodel.StatisticsViewModel

class StatisticsActivity : AppCompatActivity() {

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    private lateinit var tvTotalStudyTime: TextView
    private lateinit var tvTotalSessions: TextView
    private lateinit var tvTopicsCompleted: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvStreakStatus: TextView
    private lateinit var tvDaysUntilExpire: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Bind views
        tvTotalStudyTime = findViewById(R.id.tvTotalStudyTime)
        tvTotalSessions = findViewById(R.id.tvTotalSessions)
        tvTopicsCompleted = findViewById(R.id.tvTopicsCompleted)
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak)
        tvStreakStatus = findViewById(R.id.tvStreakStatus)
        tvDaysUntilExpire = findViewById(R.id.tvDaysUntilExpire)

        // Observe LiveData
        statisticsViewModel.totalStudyTime.observe(this) { time ->
            tvTotalStudyTime.text = time
        }

        statisticsViewModel.totalSessions.observe(this) { sessions ->
            tvTotalSessions.text = sessions.toString()
        }

        statisticsViewModel.totalTopicsCompleted.observe(this) { topics ->
            tvTopicsCompleted.text = topics.toString()
        }

        statisticsViewModel.currentStreak.observe(this) { streak ->
            tvCurrentStreak.text = "🔥 $streak"
        }

        statisticsViewModel.streakStatus.observe(this) { status ->
            tvStreakStatus.text = when (status) {
                "Active" -> "🔥 Active - Keep it up!"
                "Recoverable" -> "⏸️ Paused - You can still recover"
                "Expired" -> "❌ Expired - Start fresh today"
                else -> status
            }
        }

        statisticsViewModel.daysUntilExpire.observe(this) { daysLeft ->
            tvDaysUntilExpire.text = when {
                daysLeft == 0 -> "Streak expired today"
                daysLeft == 1 -> "1 day left to recover"
                else -> "$daysLeft days left to recover"
            }
        }

        statisticsViewModel.weeklyData.observe(this) { weeklyMap ->
            // Bisa digunakan untuk chart nanti
        }
    }
}