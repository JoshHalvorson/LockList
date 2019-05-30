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

class PlaylistsListRecyclerviewAdapter(
        private val isDeleting: Boolean,
        private val playlists: MutableList<Playlist>,
        private val callback: OnListItemClick,
        private val ids: List<String>?
) : RecyclerView.Adapter<PlaylistsListRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.playlists_list_element_layout, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val playlist = playlists.get(position)
        viewHolder.playlistName.text = playlist.playlistName
        viewHolder.playlistVideos.text = "${playlist.playlistVideoCount} videos"
        Picasso.get()
                .load(playlist.playlistThumbnail)
                .into(viewHolder.playlistThumbnail)
        if (isDeleting) {
            viewHolder.deletePlaylistButton.visibility = View.VISIBLE
            viewHolder.deletePlaylistButton.setOnClickListener { callback.onListItemClick(playlist) }
        } else {
            viewHolder.parent.setOnClickListener { callback.onListItemClick(playlist) }
        }

        when (playlist.privacyStatus) {
            "public" -> viewHolder.playlistStatusImage.setImageResource(R.drawable.ic_playlist_public)
            "unlisted" -> viewHolder.playlistStatusImage.setImageResource(R.drawable.ic_playlist_unlisted)
            "private" -> viewHolder.playlistStatusImage.setImageResource(R.drawable.ic_playlist_private)
        }

        if (ids != null) {
            if (ids.contains(playlist.playlistId)) {
                viewHolder.userPlaylistImage.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = view.findViewById(R.id.playlists_list_element_name)
        val playlistVideos: TextView = view.findViewById(R.id.playlist_videos)
        val playlistThumbnail: ImageView = view.findViewById(R.id.playlist_thumbnail)
        val parent: ConstraintLayout = view.findViewById(R.id.playlist_item_parent)
        val deletePlaylistButton: ImageButton = view.findViewById(R.id.delete_playlist_button)
        val playlistStatusImage: ImageView = view.findViewById(R.id.playlist_status_image)
        val userPlaylistImage: ImageView = view.findViewById(R.id.user_playlist_image)
    }

    interface OnListItemClick {
        fun onListItemClick(playlist: Playlist?)
    }
}