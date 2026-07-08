package com.example.studybloom.ui.statistics

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.studybloom.R
import com.example.studybloom.viewmodel.StatisticsViewModel

class StatisticsActivity : AppCompatActivity() {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // 1. Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Bind Views
        val tvCurrentStreak = findViewById<TextView>(R.id.tvCurrentStreak)
        val tvStreakStatus = findViewById<TextView>(R.id.tvStreakStatus)
        val tvDaysUntilExpire = findViewById<TextView>(R.id.tvDaysUntilExpire)
        val tvTotalStudyTime = findViewById<TextView>(R.id.tvTotalStudyTime)
        val tvTotalSessions = findViewById<TextView>(R.id.tvTotalSessions)
        val tvWeeklySummary = findViewById<TextView>(R.id.tvWeeklySummary)

        // 3. Observe Data dari ViewModel
        viewModel.currentStreak.observe(this) { streak ->
            tvCurrentStreak.text = streak?.toString() ?: "0"
        }

        viewModel.streakStatus.observe(this) { status ->
            tvStreakStatus.text = status ?: "-"
        }

        viewModel.daysUntilExpire.observe(this)  { days ->
            val d = days ?: 0
            tvDaysUntilExpire.text = if (d > 0) "$d days until expire" else "Streak Expired"
            if (d <= 1) {
                tvDaysUntilExpire.setTextColor(getColor(R.color.error))
            } else {
                tvDaysUntilExpire.setTextColor(getColor(R.color.outline))
            }
        }

        viewModel.totalStudyTime.observe(this) { time ->
            tvTotalStudyTime.text = time ?: "0h 0m"
        }

        viewModel.totalSessions.observe(this) { sessions ->
            tvTotalSessions.text = sessions?.toString() ?: "0"
        }
        

        viewModel.weeklySummary.observe(this) { summary ->
            tvWeeklySummary.text = summary ?: "No study sessions recorded this week."
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
