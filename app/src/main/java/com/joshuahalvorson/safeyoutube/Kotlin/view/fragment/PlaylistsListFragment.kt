package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.fragment_playlists_list.*
import java.util.ArrayList
import android.content.DialogInterface

class PlaylistsListFragment : Fragment() {
    private var playlists: ArrayList<Playlist> = ArrayList()
    private lateinit var adapter: PlaylistsListRecyclerviewAdapter
    var db: PlaylistDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlists_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(this.context!!,
                PlaylistDatabase::class.java, "database-playlists").build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        if (arguments != null){
            val playlistUrl = arguments!!.getString("playlist_url")
            val showFrag = arguments!!.getBoolean("show_frag")
            if (playlistUrl != null && showFrag != true){
                startAddPlaylistFragment(playlistUrl, showFrag)
            }
        }
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

        addPlaylistDialogFragment.setOnDismissListener(DialogInterface.OnDismissListener {
            updatePlaylistsList()
        })

        addPlaylistDialogFragment.show(fragmentManager, "add_playlist")
    }

    private fun updatePlaylistsList() {
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

    override fun onResume() {
        super.onResume()
        updatePlaylistsList()
    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
    }
}
