package com.joshuahalvorson.safeyoutube.adapter;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.database.Playlist;

import java.util.ArrayList;

public class PlaylistsListRecyclerviewAdapter extends RecyclerView.Adapter<PlaylistsListRecyclerviewAdapter.ViewHolder> {
    private ArrayList<Playlist> playlists;
    private OnListItemClick callback;

    public PlaylistsListRecyclerviewAdapter(ArrayList<Playlist> playlists, OnListItemClick callback) {
        this.playlists = playlists;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new
                ViewHolder(
                        LayoutInflater.from(viewGroup.getContext())
                                .inflate(
                                        R.layout.playlists_list_element_layout,
                                        viewGroup,
                                        false
                                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Playlist playlist = playlists.get(i);
        viewHolder.playlistName.setText(playlist.playlistName);
        viewHolder.playlistVideos.setText(playlist.playlistVideoCount + " videos");
        Glide.with(viewHolder.playlistThumbnail.getContext())
                .load(playlist.playlistThumbnail)
                .into(viewHolder.playlistThumbnail);
        viewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onListItemClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView playlistName, playlistVideos;
        ImageView playlistThumbnail;
        ConstraintLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlists_list_element_name);
            playlistVideos = itemView.findViewById(R.id.playlist_videos);
            playlistThumbnail = itemView.findViewById(R.id.playlist_thumbnail);
            parent = itemView.findViewById(R.id.playlist_item_parent);
        }
    }

    public interface OnListItemClick {
        void onListItemClick(Playlist playlist);
    }
}
