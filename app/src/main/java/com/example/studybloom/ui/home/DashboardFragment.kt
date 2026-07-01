package com.example.studybloom.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.studybloom.R
import com.example.studybloom.ui.session.StudySessionActivity
import com.example.studybloom.ui.subject.SubjectActivity
import com.example.studybloom.ui.topic.TopicActivity
import com.example.studybloom.viewmodel.StatisticsViewModel
import com.example.studybloom.viewmodel.SubjectViewModel
import com.example.studybloom.viewmodel.TopicViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private val statisticsViewModel: StatisticsViewModel by viewModels()
    private val subjectViewModel: SubjectViewModel by viewModels()
    private val topicViewModel: TopicViewModel by viewModels()

    private lateinit var tvGreeting: TextView
    private lateinit var tvDayStreak: TextView
    private lateinit var tvStudyTime: TextView
    private lateinit var tvTopicsDone: TextView
    private lateinit var tvSessions: TextView

    private lateinit var cardContinueLearning: MaterialCardView
    private lateinit var chipSubjectName: Chip
    private lateinit var tvLastActive: TextView
    private lateinit var tvContinueTopicName: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var progressContinue: ProgressBar
    private lateinit var btnResume: Button
    private lateinit var btnQuickStart: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvDayStreak = view.findViewById(R.id.tvDayStreak)
        tvStudyTime = view.findViewById(R.id.tvStudyTime)
        tvTopicsDone = view.findViewById(R.id.tvTopicsDone)
        tvSessions = view.findViewById(R.id.tvSessions)

        cardContinueLearning = view.findViewById(R.id.cardContinueLearning)
        chipSubjectName = view.findViewById(R.id.chipSubjectName)
        tvLastActive = view.findViewById(R.id.tvLastActive)
        tvContinueTopicName = view.findViewById(R.id.tvContinueTopicName)
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent)
        progressContinue = view.findViewById(R.id.progressContinue)
        btnResume = view.findViewById(R.id.btnResume)
        btnQuickStart = view.findViewById(R.id.btnQuickStart)

        // Setup greeting
        tvGreeting.text = getGreeting()

        // Observe statistics
        statisticsViewModel.currentStreak.observe(viewLifecycleOwner) { streak ->
            tvDayStreak.text = streak.toString()
        }

        statisticsViewModel.totalStudyTime.observe(viewLifecycleOwner) { time ->
            tvStudyTime.text = time
        }

        statisticsViewModel.totalTopicsCompleted.observe(viewLifecycleOwner) { completed ->
            tvTopicsDone.text = completed.toString()
        }

        statisticsViewModel.totalSessions.observe(viewLifecycleOwner) { sessions ->
            tvSessions.text = sessions.toString()
        }

        // Observe sessions untuk "continue learning"
        statisticsViewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (sessions.isEmpty()) {
                    cardContinueLearning.visibility = View.GONE
                    // Tampilkan empty state message jika perlu
                } else {
                    // Ambil session terakhir
                    val lastSession = sessions.filterIsInstance<com.example.studybloom.data.entity.StudySession>()
                        .maxByOrNull { it.sessionDate } ?: return@launch

                    // Ambil topic dari session
                    val topic = topicViewModel.getTopicById(lastSession.topicId)
                    topic?.let { t ->
                        // Ambil subject
                        val subject = subjectViewModel.getSubjectById(t.subjectId)
                        subject?.let { s ->
                            chipSubjectName.text = s.name
                            tvContinueTopicName.text = t.name

                            // Format last active
                            val lastActiveText = formatLastActiveTime(lastSession.sessionDate)
                            tvLastActive.text = "Last active $lastActiveText"

                            // Progress (simple: berapa % complete)
                            val progress = if (t.completed) 100 else 65
                            tvProgressPercent.text = "$progress%"
                            progressContinue.progress = progress

                            // Resume button click
                            btnResume.setOnClickListener {
                                val intent = Intent(requireActivity(), StudySessionActivity::class.java)
                                intent.putExtra("TOPIC_ID", t.id)
                                intent.putExtra("TOPIC_NAME", t.name)
                                intent.putExtra("SUBJECT_NAME", s.name)
                                startActivity(intent)
                            }

                            cardContinueLearning.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        // Quick Start button
        btnQuickStart.setOnClickListener {
            showQuickStartDialog()
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning!"
            hour < 18 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
    }

    private fun formatLastActiveTime(sessionDate: String): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = formatter.parse(sessionDate) ?: return "sometime"
            val today = Date()
            val diffMs = today.time - date.time
            val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()

            when {
                days == 0 -> "today"
                days == 1 -> "yesterday"
                days < 7 -> "$days days ago"
                else -> formatter.format(date)
            }
        } catch (e: Exception) {
            "sometime"
        }
    }

    private fun showQuickStartDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Ambil semua subjects
            subjectViewModel.allSubjects.observe(viewLifecycleOwner) { subjects ->
                val subjectNames = subjects.map { it.name }.toTypedArray()
                val subjectIds = subjects.map { it.id }.toIntArray()

                AlertDialog.Builder(requireActivity())
                    .setTitle("Select Subject")
                    .setItems(subjectNames) { _, which ->
                        val selectedSubjectId = subjectIds[which]
                        val selectedSubjectName = subjectNames[which]
                        showTopicSelectionDialog(selectedSubjectId, selectedSubjectName)
                    }
                    .show()
            }
        }
    }

    private fun showTopicSelectionDialog(subjectId: Int, subjectName: String) {
        topicViewModel.loadTopics(subjectId)
        topicViewModel.topics.observe(viewLifecycleOwner) { topics ->
            val topicNames = topics.map { it.name }.toTypedArray()
            val topicIds = topics.map { it.id }.toIntArray()

            if (topicNames.isEmpty()) {
                Toast.makeText(requireActivity(), "No topics in this subject", Toast.LENGTH_SHORT).show()
                return@observe
            }

            AlertDialog.Builder(requireActivity())
                .setTitle("Select Topic")
                .setItems(topicNames) { _, which ->
                    val selectedTopicId = topicIds[which]
                    val selectedTopicName = topicNames[which]

                    val intent = Intent(requireActivity(), StudySessionActivity::class.java)
                    intent.putExtra("TOPIC_ID", selectedTopicId)
                    intent.putExtra("TOPIC_NAME", selectedTopicName)
                    intent.putExtra("SUBJECT_NAME", subjectName)
                    startActivity(intent)
                }
                .show()
        }
    }
}
