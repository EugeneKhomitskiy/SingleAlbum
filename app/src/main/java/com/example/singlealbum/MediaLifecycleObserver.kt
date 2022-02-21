package com.example.singlealbum

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MediaLifecycleObserver : LifecycleObserver {
    var player: MediaPlayer? = MediaPlayer()

    fun play() {
        player?.prepare()
        player?.start()
    }

    fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                player?.pause()
            }
            Lifecycle.Event.ON_STOP -> {
                player?.stop()
                player?.reset()
            }
            Lifecycle.Event.ON_DESTROY -> { source.lifecycle.removeObserver(this) }
            else -> Unit
        }
    }
}