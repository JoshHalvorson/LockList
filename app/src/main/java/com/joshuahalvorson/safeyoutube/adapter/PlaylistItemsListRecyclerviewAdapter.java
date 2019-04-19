package com.joshuahalvorson.safeyoutube.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joshuahalvorson.safeyoutube.R;
import com.joshuahalvorson.safeyoutube.model.Item;

import java.util.ArrayList;

public class PlaylistItemsListRecyclerviewAdapter extends RecyclerView.Adapter<PlaylistItemsListRecyclerviewAdapter.ViewHolder>{
    private ArrayList<Item> items;

    public PlaylistItemsListRecyclerviewAdapter(ArrayList<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new
                ViewHolder(
                        LayoutInflater.from(viewGroup.getContext())
                                .inflate(
                                        R.layout.playlist_items_list_element_layout,
                                        viewGroup,
                                        false
                                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Item item = items.get(i);
        viewHolder.videoName.setText(item.getSnippet().getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView videoName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.video_name);
        }
    }
}
