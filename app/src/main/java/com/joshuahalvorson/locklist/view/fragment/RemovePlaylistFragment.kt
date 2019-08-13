package com.joshuahalvorson.locklist.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.locklist.database.PlaylistDatabase
import com.joshuahalvorson.locklist.model.Playlist
import com.joshuahalvorson.locklist.util.SharedPrefsHelper
import kotlinx.android.synthetic.main.fragment_remove_playlist.*

class RemovePlaylistFragment : androidx.fragment.app.Fragment() {
    private var adapter: PlaylistsListRecyclerviewAdapter? = null
    private var db: PlaylistDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remove_playlist, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.title = "Settings"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.title = "Delete local playlist"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val playlists = ArrayList<Playlist>()
        db = context?.let {
            Room.databaseBuilder<PlaylistDatabase>(it,
                    PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        }

        adapter = PlaylistsListRecyclerviewAdapter(true, playlists, object : PlaylistsListRecyclerviewAdapter.OnListItemClick {
            override fun onListItemClick(playlist: Playlist?) {
                if (playlist != null) {
                    Thread(Runnable {
                        /*if (playlist.isRemote) {
                            db?.remotePlaylistDao()?.deletePlaylistById(playlist.playlistId)
                        } else {
                            db?.localPlaylistDao()?.deletePlaylistById(playlist.playlistId)
                        }*/
                        db?.localPlaylistDao()?.deletePlaylistById(playlist.playlistId)
                        playlists.remove(playlist)
                        activity?.runOnUiThread {
                            if (playlists.size > 0) {
                                no_playlists_to_remove_text.visibility = View.GONE
                            } else {
                                no_playlists_to_remove_text.visibility = View.VISIBLE
                            }
                            adapter?.notifyDataSetChanged()
                        }
                    }).start()
                }
            }
        })

        playlists_to_remove_list.layoutManager = LinearLayoutManager(context)
        playlists_to_remove_list.adapter = adapter

        Thread(Runnable {
            //val remotePlaylists = db?.remotePlaylistDao()?.getAllPlaylists()
            val localPlaylists = db?.localPlaylistDao()?.getAllPlaylists()

            /*remotePlaylists?.forEach {
                playlists.add(Playlist(
                        it.playlistId,
                        it.playlistName,
                        it.playlistVideoCount,
                        it.playlistThumbnail,
                        it.privacyStatus,
                        true
                ))
            }*/

            localPlaylists?.forEach {
                playlists.add(Playlist(
                        it.playlistId,
                        it.playlistName,
                        it.playlistVideoCount,
                        it.playlistThumbnail,
                        it.privacyStatus,
                        false
                ))
            }

            activity?.runOnUiThread {
                if (playlists.size > 0) {
                    no_playlists_to_remove_text.visibility = View.GONE
                } else {
                    no_playlists_to_remove_text.visibility = View.VISIBLE
                }
                adapter?.notifyDataSetChanged()
            }
        }).start()
    }

    override fun onDetach() {
        super.onDetach()
        db?.close()
    }
}
