package com.joshuahalvorson.safeyoutube.view.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.joshuahalvorson.safeyoutube.view.model.PlaylistResultOverview;

public class YoutubeDataApiViewModel extends ViewModel {
    private MutableLiveData<PlaylistResultOverview> liveData;
    private YoutubeDataApiRepository youtubeDataApiRepository;

    public LiveData<PlaylistResultOverview> getPlaylistOverview(String playlistId, String maxResults){
        liveData = YoutubeDataApiRepository.getPlaylistOverview(playlistId, maxResults);
        return liveData;
    }
}
