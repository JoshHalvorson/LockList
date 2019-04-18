package com.joshuahalvorson.safeyoutube.view.network;

import com.joshuahalvorson.safeyoutube.view.model.PlaylistResultOverview;

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
}
