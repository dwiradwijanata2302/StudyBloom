package com.example.studybloom.ui.subject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybloom.R
import com.example.studybloom.adapter.SubjectAdapter
import com.example.studybloom.viewmodel.SubjectViewModel
import com.example.studybloom.viewmodel.TopicViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class SubjectActivity : AppCompatActivity() {

    private val subjectViewModel: SubjectViewModel by viewModels()
    private val topicViewModel: TopicViewModel by viewModels()
    private lateinit var adapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup RecyclerView
        adapter = SubjectAdapter(emptyList()) { subject ->
            // Klik subject → buka form edit
            val intent = Intent(this, SubjectFormActivity::class.java)
            intent.putExtra("SUBJECT_ID", subject.id)
            intent.putExtra("SUBJECT_NAME", subject.name)
            startActivity(intent)
        }

        val rvSubjects = findViewById<RecyclerView>(R.id.rvSubjects)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        rvSubjects.layoutManager = LinearLayoutManager(this)
        rvSubjects.adapter = adapter

        // Observe data subject dari ViewModel
        subjectViewModel.allSubjects.observe(this) { subjects ->
            if (subjects.isEmpty()) {
                rvSubjects.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            } else {
                rvSubjects.visibility = View.VISIBLE
                tvEmptyState.visibility = View.GONE
                adapter.updateList(subjects)

                // Setelah dapat list subject, hitung progress tiap subject
                lifecycleScope.launch {
                    val progressMap = mutableMapOf<Int, Pair<Int, Int>>()
                    subjects.forEach { subject ->
                        val total = topicViewModel.getTotalTopics(subject.id)
                        val completed = topicViewModel.getCompletedTopics(subject.id)
                        progressMap[subject.id] = Pair(completed, total)
                    }
                    adapter.updateProgress(progressMap)
                }
            }
        }

        // FAB buka form tambah subject
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddSubject)
        fabAdd.setOnClickListener {
            val intent = Intent(this, SubjectFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
