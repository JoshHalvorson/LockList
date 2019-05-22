package com.joshuahalvorson.safeyoutube.Kotlin.view.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var items: ArrayList<Models.Item> = ArrayList()
    private var sharedPref: SharedPreferences? = null
    private var ageValue: Int? = 0

    private lateinit var itemAdapter: ItemsRecyclerviewAdapter
    private lateinit var playlistId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_playlist)

        val playlistId = intent.getStringExtra("playlist_id_key")

        val viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        itemAdapter = ItemsRecyclerviewAdapter(items, object : ItemsRecyclerviewAdapter.OnVideoClicked {
            override fun onVideoClicked(itemIndex: Int) {

            }
        })

        videos_list.layoutManager = LinearLayoutManager(applicationContext)

        val liveData = viewModel.getPlaylistOverview(playlistId)
        liveData?.observe(this, androidx.lifecycle.Observer { playlistResultOverview ->
            if (playlistResultOverview != null) {
                videos_list.adapter = itemAdapter
                items.addAll(playlistResultOverview.items)
                itemAdapter.notifyDataSetChanged()

            }
        })

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

    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

}
