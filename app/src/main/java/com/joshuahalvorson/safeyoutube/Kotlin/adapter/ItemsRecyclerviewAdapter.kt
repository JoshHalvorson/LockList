package com.joshuahalvorson.safeyoutube.Kotlin.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.api.services.youtube.model.PlaylistItem
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models
import com.joshuahalvorson.safeyoutube.R
import java.util.ArrayList


class ItemsRecyclerviewAdapter(
        private val items: List<Models.Item>, private val callback: OnVideoClicked
): RecyclerView.Adapter<ItemsRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ItemsRecyclerviewAdapter.ViewHolder {
        return ViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.playlist_items_list_element_layout, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ItemsRecyclerviewAdapter.ViewHolder, position: Int) {
        val item = items[position]
        val name = item.snippet?.title
        viewHolder.videoName.setText(name)
        Glide.with(viewHolder.videoThumbnail.getContext())
                .load(item.snippet?.thumbnails?.standard?.url)
                .into(viewHolder.videoThumbnail)

        viewHolder.videoParent.setOnClickListener(View.OnClickListener { callback.onVideoClicked(position) })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val videoParent = itemView.findViewById<ConstraintLayout>(R.id.video_parent)
        val videoName = itemView.findViewById<TextView>(R.id.video_name)
        val videoThumbnail = itemView.findViewById<ImageView>(R.id.video_thumbnail)
    }

    interface OnVideoClicked {
        fun onVideoClicked(itemIndex: Int)
    }

}