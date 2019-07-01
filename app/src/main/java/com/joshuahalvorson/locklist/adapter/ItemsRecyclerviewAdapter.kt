package com.joshuahalvorson.locklist.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.model.Models
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_items_list_element_layout.view.*
import java.lang.Exception

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
            itemView.item_progress_circle.visibility = View.VISIBLE
            val name = item.snippet?.title
            videoName.text = name
            Picasso.get()
                    .load(item.snippet?.thumbnails?.default?.url)
                    .error(R.drawable.ic_broken_image_black_24dp)
                    .placeholder(R.drawable.placeholder_120x_90x)
                    .into(videoThumbnail, object: Callback{
                        override fun onSuccess() {
                            itemView.item_progress_circle.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            Log.i("itemImageLoad", e.toString())
                        }
                    })

            videoParent.setOnClickListener { callback.onVideoClicked(position) }
        }
    }

    interface OnVideoClicked {
        fun onVideoClicked(itemIndex: Int)
    }

}