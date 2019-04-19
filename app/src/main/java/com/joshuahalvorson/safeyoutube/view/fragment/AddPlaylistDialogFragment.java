package com.joshuahalvorson.safeyoutube.view.fragment;


import android.app.Activity;
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

public class AddPlaylistDialogFragment extends DialogFragment {
    public static final String PLAYLIST_URL_KEY = "playlist_url";
    private EditText urlEditText, playlistNameEditText;
    private Button addPlaylistButton;
    private ReturnDataFromDialogFragment dialogFragmentCallback;

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

        if (getArguments() != null){
            dialogFragmentCallback = (ReturnDataFromDialogFragment) getActivity();
            String url = getArguments().getString(PLAYLIST_URL_KEY);
            String[] urlParts = url.split("list=");
            dialogFragmentCallback.returnData(urlParts[0], urlParts[1]);
            dismiss();
        }

        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!urlEditText.getText().toString().equals("") &&
                        !playlistNameEditText.getText().toString().equals("")){
                    String url = urlEditText.getText().toString();
                    String[] urlParts = url.split("list=");
                    String playlistId = urlParts[1];
                    dialogFragmentCallback = (ReturnDataFromDialogFragment) getActivity();
                    dialogFragmentCallback.returnData(
                            playlistNameEditText.getText().toString(), urlParts[1]);
                    dismiss();
                }
            }
        });
    }

    public interface ReturnDataFromDialogFragment {
        void returnData(String playlistName, String playlistId);
    }
}
