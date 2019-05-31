package com.joshuahalvorson.safeyoutube.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.database.Playlist
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlists_list_element_layout.view.*

class PlaylistsListRecyclerviewAdapter(
        private val isDeleting: Boolean,
        private val playlists: MutableList<Playlist>,
        private val callback: OnListItemClick,
        private val ids: List<String>
) : RecyclerView.Adapter<PlaylistsListRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.playlists_list_element_layout, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindModel(playlists[position], isDeleting, callback, ids)
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

        fun bindModel(playlist: Playlist, isDeleting: Boolean, callback: OnListItemClick, ids: List<String>){
            playlistName.text = playlist.playlistName
            playlistVideos.text = "${playlist.playlistVideoCount} videos"
            Picasso.get()
                    .load(playlist.playlistThumbnail)
                    .into(playlistThumbnail)
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

            if (ids.contains(playlist.playlistId)) {
                userPlaylistImage.visibility = View.VISIBLE
            }
        }
    }

    interface OnListItemClick {
        fun onListItemClick(playlist: Playlist?)
    }
}