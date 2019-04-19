package com.joshuahalvorson.safeyoutube.network;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.joshuahalvorson.safeyoutube.ApiKey;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class YoutubeDataApiRepository {
    private static final String TAG = "YoutubeDataApiRepository";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(YoutubeDataClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static YoutubeDataClient client = retrofit.create(YoutubeDataClient.class);

    public static MutableLiveData<PlaylistResultOverview> getPlaylistOverview(String playlistId){
        final MutableLiveData<PlaylistResultOverview> playlistResultOverviewMutableLiveData = new MutableLiveData<>();
        Call<PlaylistResultOverview> call = client.getPlaylistOverview("snippet", playlistId, "50", ApiKey.KEY);
        call.enqueue(new Callback<PlaylistResultOverview>() {
            @Override
            public void onResponse(Call<PlaylistResultOverview> call, Response<PlaylistResultOverview> response) {
                playlistResultOverviewMutableLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<PlaylistResultOverview> call, Throwable t) {
                Log.i("getPlaylistOverview", t.getLocalizedMessage());
            }
        });
        return playlistResultOverviewMutableLiveData;
    }
}
