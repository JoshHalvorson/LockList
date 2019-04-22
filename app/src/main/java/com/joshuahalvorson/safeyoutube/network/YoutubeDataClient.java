package com.joshuahalvorson.safeyoutube.network;

import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;
import com.joshuahalvorson.safeyoutube.model.VideoInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeDataClient {
    public static final String BASE_URL = "https://www.googleapis.com/youtube/v3/";

    @GET("playlistItems")
    Call<PlaylistResultOverview> getPlaylistOverview(@Query("part") String part,
                                                     @Query("playlistId") String playlistId,
                                                     @Query("maxResults") String maxResults,
                                                     @Query("key") String key);

    @GET("playlists")
    Call<PlaylistResultOverview> getPlaylistInfo(@Query("part") String part,
                                                 @Query("id") String playlistId,
                                                 @Query("key") String key);

    @GET("videos")
    Call<VideoInfo> getVideoInfo(@Query("part") String part,
                                 @Query("id") String playlistId,
                                 @Query("key") String key);

}
