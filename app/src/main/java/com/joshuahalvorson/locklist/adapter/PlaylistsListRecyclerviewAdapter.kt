package com.joshuahalvorson.locklist.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.model.Playlist
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlists_list_element_layout.view.*
import java.lang.Exception

class PlaylistsListRecyclerviewAdapter(
        private val isDeleting: Boolean,
        private val playlists: MutableList<Playlist>,
        private val callback: OnListItemClick
) : RecyclerView.Adapter<PlaylistsListRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.playlists_list_element_layout, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindModel(playlists[position], isDeleting, callback)
    }

    override fun getItemCount() = playlists.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val parent: ConstraintLayout = view.playlist_item_parent
        private val playlistName: TextView = view.playlists_list_element_name
        private val playlistVideos: TextView = view.playlist_videos
        private val playlistThumbnail: ImageView = view.playlist_thumbnail
        private val deletePlaylistButton: ImageButton = view.delete_playlist_button
        private val playlistStatusImage: ImageView = view.playlist_status_image
        private val userPlaylistImage: ImageView = view.user_playlist_image

        fun bindModel(playlist: Playlist, isDeleting: Boolean, callback: OnListItemClick) {
            itemView.playlist_loading_circle.visibility = View.VISIBLE
            playlistName.text = playlist.playlistName
            playlistVideos.text = "${playlist.playlistVideoCount} videos"
            Picasso.get()
                    .load(playlist.playlistThumbnail)
                    .error(R.drawable.ic_broken_image_black_24dp)
                    .placeholder(R.drawable.placeholder_120x_90x)
                    .into(playlistThumbnail, object: Callback{
                        override fun onSuccess() {
                            itemView.playlist_loading_circle.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            Log.i("playlistImageLoad", e.toString())
                        }
                    })
            if (isDeleting) {
                deletePlaylistButton.visibility = View.VISIBLE
                deletePlaylistButton.setOnClickListener { callback.onListItemClick(playlist) }
            } else {
                parent.setOnClickListener { callback.onListItemClick(playlist) }
            }

            when (playlist.privacyStatus) {
                "public" -> playlistStatusImage.setImageResource(R.drawable.ic_playlist_public)
                "unlisted" -> playlistStatusImage.setImageResource(R.drawable.ic_playlist_unlisted)
                "private" -> playlistStatusImage.setImageResource(R.drawable.ic_playlist_private)
            }

            if (playlist.isRemote) {
                userPlaylistImage.visibility = View.VISIBLE
            }
        }
    }

    interface OnListItemClick {
        fun onListItemClick(playlist: Playlist?)
    }
}