package com.joshuahalvorson.safeyoutube.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.model.PlaylistResultOverview;

import java.util.ArrayList;

public class PlaylistsListRecyclerviewAdapter extends RecyclerView.Adapter<PlaylistsListRecyclerviewAdapter.ViewHolder> {
    private ArrayList<String> playlistItems;
    private OnListItemClick callback;

    public PlaylistsListRecyclerviewAdapter(ArrayList<String> items, OnListItemClick callback) {
        this.playlistItems = items;
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
        final String playlist = playlistItems.get(i);
        viewHolder.playlistName.setText(playlist);
        viewHolder.playlistName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onListItemClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView playlistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlists_list_element_name);
        }
    }

    public interface OnListItemClick {
        void onListItemClick(String playlistId);
    }
}
