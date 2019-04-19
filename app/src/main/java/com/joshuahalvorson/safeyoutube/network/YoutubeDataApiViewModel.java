package com.joshuahalvorson.safeyoutube.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;

public class YoutubeDataApiViewModel extends ViewModel {
    private MutableLiveData<PlaylistResultOverview> liveData;
    private YoutubeDataApiRepository youtubeDataApiRepository;

    public LiveData<PlaylistResultOverview> getPlaylistOverview(String playlistId){
        liveData = YoutubeDataApiRepository.getPlaylistOverview(playlistId);
        return liveData;
    }
}
