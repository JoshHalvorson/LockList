package com.joshuahalvorson.safeyoutube.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;

public class YoutubeDataApiViewModel extends ViewModel {
    private MutableLiveData<PlaylistResultOverview> playlistOverview;
    private MutableLiveData<PlaylistResultOverview> playlistInfo;
    private YoutubeDataApiRepository youtubeDataApiRepository;

    public LiveData<PlaylistResultOverview> getPlaylistOverview(String playlistId){
        playlistOverview = YoutubeDataApiRepository.getPlaylistOverview(playlistId);
        return playlistOverview;
    }

    public LiveData<PlaylistResultOverview> getPlaylistInfo(String playlistId){
        playlistInfo = YoutubeDataApiRepository.getPlaylistInfo(playlistId);
        return playlistInfo;
    }
}
