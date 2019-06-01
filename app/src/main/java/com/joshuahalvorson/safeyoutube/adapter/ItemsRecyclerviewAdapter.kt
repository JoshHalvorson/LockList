package com.joshuahalvorson.safeyoutube.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.model.Models
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_items_list_element_layout.view.*

class ItemsRecyclerviewAdapter(
        private val items: List<Models.Item>, private val callback: OnVideoClicked
) : RecyclerView.Adapter<ItemsRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.playlist_items_list_element_layout, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindModel(items[position], callback, position)
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val videoParent: ConstraintLayout = itemView.video_parent
        private val videoName: TextView = itemView.video_name
        private val videoThumbnail: ImageView = itemView.video_thumbnail

        fun bindModel(item: Models.Item, callback: OnVideoClicked, position: Int) {
            val name = item.snippet?.title
            videoName.text = name
            Picasso.get()
                    .load(item.snippet?.thumbnails?.default?.url)
                    .into(videoThumbnail)

            videoParent.setOnClickListener { callback.onVideoClicked(position) }
        }
    }

    interface OnVideoClicked {
        fun onVideoClicked(itemIndex: Int)
    }

}