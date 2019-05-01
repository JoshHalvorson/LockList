package com.joshuahalvorson.safeyoutube.view.fragment;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.adapter.PlaylistsListRecyclerviewAdapter;
import com.joshuahalvorson.safeyoutube.database.Playlist;
import com.joshuahalvorson.safeyoutube.database.PlaylistDatabase;
import com.joshuahalvorson.safeyoutube.view.activity.WatchPlaylistActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RemovePlaylistDialogFragment extends Fragment {
    private PlaylistsListRecyclerviewAdapter adapter;
    private ArrayList<Playlist> playlists;
    public static PlaylistDatabase db;

    public RemovePlaylistDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_remove_playlist_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        playlists = new ArrayList<>();

        db = Room.databaseBuilder(getContext(),
                PlaylistDatabase.class, "database-playlists").build();

        adapter = new PlaylistsListRecyclerviewAdapter(playlists, new PlaylistsListRecyclerviewAdapter.OnListItemClick() {
            @Override
            public void onListItemClick(final Playlist playlist) {
                Log.i("clicked", playlist.playlistId);
                /*Intent intent = new Intent(getContext(), WatchPlaylistActivity.class);
                intent.putExtra(WatchPlaylistActivity.PLAYLIST_ID_KEY, playlist.playlistId);
                startActivity(intent);*/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.playlistDao().delete(playlist);
                        playlists.remove(playlist);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }
        });

        RecyclerView playlistsListRecyclerview = view.findViewById(R.id.playlists_to_remove_list);
        playlistsListRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        playlistsListRecyclerview.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Playlist> tempPlaylists = db.playlistDao().getAll();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tempPlaylists != null){
                            playlists.clear();
                            playlists.addAll(tempPlaylists);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }
}
