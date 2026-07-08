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
    private val onItemClick: (Subject) -> Unit,
    private val onItemLongClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

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
        holder.tvSubjectName.text = subject.name

        // 2. Perubahan Logika UI Subject: Menampilkan jumlah topik
        val progress = progressMap[subject.id]
        val total = progress?.second ?: 0
        holder.tvTopicsProgress.text = "$total Topics"

        // 2. Perubahan Logika UI Subject: Hapus tampilan persentase dan ProgressBar
        holder.tvProgressPercent.visibility = View.GONE
        holder.progressBar.visibility = View.GONE

        // Klik biasa -> Buka daftar topik
        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }

        // Klik lama -> EDIT / HAPUS SUBJECT
        holder.itemView.setOnLongClickListener {
            onItemLongClick(subject)
            true
        }
    }

    override fun getItemCount(): Int = subjects.size

    fun updateList(newList: List<Subject>) {
        subjects = newList
        notifyDataSetChanged()
    }

    fun updateProgress(newProgressMap: Map<Int, Pair<Int, Int>>) {
        progressMap = newProgressMap
        notifyDataSetChanged()
    }
}
