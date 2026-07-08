package com.example.studybloom.ui.topic

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studybloom.R
import com.example.studybloom.data.entity.Topic
import com.example.studybloom.viewmodel.TopicViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class TopicFormActivity : AppCompatActivity() {

    private val topicViewModel: TopicViewModel by viewModels()

    private lateinit var tvFormTitle: TextView
    private lateinit var tilTopicName: TextInputLayout
    private lateinit var etTopicName: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private var topicId: Int = -1
    private var subjectId: Int = -1
    private var existingTopic: Topic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_form)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Bind views
        tvFormTitle = findViewById(R.id.tvFormTitle)
        tilTopicName = findViewById(R.id.tilTopicName)
        etTopicName = findViewById(R.id.etTopicName)
        etNotes = findViewById(R.id.etNotes)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)

        // Ambil data dari intent
        topicId = intent.getIntExtra("TOPIC_ID", -1)
        subjectId = intent.getIntExtra("SUBJECT_ID", -1)

        if (topicId != -1) {
            setupEditMode()
        } else {
            setupAddMode()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun setupAddMode() {
        tvFormTitle.text = "New Study Topic"
        btnSave.text = "Save Topic"
        btnDelete.visibility = View.GONE

        btnSave.setOnClickListener { saveTopic() }
    }

    private fun setupEditMode() {
        tvFormTitle.text = "Edit Topic"
        btnSave.text = "Save Changes"
        btnDelete.visibility = View.VISIBLE

        // Load data topic
        lifecycleScope.launch {
            existingTopic = topicViewModel.getTopicById(topicId)
            existingTopic?.let { topic ->
                etTopicName.setText(topic.name)
                etNotes.setText(topic.notes)
            }
        }

        btnSave.setOnClickListener { saveTopic() }
        btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun saveTopic() {
        val name = etTopicName.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        // Validasi
        if (name.isEmpty()) {
            tilTopicName.error = "Topic name is required"
            return
        }
        tilTopicName.error = null

        if (topicId != -1) {
            // Mode Edit → update
            val updatedTopic = Topic(
                id = topicId,
                subjectId = subjectId,
                name = name,
                notes = notes,
                completed = existingTopic?.completed ?: false
            )
            topicViewModel.update(updatedTopic)
        } else {
            // Mode Tambah → insert
            val newTopic = Topic(
                subjectId = subjectId,
                name = name,
                notes = notes,
                completed = false
            )
            topicViewModel.insert(newTopic)
        }

        finish()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Topic")
            .setMessage("Are you sure you want to delete this topic?")
            .setPositiveButton("Delete") { _, _ ->
                existingTopic?.let { topic ->
                    topicViewModel.delete(topic)
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
