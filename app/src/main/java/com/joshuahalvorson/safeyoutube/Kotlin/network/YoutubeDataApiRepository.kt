package com.joshuahalvorson.safeyoutube.Kotlin.network

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.joshuahalvorson.safeyoutube.ApiKey
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.PlaylistResultOverview
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.VideoInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YoutubeDataApiRepository {
    val BASE_URL = "https://www.googleapis.com/youtube/v3/"

    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val client = retrofit.create(YoutubeDataClient::class.java)

    fun getPlaylistOverview(playlistId: String): MutableLiveData<PlaylistResultOverview> {
        val playlistResultOverviewMutableLiveData = MutableLiveData<PlaylistResultOverview>()
        val call = client.getPlaylistOverview("snippet, status", playlistId, "50", ApiKey.KEY)
        call.enqueue(object : Callback<PlaylistResultOverview> {
            override fun onResponse(call: Call<PlaylistResultOverview>, response: Response<PlaylistResultOverview>) {
                playlistResultOverviewMutableLiveData.value = response.body()
            }

            override fun onFailure(call: Call<PlaylistResultOverview>, t: Throwable) {
                Log.i("getPlaylistOverview", t.localizedMessage)
            }
        })
        return playlistResultOverviewMutableLiveData
    }

    fun getPlaylistInfo(playlistId: String): MutableLiveData<PlaylistResultOverview> {
        val playlistInfoMutableLiveData = MutableLiveData<PlaylistResultOverview>()
        val call = client.getPlaylistInfo("snippet, status", playlistId, ApiKey.KEY)
        call.enqueue(object : Callback<PlaylistResultOverview> {
            override fun onResponse(call: Call<PlaylistResultOverview>, response: Response<PlaylistResultOverview>) {
                playlistInfoMutableLiveData.value = response.body()
            }

            override fun onFailure(call: Call<PlaylistResultOverview>, t: Throwable) {
                Log.i("getPlaylistInfo", t.localizedMessage)
            }
        })
        return playlistInfoMutableLiveData
    }

    fun getVideoInfo(videoId: String): MutableLiveData<VideoInfo> {
        val playlistInfoMutableLiveData = MutableLiveData<VideoInfo>()
        val call = client.getVideoInfo("contentDetails", videoId, ApiKey.KEY)
        call.enqueue(object : Callback<VideoInfo> {
            override fun onResponse(call: Call<VideoInfo>, response: Response<VideoInfo>) {
                playlistInfoMutableLiveData.value = response.body()
            }

            override fun onFailure(call: Call<VideoInfo>, t: Throwable) {
                Log.i("getPlaylistInfo", t.localizedMessage)
            }
        })
        return playlistInfoMutableLiveData
    }

}
