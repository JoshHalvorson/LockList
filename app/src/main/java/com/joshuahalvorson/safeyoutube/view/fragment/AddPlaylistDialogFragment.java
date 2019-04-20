package com.joshuahalvorson.safeyoutube.view.fragment;


import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.database.Playlist;
import com.joshuahalvorson.safeyoutube.model.Item;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;
import com.joshuahalvorson.safeyoutube.network.YoutubeDataApiViewModel;

public class AddPlaylistDialogFragment extends DialogFragment {
    public static final String PLAYLIST_URL_KEY = "playlist_url";
    private EditText urlEditText, playlistNameEditText;
    private Button addPlaylistButton;
    private ReturnDataFromDialogFragment dialogFragmentCallback;
    private YoutubeDataApiViewModel viewModel;

    public AddPlaylistDialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_playlist_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        urlEditText = view.findViewById(R.id.video_url_edit_text);
        addPlaylistButton = view.findViewById(R.id.add_playlist_button);
        playlistNameEditText = view.findViewById(R.id.playlist_name_edit_text);

        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel.class);

        if (getArguments() != null){
            dialogFragmentCallback = (ReturnDataFromDialogFragment) getActivity();
            String url = getArguments().getString(PLAYLIST_URL_KEY);
            final String[] urlParts = url.split("list=");
            final String playlistId = urlParts[1];
            LiveData<PlaylistResultOverview> liveData = viewModel.getPlaylistInfo(playlistId);
            liveData.observe(this, new Observer<PlaylistResultOverview>() {
                @Override
                public void onChanged(@Nullable final PlaylistResultOverview playlistInfo) {
                    if(playlistInfo != null){
                        LiveData<PlaylistResultOverview> liveData = viewModel.getPlaylistOverview(playlistId);
                        liveData.observe(getViewLifecycleOwner(), new Observer<PlaylistResultOverview>() {
                            @Override
                            public void onChanged(@Nullable PlaylistResultOverview playlistResultOverview) {
                                if(playlistResultOverview != null){
                                    Item item = playlistInfo.getItems().get(0);
                                    String title = item.getSnippet().getTitle();
                                    dialogFragmentCallback.returnData(new Playlist(
                                            playlistId,
                                            title,
                                            playlistResultOverview.getPageInfo().getTotalResults(),
                                            item.getSnippet().getThumbnails().getDefault().getUrl()));
                                    dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }

        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!urlEditText.getText().toString().equals("") &&
                        !playlistNameEditText.getText().toString().equals("")){
                    String url = urlEditText.getText().toString();
                    final String[] urlParts = url.split("list=");
                    final String playlistId = urlParts[1];
                    LiveData<PlaylistResultOverview> liveData = viewModel.getPlaylistOverview(playlistId);
                    liveData.observe(getViewLifecycleOwner(), new Observer<PlaylistResultOverview>() {
                        @Override
                        public void onChanged(@Nullable PlaylistResultOverview playlistResultOverview) {
                            if(playlistResultOverview != null){
                                Item item = playlistResultOverview.getItems().get(0);
                                dialogFragmentCallback = (ReturnDataFromDialogFragment) getActivity();
                                dialogFragmentCallback.returnData(new Playlist(playlistId,
                                                playlistNameEditText.getText().toString(),
                                                playlistResultOverview.getPageInfo().getTotalResults(),
                                                item.getSnippet().getThumbnails().getDefault().getUrl()));
                                dismiss();
                            }
                        }
                    });

                }
            }
        });
    }

    public interface ReturnDataFromDialogFragment {
        void returnData(Playlist playlist);
    }
}
