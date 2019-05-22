package com.joshuahalvorson.safeyoutube.Kotlin.view.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.OrientationEventListener
import android.view.View
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
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.ItemsRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.adapter.PlaylistItemsListRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.Kotlin.model.Models
import com.joshuahalvorson.safeyoutube.Kotlin.network.YoutubeDataApiViewModel
import com.joshuahalvorson.safeyoutube.R
import kotlinx.android.synthetic.main.activity_watch_playlist.*
import kotlinx.io.IOException
import java.util.*

class WatchPlaylistActivity : AppCompatActivity() {
    private var youtubeItems: ArrayList<PlaylistItem> = ArrayList()
    private var items: ArrayList<Models.Item> = ArrayList()
    private var sharedPref: SharedPreferences? = null
    private var ageValue: Int? = 0
    private var mYoutubePlayer: YouTubePlayer? = null

    private lateinit var adapter: PlaylistItemsListRecyclerviewAdapter
    private lateinit var itemAdapter: ItemsRecyclerviewAdapter
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
            var toastText = p0.toString()
            when (p0.toString()) {
                "INTERNAL_ERROR" -> toastText = "Can't play this playlist, make sure it's not private on youtube"
                "NOT_PLAYABLE" -> toastText = "Private videos can't be played"
            }
            Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_playlist)

        val playlistId = intent.getStringExtra("playlist_id_key")

        val viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        youtubePlayerFragment = supportFragmentManager.findFragmentById(R.id.youtube_fragment) as YouTubePlayerSupportFragment

        adapter = PlaylistItemsListRecyclerviewAdapter(youtubeItems, object : PlaylistItemsListRecyclerviewAdapter.OnVideoClicked {
            override fun onVideoClicked(itemIndex: Int) {
                mYoutubePlayer?.loadPlaylist(playlistId, itemIndex, 1)
                mYoutubePlayer?.play()
            }
        })

        itemAdapter = ItemsRecyclerviewAdapter(items, object : ItemsRecyclerviewAdapter.OnVideoClicked {
            override fun onVideoClicked(itemIndex: Int) {
                mYoutubePlayer?.loadPlaylist(playlistId, itemIndex, 1)
                mYoutubePlayer?.play()
            }
        })

        videos_list.layoutManager = LinearLayoutManager(applicationContext)

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(YouTubeScopes.YOUTUBE_READONLY))
                .setBackOff(ExponentialBackOff())
        val accountName = getPreferences(Context.MODE_PRIVATE)
                ?.getString("account_name", null)
        if (accountName != null) {
            googleAccountCredential.selectedAccountName = accountName
        } else {
            //not logged in
        }

        if (accountName != null) {
            videos_list.adapter = adapter
            GetPlaylistItemsTask().execute(playlistId)
        } else {
            //when not logged in, user network call method
            val liveData = viewModel.getPlaylistOverview(playlistId)
            liveData?.observe(this, android.arch.lifecycle.Observer { playlistResultOverview ->
                if (playlistResultOverview != null) {
                    videos_list.adapter = itemAdapter
                    items.addAll(playlistResultOverview.items)
                    itemAdapter.notifyDataSetChanged()
                    initializeVideo(youtubePlayerFragment, playlistId)
                }
            })
        }

        val orientationEventListener = object : OrientationEventListener(applicationContext, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation <= 10) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        ageValue = sharedPref?.getInt(getString(R.string.age_range_key), 1)
    }

    private fun initializeVideo(fragment: YouTubePlayerSupportFragment, id: String) {
        fragment.initialize(ApiKey.KEY, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, b: Boolean) {
                youTubePlayer.loadPlaylist(id)
                youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener)
                youTubePlayer.fullscreenControlFlags = 0
                when (ageValue) {
                    0 -> youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
                    1 -> youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                    2 -> youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
                }
                youTubePlayer.fullscreenControlFlags = YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION; YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE; YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                mYoutubePlayer = youTubePlayer
            }

            override fun onInitializationFailure(provider: YouTubePlayer.Provider, youTubeInitializationResult: YouTubeInitializationResult) {
                if (youTubeInitializationResult.isUserRecoverableError) {
                    youTubeInitializationResult.getErrorDialog(this@WatchPlaylistActivity, 1).show()
                } else {
                    val errorMessage = String.format(
                            getString(R.string.error_player), youTubeInitializationResult.toString())
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            super.onBackPressed()
        }
    }

    private inner class GetPlaylistItemsTask : AsyncTask<String, Void, ArrayList<PlaylistItem>>() {
        private var youtubeService: YouTube? = null
        private var lastError: Exception? = null

        override fun doInBackground(vararg params: String): ArrayList<PlaylistItem>? {
            return try {
                getDataFromApi(params[0])
            } catch (e: Exception) {
                lastError = e
                cancel(true)
                null
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
                youtubeItems.addAll(output)
                adapter.notifyDataSetChanged()
                initializeVideo(youtubePlayerFragment, playlistId)
            }
        }

        override fun onCancelled() {
            //mProgress.hide()
            if (lastError != null) {
                when (lastError) {
                    is GooglePlayServicesAvailabilityIOException -> showGooglePlayServicesAvailabilityErrorDialog(
                            (lastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                    is UserRecoverableAuthIOException -> startActivityForResult(
                            (lastError as UserRecoverableAuthIOException).intent,
                            1003)
                    else -> Toast.makeText(applicationContext, "The following error occurred:\n" + lastError!!.message, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(applicationContext, "Request cancelled.", Toast.LENGTH_LONG).show()
            }
        }
    }

    internal fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                1002)
        dialog.show()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mYoutubePlayer?.setFullscreen(true)
            hideSystemUI()
        } else {
            mYoutubePlayer?.setFullscreen(false)
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

}
