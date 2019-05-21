package com.joshuahalvorson.safeyoutube.Kotlin.network

import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.PlaylistResultOverview
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models.VideoInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeDataClient {
    @GET("playlistItems")
    fun getPlaylistOverview(@Query("part") part: String,
                            @Query("playlistId") playlistId: String,
                            @Query("maxResults") maxResults: String,
                            @Query("key") key: String): Call<PlaylistResultOverview>

    @GET("playlists")
    fun getPlaylistInfo(@Query("part") part: String,
                        @Query("id") playlistId: String,
                        @Query("key") key: String): Call<PlaylistResultOverview>

    @GET("videos")
    fun getVideoInfo(@Query("part") part: String,
                     @Query("id") playlistId: String,
                     @Query("key") key: String): Call<VideoInfo>

}