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
    private EditText urlEditText;
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

        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!urlEditText.getText().toString().equals("")){
                    String url = urlEditText.getText().toString();
                    String[] urlParts = url.split("list=");
                    if(urlParts.length == 2){
                        String playlistId = urlParts[1];
                        Log.i("playlistId", playlistId);
                        dialogFragmentCallback = (ReturnDataFromDialogFragment) getActivity();
                        dialogFragmentCallback.returnData(playlistId);
                        dismiss();
                    }
                }
            }
        });
    }

    public interface ReturnDataFromDialogFragment {
        void returnData(String playlistId);
    }
}