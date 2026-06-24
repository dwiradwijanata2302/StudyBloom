package com.example.studybloom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybloom.R
import com.example.studybloom.data.entity.Subject

class SubjectAdapter(
    private var subjects: List<Subject>,
    private val onItemClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    // Data tambahan untuk progress tiap subject
    // key: subjectId, value: Pair(completedTopics, totalTopics)
    private var progressMap: Map<Int, Pair<Int, Int>> = emptyMap()

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        val tvTopicsProgress: TextView = itemView.findViewById(R.id.tvTopicsProgress)
        val tvProgressPercent: TextView = itemView.findViewById(R.id.tvProgressPercent)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressSubject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]

        // Tampilkan nama subject
        holder.tvSubjectName.text = subject.name

        // Ambil data progress dari progressMap
        val progress = progressMap[subject.id]
        val completed = progress?.first ?: 0
        val total = progress?.second ?: 0

        // Hitung persentase
        val percent = if (total > 0) (completed * 100 / total) else 0

        // Tampilkan progress
        holder.tvTopicsProgress.text = "$completed of $total topics completed"
        holder.tvProgressPercent.text = "$percent%"
        holder.progressBar.progress = percent

        // Klik item → buka form edit
        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }
    }

    override fun getItemCount(): Int = subjects.size

    // Dipanggil saat data subject berubah
    fun updateList(newList: List<Subject>) {
        subjects = newList
        notifyDataSetChanged()
    }

    // Dipanggil saat data progress berubah
    fun updateProgress(newProgressMap: Map<Int, Pair<Int, Int>>) {
        progressMap = newProgressMap
        notifyDataSetChanged()
    }
}