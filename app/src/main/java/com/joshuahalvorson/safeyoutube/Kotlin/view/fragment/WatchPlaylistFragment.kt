package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.joshuahalvorson.safeyoutube.ApiKey
import com.joshuahalvorson.safeyoutube.Java.view.activity.WatchPlaylistActivity.PLAYLIST_ID_KEY
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistItemsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.database.Playlist
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models
import com.joshuahalvorson.safeyoutube.Kotlin.network.YoutubeDataApiViewModel

import kotlinx.android.synthetic.main.content_watch_playlist.*
import java.util.ArrayList
import com.joshuahalvorson.safeyoutube.R


class WatchPlaylistFragment : Fragment() {
    private var items: ArrayList<Models.Item> = ArrayList()
    private var sharedPref: SharedPreferences? = null
    private var ageValue: Int? = 0
    private var mYoutubePlayer: YouTubePlayer? = null

    private lateinit var adapter: PlaylistItemsListRecyclerviewAdapter
    private lateinit var playlistId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_watch_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        adapter = PlaylistItemsListRecyclerviewAdapter(items, object: PlaylistItemsListRecyclerviewAdapter.OnVideoClicked{
            override fun onVideoClicked(itemIndex: Int) {
                mYoutubePlayer?.loadPlaylist(playlistId, itemIndex, 1)
                mYoutubePlayer?.play()
            }
        })

        var youTubePlayerFragment = childFragmentManager.findFragmentById(R.id.youtube_fragment) as YouTubePlayerSupportFragment

        videos_list.layoutManager = LinearLayoutManager(context)
        videos_list.adapter = adapter

        if (arguments != null) {
            playlistId = arguments!!.getString("playlist_id", "")
            if (youTubePlayerFragment != null) {
                initializeVideo(youTubePlayerFragment, playlistId)
            }
        }

        val liveData = viewModel.getPlaylistOverview(playlistId)
        liveData?.observe(this, Observer<Models.PlaylistResultOverview> { playlistResultOverview ->
            if (playlistResultOverview != null) {
                items.clear()
                items.addAll(playlistResultOverview.items)
                adapter.notifyDataSetChanged()
            }
        })

    }

    override fun onResume() {
        super.onResume()
        sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        ageValue = sharedPref?.getInt(getString(R.string.age_range_key), 1)
    }

    private fun initializeVideo(fragment: YouTubePlayerSupportFragment, playlistId: String) {
        fragment.initialize(ApiKey.KEY, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, b: Boolean) {
                mYoutubePlayer = youTubePlayer
                youTubePlayer.loadPlaylist(playlistId)
                youTubePlayer.setFullscreenControlFlags(0)
                when (ageValue) {
                    0 -> youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
                    1 -> youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                    2 -> youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
                }

            }

            override fun onInitializationFailure(provider: YouTubePlayer.Provider, youTubeInitializationResult: YouTubeInitializationResult) {
                if (youTubeInitializationResult.isUserRecoverableError) {
                    youTubeInitializationResult.getErrorDialog(activity, 1).show()
                } else {
                    val errorMessage = String.format(
                            getString(R.string.error_player), youTubeInitializationResult.toString())
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
