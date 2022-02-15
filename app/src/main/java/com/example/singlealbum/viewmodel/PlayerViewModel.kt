package com.example.singlealbum.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.singlealbum.api.Api
import com.example.singlealbum.dto.Album
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _data = MutableLiveData<Album>()
    val data: LiveData<Album>
        get() = _data

    val playId = MutableLiveData<Int>()

    init {
        loadTracks()
    }

    private fun loadTracks() = viewModelScope.launch {
        try {
            val response = Api.retrofitService.loadAlbum()
            if (response.isSuccessful) {
                _data.value = response.body()
            }
        } catch (e: Exception) {}
    }

    fun play(id: Int) {
        playId.value = id

        _data.value = data.value?.let { album ->
            album.copy(tracks = album.tracks.map { track ->
                if (id == track.id) {
                    track.copy(isPlaying = !track.isPlaying)
                } else {
                    track.copy(isPlaying = false)
                }
            })
        }
    }
}