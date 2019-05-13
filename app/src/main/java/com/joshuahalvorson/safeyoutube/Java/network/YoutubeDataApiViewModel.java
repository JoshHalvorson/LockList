package com.joshuahalvorson.safeyoutube.Java.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.joshuahalvorson.safeyoutube.Java.model.PlaylistResultOverview;
import com.joshuahalvorson.safeyoutube.Java.model.VideoInfo;

public class YoutubeDataApiViewModel extends ViewModel {
    private MutableLiveData<PlaylistResultOverview> playlistOverview;
    private MutableLiveData<PlaylistResultOverview> playlistInfo;
    private MutableLiveData<VideoInfo> videoInfo;
    private YoutubeDataApiRepository youtubeDataApiRepository;

    public LiveData<PlaylistResultOverview> getPlaylistOverview(String playlistId){
        playlistOverview = YoutubeDataApiRepository.getPlaylistOverview(playlistId);
        return playlistOverview;
    }

    public LiveData<PlaylistResultOverview> getPlaylistInfo(String playlistId){
        playlistInfo = YoutubeDataApiRepository.getPlaylistInfo(playlistId);
        return playlistInfo;
    }

    public LiveData<VideoInfo> getVideoInfo(String videoId){
        videoInfo = YoutubeDataApiRepository.getVideoInfo(videoId);
        return videoInfo;
    }
}
