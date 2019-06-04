package com.joshuahalvorson.safeyoutube.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.database.Playlist
import com.joshuahalvorson.safeyoutube.database.PlaylistDatabase
import com.joshuahalvorson.safeyoutube.util.SharedPrefsHelper
import kotlinx.android.synthetic.main.fragment_remove_playlist.*

class RemovePlaylistFragment : androidx.fragment.app.Fragment() {
    private var adapter: PlaylistsListRecyclerviewAdapter? = null
    private var db: PlaylistDatabase? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remove_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val playlists = ArrayList<Playlist>()
        db = context?.let {
            Room.databaseBuilder<PlaylistDatabase>(it,
                PlaylistDatabase::class.java, getString(R.string.database_playlist_name)).build()
        }
        val sharedPrefsHelper = SharedPrefsHelper(activity?.getSharedPreferences(
                SharedPrefsHelper.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE))
        val ids = sharedPrefsHelper.get(SharedPrefsHelper.ACCOUNT_PLAYLISTS_KEY, "")?.split(", ")

        adapter = ids?.let {
            PlaylistsListRecyclerviewAdapter(true, playlists, object : PlaylistsListRecyclerviewAdapter.OnListItemClick {
                override fun onListItemClick(playlist: Playlist?) {
                    if (playlist != null) {
                        Thread(Runnable {
                            db?.playlistDao()?.delete(playlist)
                            playlists.remove(playlist)
                            activity?.runOnUiThread {
                                adapter?.notifyDataSetChanged()
                            }
                        }).start()
                    }
                }
            }, it)
        }

        playlists_to_remove_list.layoutManager = LinearLayoutManager(context)
        playlists_to_remove_list.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        playlists_to_remove_list.adapter = adapter

        Thread(Runnable {
            val tempPlaylists = db?.playlistDao()?.getAllPlaylists()
            activity?.runOnUiThread {
                playlists.clear()
                tempPlaylists?.let { playlists.addAll(it) }
                adapter?.notifyDataSetChanged()
            }
        }).start()
    }

    override fun onDetach() {
        super.onDetach()
        db?.close()
    }
}
