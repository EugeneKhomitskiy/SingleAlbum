package com.example.singlealbum.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.singlealbum.R
import com.example.singlealbum.databinding.TrackBinding
import com.example.singlealbum.dto.Track

interface TrackCallback {
    fun onPlay(track: Track)
}

class TrackAdapter(private val trackCallback: TrackCallback) :
    ListAdapter<Track, TrackViewHolder>(TrackDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = TrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding, trackCallback)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }
}

class TrackViewHolder(
    private val binding: TrackBinding,
    private val trackCallback: TrackCallback
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track) {

        with(binding.track) {
            text = track.file
            setTextColor(
                if (track.isPlaying || track.selected) {
                    ContextCompat.getColor(this.context, R.color.teal_700)
                } else {
                    ContextCompat.getColor(this.context, R.color.black)
                }
            )
        }

        with(binding.button) {
            setImageResource(
                if (track.isPlaying) {
                    R.drawable.ic_baseline_pause_24
                } else {
                    R.drawable.ic_baseline_play_24
                }
            )

            setOnClickListener {
                trackCallback.onPlay(track)
            }
        }
    }
}

class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}