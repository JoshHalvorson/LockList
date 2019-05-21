package com.joshuahalvorson.safeyoutube.Kotlin.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.R

class PlaylistsListRecyclerviewAdapter(
        private val isDeleting: Boolean, private val playlists: MutableList<Playlist>, private val callback: OnListItemClick
): RecyclerView.Adapter<PlaylistsListRecyclerviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.playlists_list_element_layout, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val playlist = playlists.get(position)
        viewHolder.playlistName.text = playlist.playlistName
        viewHolder.playlistVideos.text = "${playlist.playlistVideoCount} videos"
        Glide.with(viewHolder.playlistThumbnail.getContext())
                .load(playlist.playlistThumbnail)
                .into(viewHolder.playlistThumbnail)
        if (isDeleting) {
            viewHolder.deletePlaylistButton.visibility = View.VISIBLE
            viewHolder.deletePlaylistButton.setOnClickListener(View.OnClickListener { callback.onListItemClick(playlist) })
        } else {
            viewHolder.parent.setOnClickListener(View.OnClickListener { callback.onListItemClick(playlist) })
        }
        when(playlist.playlistStatus){
            "public" -> viewHolder.playlistStatusImage.setImageResource(R.drawable.ic_playlist_public)
            "unlisted" -> viewHolder.playlistStatusImage.setImageResource(R.drawable.ic_playlist_unlisted)
            "private" -> viewHolder.playlistStatusImage.setImageResource(R.drawable.ic_playlist_private)

        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val playlistName: TextView = view.findViewById(R.id.playlists_list_element_name)
        val playlistVideos:TextView = view.findViewById(R.id.playlist_videos)
        val playlistThumbnail: ImageView = view.findViewById(R.id.playlist_thumbnail)
        val parent: ConstraintLayout = view.findViewById(R.id.playlist_item_parent)
        val deletePlaylistButton: ImageButton = view.findViewById(R.id.delete_playlist_button)
        val playlistStatusImage: ImageView = view.findViewById(R.id.playlist_status_image)
    }

    interface OnListItemClick {
        fun onListItemClick(playlist: Playlist?)
    }
}