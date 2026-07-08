package com.example.studybloom.ui.topic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybloom.R
import com.example.studybloom.adapter.TopicAdapter
import com.example.studybloom.data.entity.Topic
import com.example.studybloom.ui.session.StudySessionActivity
import com.example.studybloom.viewmodel.StudySessionViewModel
import com.example.studybloom.viewmodel.TopicViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TopicActivity : AppCompatActivity() {

    private val topicViewModel: TopicViewModel by viewModels()
    private val sessionViewModel: StudySessionViewModel by viewModels()
    private lateinit var adapter: TopicAdapter

    private var subjectId: Int = -1
    private var subjectName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic)

        subjectId = intent.getIntExtra("SUBJECT_ID", -1)
        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Topics"

        if (subjectId == -1) {
            Toast.makeText(this, "Error: Invalid Subject", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = subjectName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val rvTopics = findViewById<RecyclerView>(R.id.rvTopics)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        val progressBar = findViewById<ProgressBar?>(R.id.progressCourse)
        val tvHeaderStats = findViewById<TextView?>(R.id.tvProgressPercent) 
        val tvTotalTopics = findViewById<TextView>(R.id.tvTotalTopics)
        val tvHeaderTitle = findViewById<TextView?>(R.id.tvHeaderTitle)

        // Pastikan label header benar
        tvHeaderTitle?.text = "Study Progress"
        progressBar?.visibility = View.GONE

        adapter = TopicAdapter(
            topics = emptyList(),
            onItemClick = { topic -> startTopicForm(topic.id) },
            onStartSession = { topic -> startStudySession(topic) }
        )

        rvTopics?.layoutManager = LinearLayoutManager(this)
        rvTopics?.adapter = adapter

        topicViewModel.loadTopics(subjectId)

        // Observe Topics
        topicViewModel.topics.observe(this) { topics ->
            if (topics.isNullOrEmpty()) {
                rvTopics?.visibility = View.GONE
                tvEmptyState?.visibility = View.VISIBLE
                tvTotalTopics?.text = "0 Topics"
                tvHeaderStats?.text = "Total Study Time: 0m"
            } else {
                rvTopics?.visibility = View.VISIBLE
                tvEmptyState?.visibility = View.GONE
                adapter.updateList(topics)
                tvTotalTopics?.text = "${topics.size} Topics"
                
                // Trigger update stats segera
                updateHeaderStats(topics, sessionViewModel.allSessions.value ?: emptyList(), tvHeaderStats)
            }
        }

        // Observe Sessions
        sessionViewModel.allSessions.observe(this) { sessions ->
            val topicsList = topicViewModel.topics.value
            if (!topicsList.isNullOrEmpty()) {
                updateHeaderStats(topicsList, sessions ?: emptyList(), tvHeaderStats)
            }
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTopic)
        fabAdd.setOnClickListener {
            val intent = Intent(this, TopicFormActivity::class.java)
            intent.putExtra("SUBJECT_ID", subjectId) 
            startActivity(intent)
        }
    }

    private fun updateHeaderStats(topics: List<Topic>, sessions: List<com.example.studybloom.data.entity.StudySession>, tvHeaderStats: TextView?) {
        val subjectTopicIds = topics.map { it.id }.toSet()
        val totalDurationMinutes = sessions.filter { it.topicId in subjectTopicIds }
            .sumOf { it.duration }
        
        val hours = totalDurationMinutes / 60
        val minutes = totalDurationMinutes % 60
        
        // Teks ini akan menimpa "0% Completed" yang mungkin ada di XML
        tvHeaderStats?.text = if (hours > 0) {
            "Total Study Time: ${hours}h ${minutes}m"
        } else {
            "Total Study Time: ${minutes}m"
        }

        val durationMap = mutableMapOf<Int, Int>()
        topics.forEach { topic ->
            val topicDuration = sessions.filter { it.topicId == topic.id }
                .sumOf { it.duration }
            durationMap[topic.id] = topicDuration
        }
        adapter.updateDurationMap(durationMap)
    }

    private fun startTopicForm(topicId: Int) {
        val intent = Intent(this, TopicFormActivity::class.java)
        intent.putExtra("TOPIC_ID", topicId)
        intent.putExtra("SUBJECT_ID", subjectId)
        startActivity(intent)
    }

    private fun startStudySession(topic: Topic) {
        val intent = Intent(this, StudySessionActivity::class.java)
        intent.putExtra("TOPIC_ID", topic.id)
        intent.putExtra("TOPIC_NAME", topic.name)
        intent.putExtra("SUBJECT_NAME", subjectName)
        startActivity(intent)
    }
}
