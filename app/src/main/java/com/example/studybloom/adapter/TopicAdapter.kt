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
    private val onItemClick: (Topic) -> Unit, // Sekarang digunakan untuk Edit (Long Click)
    private val onStartSession: (Topic) -> Unit // Sekarang digunakan untuk Start (Klik Biasa)
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

        holder.tvTopicName.text = topic.name

        // Ambil durasi total dari durationMap
        val totalDurationMinutes = durationMap[topic.id] ?: 0
        val hours = totalDurationMinutes / 60
        val minutes = totalDurationMinutes % 60

        // 3. UI Topic: Ganti keterangan status menjadi "Time Spent" dan tampilkan akumulasi durasi
        holder.tvTopicStatus.text = "Time Spent"
        holder.tvTopicDuration.text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

        // 3. UI Topic: Sembunyikan ProgressBar pada item topik
        holder.progressTopic.visibility = View.GONE

        // Icon tetap sebagai visual status
        if (topic.completed) {
            holder.ivTopicStatus.setImageResource(R.drawable.ic_topic_completed)
        } else {
            holder.ivTopicStatus.setImageResource(R.drawable.ic_topic_in_progress)
        }

        // 4. Navigasi: Klik biasa untuk MULAI sesi (lebih user-friendly)
        holder.itemView.setOnClickListener {
            onStartSession(topic)
        }

        // 4. Navigasi: Klik lama untuk EDIT topik
        holder.itemView.setOnLongClickListener {
            onItemClick(topic)
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
