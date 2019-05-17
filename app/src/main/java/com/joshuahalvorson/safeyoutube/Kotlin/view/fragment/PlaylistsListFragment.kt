package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.fragment_playlists_list.*
import java.util.ArrayList

class PlaylistsListFragment : Fragment() {
    private var listenerPlaylist: OnPlaylistFragmentInteractionListener? = null
    private var playlists: ArrayList<Playlist> = ArrayList()
    private lateinit var adapter: PlaylistsListRecyclerviewAdapter
    var db: PlaylistDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlists_list, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPlaylistFragmentInteractionListener) {
            listenerPlaylist = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnPlaylistFragmentInteractionListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = Room.databaseBuilder(this.context!!,
                PlaylistDatabase::class.java, "database-playlists").build()
        adapter = PlaylistsListRecyclerviewAdapter(false, playlists, object: PlaylistsListRecyclerviewAdapter.OnListItemClick{
            override fun onListItemClick(playlist: Playlist?) {
                val watchFrag = WatchPlaylistFragment()
                var bundle = Bundle()
                if (playlist != null) {
                    bundle.putString("playlist_id", playlist.playlistId)
                    watchFrag.arguments = bundle
                }
                fragmentManager?.beginTransaction()
                        ?.add(R.id.fragment_container, watchFrag)
                        ?.addToBackStack("")
                        ?.commit()
            }
        })
        playlists_list.layoutManager = LinearLayoutManager(context)
        playlists_list.adapter = adapter

        activity?.findViewById<FloatingActionButton>(R.id.add_playlist_button)?.setOnClickListener {
            startAddPlaylistFragment(null, true)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listenerPlaylist = null
    }

    override fun onResume() {
        super.onResume()
        Thread(Runnable {
            val tempPlaylists = db?.playlistDao()?.getAllPlaylists()
            activity?.runOnUiThread {
                if (tempPlaylists != null) {
                    playlists.clear()
                    playlists.addAll(tempPlaylists)
                    adapter.notifyDataSetChanged()
                }
            }
        }).start()
    }

    private fun startAddPlaylistFragment(url: String?, showFrag: Boolean) {
        val addPlaylistDialogFragment = AddPlaylistFragment()
        val bundle: Bundle
        if (url != null) {
            bundle = Bundle()
            bundle.putString("playlist_url", url)
            if (!showFrag) {
                bundle.putBoolean("show_frag", false)
            }
            addPlaylistDialogFragment.arguments = bundle
        }
        addPlaylistDialogFragment.show(fragmentManager, "add_playlist")
    }

    interface OnPlaylistFragmentInteractionListener {
        fun onPlaylistInteraction(playlist: Playlist)
    }
}
