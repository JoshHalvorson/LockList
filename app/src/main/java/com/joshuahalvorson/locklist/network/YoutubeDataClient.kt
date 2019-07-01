package com.joshuahalvorson.locklist.network

import com.joshuahalvorson.locklist.model.Models.PlaylistResultOverview
import com.joshuahalvorson.locklist.model.Models.VideoInfo
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeDataClient {
    @GET("playlistItems")
    fun getPlaylistOverview(@Query("part") part: String,
                            @Query("playlistId") playlistId: String,
                            @Query("maxResults") maxResults: String,
                            @Query("key") key: String): Flowable<PlaylistResultOverview>

    @GET("playlists")
    fun getPlaylistInfo(@Query("part") part: String,
                        @Query("id") playlistId: String,
                        @Query("key") key: String): Flowable<PlaylistResultOverview>

    @GET("videos")
    fun getVideoInfo(@Query("part") part: String,
                     @Query("id") playlistId: String,
                     @Query("key") key: String): Flowable<VideoInfo>

}