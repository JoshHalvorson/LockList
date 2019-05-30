package com.joshuahalvorson.safeyoutube.network

import androidx.lifecycle.ViewModel
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.joshuahalvorson.safeyoutube.ApiKey
import com.joshuahalvorson.safeyoutube.model.Models.PlaylistResultOverview
import io.reactivex.Flowable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YoutubeDataApiViewModel : ViewModel() {
    private var retrofit: Retrofit? = null
    private var client: YoutubeDataClient? = null

    private fun buildRetrofit(){
        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        client = retrofit?.create(YoutubeDataClient::class.java)
    }

    fun getPlaylistOverview(playlistId: String): Flowable<PlaylistResultOverview>? {
        if (retrofit == null || client == null){
            buildRetrofit()
        }
        return client?.getPlaylistOverview("snippet, status, contentDetails", playlistId, "50", ApiKey.KEY)
    }

    fun getPlaylistInfo(playlistId: String): Flowable<PlaylistResultOverview>? {
        if (retrofit == null || client == null){
            buildRetrofit()
        }
        return client?.getPlaylistInfo("snippet, status", playlistId, ApiKey.KEY)
    }

    companion object {
        const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    }

}