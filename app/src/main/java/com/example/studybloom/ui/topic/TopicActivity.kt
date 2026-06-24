package com.example.studybloom.ui.topic

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybloom.R
import com.example.studybloom.adapter.TopicAdapter
import com.example.studybloom.ui.session.StudySessionActivity
import com.example.studybloom.viewmodel.StudySessionViewModel
import com.example.studybloom.viewmodel.TopicViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TopicActivity : AppCompatActivity() {

    private val topicViewModel: TopicViewModel by viewModels()
    private val sessionViewModel: StudySessionViewModel by viewModels()
    private lateinit var adapter: TopicAdapter

    private var subjectId: Int = -1
    private var subjectName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        // Ambil data dari intent
        subjectId = intent.getIntExtra("SUBJECT_ID", -1)
        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Topics"

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = subjectName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup adapter
        adapter = TopicAdapter(
            topics = emptyList(),
            onItemClick = { topic ->
                // Klik → buka form edit
                val intent = Intent(this, TopicFormActivity::class.java)
                intent.putExtra("TOPIC_ID", topic.id)
                intent.putExtra("SUBJECT_ID", subjectId)
                startActivity(intent)
            },
            onStartSession = { topic ->
                // Long click → mulai sesi Pomodoro
                val intent = Intent(this, StudySessionActivity::class.java)
                intent.putExtra("TOPIC_ID", topic.id)
                intent.putExtra("TOPIC_NAME", topic.name)
                intent.putExtra("SUBJECT_NAME", subjectName)
                startActivity(intent)
            }
        )

        // Setup RecyclerView
        val rvTopics = findViewById<RecyclerView>(R.id.rvTopics)
        rvTopics.layoutManager = LinearLayoutManager(this)
        rvTopics.adapter = adapter

        // Load topics
        topicViewModel.loadTopics(subjectId)

        // Observe topics
        topicViewModel.topics.observe(this) { topics ->
            adapter.updateList(topics)

            // Update card progress atas
            val total = topics.size
            val completed = topics.count { it.completed }
            val percent = if (total > 0) (completed * 100 / total) else 0

            findViewById<TextView>(R.id.tvTotalTopics).text = "$total Topics"
            findViewById<ProgressBar>(R.id.progressCourse).progress = percent
            findViewById<TextView>(R.id.tvProgressPercent).text = "$percent% Completed"

            // Hitung durasi tiap topic
            lifecycleScope.launch {
                val durationMap = mutableMapOf<Int, Int>()
                topics.forEach { topic ->
                    val sessions = sessionViewModel.getSessionsByDate("")
                    // Hitung total durasi per topic dari semua session
                    var totalDuration = 0
                    sessionViewModel.allSessions.value?.forEach { session ->
                        if (session.topicId == topic.id) {
                            totalDuration += session.duration
                        }
                    }
                    durationMap[topic.id] = totalDuration
                }
                adapter.updateDurationMap(durationMap)
            }
        }

        // FAB → tambah topic baru
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTopic)
        fabAdd.setOnClickListener {
            val intent = Intent(this, TopicFormActivity::class.java)
            intent.putExtra("SUBJECT_ID", subjectId)
            startActivity(intent)
        }
    }
}