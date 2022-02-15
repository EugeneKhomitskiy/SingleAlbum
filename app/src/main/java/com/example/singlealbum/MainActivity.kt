package com.example.singlealbum

import android.media.AudioAttributes
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import com.example.singlealbum.adapter.TrackAdapter
import com.example.singlealbum.adapter.TrackCallback
import com.example.singlealbum.databinding.ActivityMainBinding
import com.example.singlealbum.dto.Track
import com.example.singlealbum.viewmodel.PlayerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var mediaObserver: MediaLifecycleObserver

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaObserver = MediaLifecycleObserver()

        val adapter = TrackAdapter(object : TrackCallback {
            override fun onPlay(track: Track) {

                if (track.id != viewModel.playId.value) {
                    mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_STOP)
                }

                if (mediaObserver.player?.isPlaying == true) {
                    mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)

                } else {
                    if (track.id != viewModel.playId.value) {
                        mediaObserver.apply {
                            player?.setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                            player?.setDataSource("${BuildConfig.BASE_URL}${track.id}.mp3")
                        }.play()
                    } else {
                        mediaObserver.player?.start()
                    }
                }
                viewModel.play(track.id)
            }
        })
        binding.list.adapter = adapter

        viewModel.data.observe(this) {
            adapter.submitList(it.tracks)
            binding.album.text = it.title
            binding.artistName.text = it.artist
            binding.published.text = it.published
        }

        mediaObserver.player?.setOnCompletionListener {
            var nextId = viewModel.playId.value!!
            mediaObserver.onStateChanged(this, Lifecycle.Event.ON_STOP)
            viewModel.data.value?.let {
                nextId = if (viewModel.playId.value == it.tracks.size) 1 else nextId++
            }
            mediaObserver.apply {
                player?.setDataSource("${BuildConfig.BASE_URL}${nextId}.mp3")
            }.play()
            viewModel.play(nextId)
        }

        lifecycle.addObserver(mediaObserver)
    }

    override fun onPause() {
        super.onPause()
        mediaObserver.onStateChanged(this, Lifecycle.Event.ON_PAUSE)
        viewModel.play(mediaObserver.player?.currentPosition!!)
    }
}