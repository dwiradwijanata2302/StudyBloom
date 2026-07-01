package com.example.studybloom.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.studybloom.R
import com.example.studybloom.ui.statistics.StatisticsActivity
import com.example.studybloom.ui.subject.SubjectActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_dashboard)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            bottomNavigation.selectedItemId = R.id.nav_home
        }

        // Setup navigation listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_subjects -> {
                    startActivity(android.content.Intent(this, SubjectActivity::class.java))
                    false // jangan highlight, karena start activity lain
                }
                R.id.nav_statistics -> {
                    startActivity(android.content.Intent(this, StatisticsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}