package com.example.studybloom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybloom.R
import com.example.studybloom.data.entity.Topic

class TopicAdapter(
    private var topics: List<Topic>,
    private val onItemClick: (Topic) -> Unit,
    private val onStartSession: (Topic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    // key: topicId, value: total durasi belajar (menit)
    private var durationMap: Map<Int, Int> = emptyMap()

    inner class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivTopicStatus: ImageView = itemView.findViewById(R.id.ivTopicStatus)
        val tvTopicName: TextView = itemView.findViewById(R.id.tvTopicName)
        val tvTopicStatus: TextView = itemView.findViewById(R.id.tvTopicStatus)
        val tvTopicDuration: TextView = itemView.findViewById(R.id.tvTopicDuration)
        val progressTopic: ProgressBar = itemView.findViewById(R.id.progressTopic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]

        // Tampilkan nama topic
        holder.tvTopicName.text = topic.name

        // Ambil durasi total dari durationMap
        val totalDuration = durationMap[topic.id] ?: 0

        // Tampilkan durasi
        holder.tvTopicDuration.text = if (totalDuration > 0) "${totalDuration}m" else "0m"

        // Set status berdasarkan completed dan durasi
        when {
            topic.completed -> {
                holder.ivTopicStatus.setImageResource(R.drawable.ic_topic_completed)
                holder.tvTopicStatus.text = "Completed"
                holder.progressTopic.progress = 100
            }
            totalDuration > 0 -> {
                holder.ivTopicStatus.setImageResource(R.drawable.ic_topic_in_progress)
                holder.tvTopicStatus.text = "In Progress"
                holder.progressTopic.progress = 50
            }
            else -> {
                holder.ivTopicStatus.setImageResource(R.drawable.ic_topic_not_started)
                holder.tvTopicStatus.text = "Not Started"
                holder.progressTopic.progress = 0
            }
        }

        // Klik item → buka form edit
        holder.itemView.setOnClickListener {
            onItemClick(topic)
        }

        // Long click → mulai sesi belajar
        holder.itemView.setOnLongClickListener {
            onStartSession(topic)
            true
        }
    }

    override fun getItemCount(): Int = topics.size

    fun updateList(newList: List<Topic>) {
        topics = newList
        notifyDataSetChanged()
    }

    fun updateDurationMap(newMap: Map<Int, Int>) {
        durationMap = newMap
        notifyDataSetChanged()
    }
}