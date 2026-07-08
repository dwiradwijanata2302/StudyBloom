package com.example.studybloom.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.studybloom.R
import com.example.studybloom.ui.session.StudySessionActivity
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
    private lateinit var tvSessions: TextView

    private lateinit var cardContinueLearning: MaterialCardView
    private lateinit var tvContinueEmpty: TextView
    private lateinit var chipSubjectName: Chip
    private lateinit var tvLastActive: TextView
    private lateinit var tvContinueTopicName: TextView
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
        tvSessions = view.findViewById(R.id.tvSessions)

        cardContinueLearning = view.findViewById(R.id.cardContinueLearning)
        tvContinueEmpty = view.findViewById(R.id.tvContinueEmpty)
        chipSubjectName = view.findViewById(R.id.chipSubjectName)
        tvLastActive = view.findViewById(R.id.tvLastActive)
        tvContinueTopicName = view.findViewById(R.id.tvContinueTopicName)
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


        statisticsViewModel.totalSessions.observe(viewLifecycleOwner) { sessions ->
            tvSessions.text = sessions.toString()
        }

        // Observe sessions untuk "continue learning"
        statisticsViewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (sessions.isNotEmpty()) {
                    val lastSession = sessions.maxByOrNull { it.sessionDate } ?: return@launch

                    val topic = topicViewModel.getTopicById(lastSession.topicId)
                    topic?.let { t ->
                        val subject = subjectViewModel.getSubjectById(t.subjectId)
                        subject?.let { s ->
                            chipSubjectName.text = s.name
                            tvContinueTopicName.text = t.name
                            tvLastActive.text = "Last active ${formatLastActiveTime(lastSession.sessionDate)}"

                            btnResume.setOnClickListener {
                                val intent = Intent(requireActivity(), StudySessionActivity::class.java)
                                intent.putExtra("TOPIC_ID", t.id)
                                intent.putExtra("TOPIC_NAME", t.name)
                                intent.putExtra("SUBJECT_NAME", s.name)
                                startActivity(intent)
                            }

                            cardContinueLearning.visibility = View.VISIBLE
                            tvContinueEmpty.visibility = View.GONE
                        }
                    }
                } else {
                    cardContinueLearning.visibility = View.GONE
                    tvContinueEmpty.visibility = View.VISIBLE
                }
            }
        }

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
            val diffMs = Date().time - date.time
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
        val subjects = subjectViewModel.allSubjects.value
        if (subjects.isNullOrEmpty()) {
            Toast.makeText(requireActivity(), "Please add a subject first", Toast.LENGTH_SHORT).show()
            return
        }

        val subjectNames = subjects.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireActivity())
            .setTitle("Select Subject")
            .setItems(subjectNames) { _, which ->
                val selectedSubject = subjects[which]
                showTopicSelectionDialog(selectedSubject.id, selectedSubject.name)
            }
            .show()
    }

    private fun showTopicSelectionDialog(subjectId: Int, subjectName: String) {
        topicViewModel.loadTopics(subjectId)
        
        topicViewModel.topics.observe(viewLifecycleOwner, object : androidx.lifecycle.Observer<List<com.example.studybloom.data.entity.Topic>> {
            override fun onChanged(value: List<com.example.studybloom.data.entity.Topic>) {
                topicViewModel.topics.removeObserver(this)
                
                if (value.isEmpty()) {
                    Toast.makeText(requireActivity(), "No topics in $subjectName", Toast.LENGTH_SHORT).show()
                    return
                }

                val topicNames = value.map { it.name }.toTypedArray()
                AlertDialog.Builder(requireActivity())
                    .setTitle("Select Topic in $subjectName")
                    .setItems(topicNames) { _, which ->
                        val selectedTopic = value[which]
                        val intent = Intent(requireActivity(), StudySessionActivity::class.java).apply {
                            putExtra("TOPIC_ID", selectedTopic.id)
                            putExtra("TOPIC_NAME", selectedTopic.name)
                            putExtra("SUBJECT_NAME", subjectName)
                        }
                        startActivity(intent)
                    }
                    .show()
            }
        })
    }
}
