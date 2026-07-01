package com.example.studybloom.ui.subject

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studybloom.R
import com.example.studybloom.data.entity.Subject
import com.example.studybloom.viewmodel.SubjectViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SubjectFormActivity : AppCompatActivity() {

    private val subjectViewModel: SubjectViewModel by viewModels()

    private lateinit var tvFormTitle: TextView
    private lateinit var tilSubjectName: TextInputLayout
    private lateinit var etSubjectName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private var subjectId: Int = -1
    private var existingSubject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_form)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Bind views
        tvFormTitle = findViewById(R.id.tvFormTitle)
        tilSubjectName = findViewById(R.id.tilSubjectName)
        etSubjectName = findViewById(R.id.etSubjectName)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)

        // Cek mode: Edit atau Tambah
        subjectId = intent.getIntExtra("SUBJECT_ID", -1)

        if (subjectId != -1) {
            // Mode Edit
            setupEditMode()
        } else {
            // Mode Tambah
            setupAddMode()
        }
    }

    private fun setupAddMode() {
        tvFormTitle.text = "Add Subject"
        btnSave.text = "Save Subject"
        btnDelete.visibility = View.GONE

        btnSave.setOnClickListener {
            saveSubject()
        }
    }

    private fun setupEditMode() {
        tvFormTitle.text = "Edit Subject"
        btnSave.text = "Save Changes"
        btnDelete.visibility = View.VISIBLE

        // Load data subject dari database
        lifecycleScope.launch {
            existingSubject = subjectViewModel.getSubjectById(subjectId)
            existingSubject?.let { subject ->
                etSubjectName.setText(subject.name)
                etDescription.setText(subject.description)
            }
        }

        btnSave.setOnClickListener {
            saveSubject()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun saveSubject() {
        val name = etSubjectName.text.toString().trim()
        val description = etDescription.text.toString().trim()

        // Validasi nama tidak boleh kosong
        if (name.isEmpty()) {
            tilSubjectName.error = "Subject name is required"
            return
        }
        tilSubjectName.error = null

        try {
            if (subjectId != -1) {
                // Mode Edit → update
                val updatedSubject = Subject(
                    id = subjectId,
                    name = name,
                    description = description
                )
                subjectViewModel.update(updatedSubject)
                Toast.makeText(this, "Subject updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Mode Tambah → insert
                val newSubject = Subject(
                    name = name,
                    description = description
                )
                subjectViewModel.insert(newSubject)
                Toast.makeText(this, "Subject created successfully", Toast.LENGTH_SHORT).show()
            }
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving subject. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Subject")
            .setMessage("Are you sure you want to delete this subject? All topics inside will also be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                existingSubject?.let { subject ->
                    subjectViewModel.delete(subject)
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
