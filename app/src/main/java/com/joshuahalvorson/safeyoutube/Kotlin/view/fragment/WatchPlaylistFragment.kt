package com.joshuahalvorson.safeyoutube.Kotlin.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.PlaylistItem
import com.joshuahalvorson.safeyoutube.ApiKey
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistItemsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.network.YoutubeDataApiViewModel

import kotlinx.android.synthetic.main.content_watch_playlist.*
import com.joshuahalvorson.safeyoutube.R
import kotlinx.io.IOException
import java.util.*

class WatchPlaylistFragment : Fragment() {
    private var items: ArrayList<PlaylistItem> = ArrayList()
    private var sharedPref: SharedPreferences? = null
    private var ageValue: Int? = 0
    private var mYoutubePlayer: YouTubePlayer? = null

    private lateinit var adapter: PlaylistItemsListRecyclerviewAdapter
    private lateinit var playlistId: String
    private lateinit var googleAccountCredential: GoogleAccountCredential
    private lateinit var youtubePlayerFragment: YouTubePlayerSupportFragment

    private val playerStateChangeListener = object : YouTubePlayer.PlayerStateChangeListener {
        override fun onAdStarted() {
        }

        override fun onLoading() {
        }

        override fun onVideoStarted() {
        }

        override fun onLoaded(p0: String?) {
        }

        override fun onVideoEnded() {
        }

        override fun onError(p0: YouTubePlayer.ErrorReason?) {
            var toastText: String = ""
            when(p0.toString()){
                "INTERNAL_ERROR" -> toastText = "Can't play this playlist, make sure it's not private on youtube"
                "NOT_PLAYABLE" -> toastText = "Private videos can't be played"
            }
            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_watch_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PlaylistItemsListRecyclerviewAdapter(items, object: PlaylistItemsListRecyclerviewAdapter.OnVideoClicked{
            override fun onVideoClicked(itemIndex: Int) {
                mYoutubePlayer?.loadPlaylist(playlistId, itemIndex, 1)
                mYoutubePlayer?.play()
            }
        })

        youtubePlayerFragment = childFragmentManager.findFragmentById(R.id.youtube_fragment) as YouTubePlayerSupportFragment

        videos_list.layoutManager = LinearLayoutManager(context)
        videos_list.adapter = adapter

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(*arrayOf(YouTubeScopes.YOUTUBE_READONLY)))
                .setBackOff(ExponentialBackOff())
        val accountName = activity?.getPreferences(Context.MODE_PRIVATE)
                ?.getString("account_name", null);
        if (accountName != null) {
            googleAccountCredential.selectedAccountName = accountName;
        }else{
            //not logged in
        }

        if (arguments != null) {
            playlistId = arguments!!.getString("playlist_id", "")
            if(accountName != null){
                GetPlaylistItemsTask().execute(playlistId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        ageValue = sharedPref?.getInt(getString(R.string.age_range_key), 1)
    }

    private fun initializeVideo(isPlaylist: Boolean, fragment: YouTubePlayerSupportFragment, id: String) {
        fragment.initialize(ApiKey.KEY, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, b: Boolean) {
                mYoutubePlayer = youTubePlayer
                if(isPlaylist){
                    youTubePlayer.loadPlaylist(id)
                }else{
                    youTubePlayer.loadVideo(id)
                }
                youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener)
                youTubePlayer.fullscreenControlFlags = 0
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

    private inner class GetPlaylistItemsTask : AsyncTask<String, Void, ArrayList<PlaylistItem>>() {
        private var youtubeService: YouTube? = null
        private var lastError: Exception? = null

        override fun doInBackground(vararg params: String): ArrayList<PlaylistItem>? {
            try {
                return getDataFromApi(params[0])
            } catch (e: Exception) {
                lastError = e
                cancel(true)
                return null
            }

        }

        @Throws(IOException::class)
        private fun getDataFromApi(playlistId: String): ArrayList<PlaylistItem> {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            youtubeService = YouTube.Builder(
                    transport, jsonFactory, googleAccountCredential)
                    .setApplicationName("SafeYoutube")
                    .build()

            val tempList = ArrayList<PlaylistItem>()

            val result = youtubeService?.playlistItems()?.list("snippet, contentDetails")
                    ?.setPlaylistId(playlistId)
                    ?.setMaxResults(50.toLong())
                    ?.execute()

            result?.items?.forEach {
                tempList.add(it)
            }
            return tempList
        }

        override fun onPreExecute() {
            //mProgress.show()
        }

        override fun onPostExecute(output: ArrayList<PlaylistItem>?) {
            //mProgress.hide()
            if (output == null || output.size == 0) {

            } else {
                items.addAll(output)
                adapter.notifyDataSetChanged()
                initializeVideo(true, youtubePlayerFragment, playlistId)
            }
        }

        override fun onCancelled() {
            //mProgress.hide()
            if (lastError != null) {
                if (lastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            (lastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                } else if (lastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                            (lastError as UserRecoverableAuthIOException).intent,
                            1003)
                } else {
                    Toast.makeText(context, "The following error occurred:\n" + lastError!!.message, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Request cancelled.", Toast.LENGTH_LONG).show()
            }
        }
    }

    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                1002)
        dialog.show()
    }
}
