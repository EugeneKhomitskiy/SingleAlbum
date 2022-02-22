package com.example.singlealbum

import android.media.AudioAttributes
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import com.example.singlealbum.adapter.TrackAdapter
import com.example.singlealbum.adapter.TrackCallback
import com.example.singlealbum.databinding.ActivityMainBinding
import com.example.singlealbum.dto.Track
import com.example.singlealbum.util.time
import com.example.singlealbum.viewmodel.PlayerViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaObserver: MediaLifecycleObserver
    private lateinit var track: Track
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaObserver = MediaLifecycleObserver()

        val adapter = TrackAdapter(object : TrackCallback {
            override fun onPlay(track: Track) {
                play(track)
            }
        })

        binding.list.adapter = adapter

        with(binding) {
            buttonPlay.setOnClickListener {
                play(viewModel.data.value?.tracks!![viewModel.playId.value!! - 1])
            }
            buttonNext.setOnClickListener { playNextTrack() }
            buttonPrev.setOnClickListener { playPrevTrack() }
        }

        viewModel.data.observe(this) {
            adapter.submitList(it.tracks)
            binding.info.text = String.format("%s - %s", it.artist, it.title)
            binding.buttonPlay.setImageResource(
                if (viewModel.isPlaying()) {
                    R.drawable.ic_baseline_play_24
                } else {
                    R.drawable.ic_baseline_pause_24
                }
            )
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaObserver.player?.seekTo(progress)
                binding.timeStart.text = time((mediaObserver.player?.currentPosition?.div(1000))!!)
                binding.timeEnd.text = time((mediaObserver.player?.duration?.div(1000))!!)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        lifecycle.addObserver(mediaObserver)
    }

    fun play(track: Track) {
        mediaObserver.player?.setOnCompletionListener {
            playNextTrack()
        }

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
                    player?.setDataSource("${BuildConfig.BASE_URL}${track.file}")
                }.play()

                initialiseSeekBar()
            } else {
                mediaObserver.player?.start()
            }
        }
        viewModel.play(track.id)
    }

    private fun playNextTrack() {
        viewModel.data.value?.let {
            track = if (viewModel.playId.value == it.tracks.size) {
                it.tracks[0]
            } else {
                it.tracks[viewModel.playId.value!!]
            }
        }
        play(track)
    }

    private fun playPrevTrack() {
        viewModel.data.value?.let {
            track = if (viewModel.playId.value == 1) {
                it.tracks[it.tracks.size - 1]
            } else {
                it.tracks[viewModel.playId.value!! - 2]
            }
        }
        play(track)
    }

    private fun initialiseSeekBar() {
        seekBar.max = mediaObserver.player!!.duration

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    seekBar.progress = mediaObserver.player?.currentPosition!!
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    seekBar.progress = 0
                }
            }
        }, 0)
    }

    override fun onStop() {
        if (mediaObserver.player?.isPlaying == true) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)
        }
        super.onStop()
    }
}