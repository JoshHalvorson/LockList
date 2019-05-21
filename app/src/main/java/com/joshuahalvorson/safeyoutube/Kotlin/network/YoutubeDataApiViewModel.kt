package com.joshuahalvorson.safeyoutube.Kotlin.network

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.PlaylistResultOverview
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.VideoInfo

class YoutubeDataApiViewModel : ViewModel() {
    private var playlistOverview: MutableLiveData<PlaylistResultOverview>? = null
    private var playlistInfo: MutableLiveData<PlaylistResultOverview>? = null
    private var videoInfo: MutableLiveData<VideoInfo>? = null
    val repo = YoutubeDataApiRepository()

    fun getPlaylistOverview(playlistId: String): MutableLiveData<PlaylistResultOverview>? {
        playlistOverview = repo.getPlaylistOverview(playlistId)
        return playlistOverview
    }

    fun getPlaylistInfo(playlistId: String): MutableLiveData<PlaylistResultOverview>? {
        playlistInfo = repo.getPlaylistInfo(playlistId)
        return playlistInfo
    }

    fun getVideoInfo(videoId: String): MutableLiveData<VideoInfo>? {
        videoInfo = repo.getVideoInfo(videoId)
        return videoInfo
    }

}