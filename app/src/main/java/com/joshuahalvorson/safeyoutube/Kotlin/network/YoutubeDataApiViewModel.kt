package com.joshuahalvorson.safeyoutube.Kotlin.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.PlaylistResultOverview
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.VideoInfo
import com.joshuahalvorson.safeyoutube.Kotlin.network.YoutubeDataApiRepository;

class YoutubeDataApiViewModel : ViewModel() {
    private var playlistOverview: MutableLiveData<PlaylistResultOverview>? = null
    private var playlistInfo: MutableLiveData<PlaylistResultOverview>? = null
    private var videoInfo: MutableLiveData<VideoInfo>? = null

    fun getPlaylistOverview(playlistId: String): MutableLiveData<PlaylistResultOverview>? {
        playlistOverview = getPlaylistOverview(playlistId)
        return playlistOverview
    }

    fun getPlaylistInfo(playlistId: String): MutableLiveData<PlaylistResultOverview>? {
        playlistInfo = getPlaylistInfo(playlistId)
        return playlistInfo
    }

    fun getVideoInfo(videoId: String): MutableLiveData<VideoInfo>? {
        videoInfo = getVideoInfo(videoId)
        return videoInfo
    }

}