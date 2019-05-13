package com.joshuahalvorson.safeyoutube.view.fragment;


import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.database.Playlist;
import com.joshuahalvorson.safeyoutube.model.Item;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;
import com.joshuahalvorson.safeyoutube.network.YoutubeDataApiViewModel;

public class AddPlaylistDialogFragment extends DialogFragment {
    public static final String PLAYLIST_URL_KEY = "playlist_url";
    public static final String SHOW_FRAG_KEY = "show_frag";
    private EditText urlEditText;
    private Button addPlaylistButton;
    private ConstraintLayout parentView;
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
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            if(!getArguments().getBoolean(SHOW_FRAG_KEY)) {
                parentView.setVisibility(View.GONE);
                Window window = getDialog().getWindow();
                WindowManager.LayoutParams windowParams = window.getAttributes();
                windowParams.dimAmount = 0.0f;
                windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(windowParams);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        urlEditText = view.findViewById(R.id.video_url_edit_text);
        addPlaylistButton = view.findViewById(R.id.add_playlist_button);
        parentView = view.findViewById(R.id.add_playlist_frag_parent);
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
                if(!urlEditText.getText().toString().equals("")){
                    String url = urlEditText.getText().toString();
                    final String[] urlParts = url.split("list=");
                    final String playlistId = urlParts[1];
                    LiveData<PlaylistResultOverview> liveData = viewModel.getPlaylistInfo(playlistId);
                    liveData.observe(getViewLifecycleOwner(), new Observer<PlaylistResultOverview>() {
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
                                            dialogFragmentCallback = (ReturnDataFromDialogFragment) getActivity();
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
            }
        });
    }

    public interface ReturnDataFromDialogFragment {
        void returnData(Playlist playlist);
    }
}
