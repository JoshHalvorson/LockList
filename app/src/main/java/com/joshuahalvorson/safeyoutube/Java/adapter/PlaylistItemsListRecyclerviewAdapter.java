package com.joshuahalvorson.safeyoutube.Java.adapter;

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
import com.joshuahalvorson.safeyoutube.Java.model.Item;

import java.util.ArrayList;

public class PlaylistItemsListRecyclerviewAdapter extends RecyclerView.Adapter<PlaylistItemsListRecyclerviewAdapter.ViewHolder>{
    private ArrayList<Item> items;
    private OnVideoClicked callback;

    public PlaylistItemsListRecyclerviewAdapter(ArrayList<Item> items, OnVideoClicked callback) {
        this.items = items;
        this.callback = callback;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Item item = items.get(i);
        String name = item.getSnippet().getTitle();
        viewHolder.videoName.setText(name);
        Glide.with(viewHolder.videoThumbnail.getContext())
                .load(item.getSnippet().getThumbnails().getDefault().getUrl())
                .into(viewHolder.videoThumbnail);

        viewHolder.videoParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onVideoClicked(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout videoParent;
        TextView videoName;
        ImageView videoThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoParent = itemView.findViewById(R.id.video_parent);
            videoName = itemView.findViewById(R.id.video_name);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
        }
    }

    public interface OnVideoClicked{
        void onVideoClicked(int itemIndex);
    }
}
