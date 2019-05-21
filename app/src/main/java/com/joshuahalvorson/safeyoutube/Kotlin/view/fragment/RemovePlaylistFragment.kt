package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.fragment_remove_playlist.*

class RemovePlaylistFragment : Fragment() {
    private lateinit var adapter: PlaylistsListRecyclerviewAdapter
    private lateinit var db: PlaylistDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remove_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var playlists = ArrayList<Playlist>()
        db = Room.databaseBuilder<PlaylistDatabase>(context!!,
                PlaylistDatabase::class.java, "database-playlists").build()

        val ids = activity?.getPreferences(Context.MODE_PRIVATE)?.getString("account_playlists", "")?.split(", ")

        adapter = PlaylistsListRecyclerviewAdapter(true, playlists, object : PlaylistsListRecyclerviewAdapter.OnListItemClick {
            override fun onListItemClick(playlist: Playlist?) {
                if (playlist != null) {
                    Thread(Runnable {
                        db.playlistDao().delete(playlist)
                        playlists.remove(playlist)
                        activity?.runOnUiThread {
                            adapter.notifyDataSetChanged()
                        }
                    }).start()
                }
            }
        }, ids)

        playlists_to_remove_list.layoutManager = LinearLayoutManager(context)
        playlists_to_remove_list.adapter = adapter

        Thread(Runnable {
            val tempPlaylists = db.playlistDao().getAllPlaylists()
            activity?.runOnUiThread {
                playlists.clear()
                playlists.addAll(tempPlaylists)
                adapter.notifyDataSetChanged()
            }
        }).start()
    }

    override fun onDetach() {
        super.onDetach()
        db.close()
    }
}
