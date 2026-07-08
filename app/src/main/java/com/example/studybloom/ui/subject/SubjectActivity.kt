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
import com.example.studybloom.ui.topic.TopicActivity
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

        // 1. Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // 2. Bind Views
        val rvSubjects = findViewById<RecyclerView>(R.id.rvSubjects)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)

        // 3. Setup RecyclerView & Adapter
        adapter = SubjectAdapter(
            subjects = emptyList(),
            onItemClick = { subject ->
                // Klik biasa → buka daftar topik
                val intent = Intent(this, TopicActivity::class.java).apply {
                    putExtra("SUBJECT_ID", subject.id)
                    putExtra("SUBJECT_NAME", subject.name)
                }
                startActivity(intent)
            },
            onItemLongClick = { subject ->
                // TEKAN LAMA → Buka form Edit/Delete Mata Pelajaran
                val intent = Intent(this, SubjectFormActivity::class.java).apply {
                    putExtra("SUBJECT_ID", subject.id)
                }
                startActivity(intent)
            }
        )

        rvSubjects.layoutManager = LinearLayoutManager(this)
        rvSubjects.adapter = adapter

        // 4. Observe data subject dari ViewModel
        subjectViewModel.allSubjects.observe(this) { subjects ->
            if (subjects.isNullOrEmpty()) {
                rvSubjects.visibility = View.GONE
                tvEmptyState?.visibility = View.VISIBLE
            } else {
                rvSubjects.visibility = View.VISIBLE
                tvEmptyState?.visibility = View.GONE
                adapter.updateList(subjects)

                // Update progress tiap subject secara asinkron
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

        // 5. FAB buka form tambah subject
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
