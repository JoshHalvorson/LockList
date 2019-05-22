package com.joshuahalvorson.safeyoutube.view.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.adapter.ItemsRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.model.Models
import com.joshuahalvorson.safeyoutube.network.YoutubeDataApiViewModel
import com.joshuahalvorson.safeyoutube.util.Counter
import com.joshuahalvorson.safeyoutube.youtubeplayer.YoutubePlayerController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.PlayerUiController
import kotlinx.android.synthetic.main.activity_watch_playlist.*
import java.util.*

class WatchPlaylistActivity : AppCompatActivity() {
    private var items: ArrayList<Models.Item> = ArrayList()
    private var sharedPref: SharedPreferences? = null
    private var ageValue: Int? = 0

    private lateinit var itemAdapter: ItemsRecyclerviewAdapter
    private lateinit var counter: Counter
    private lateinit var playerController: YoutubePlayerController
    private lateinit var uiController: PlayerUiController
    //private lateinit var youtubePlayerView: YouTubePlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_playlist)

        val playlistId = intent.getStringExtra("playlist_id_key")

        val viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        val youtubePlayerView = findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        lifecycle.addObserver(youtubePlayerView)
        uiController = youtubePlayerView.getPlayerUiController()

        uiController.showMenuButton(false)
        uiController.showYouTubeButton(false)

        itemAdapter = ItemsRecyclerviewAdapter(items, object : ItemsRecyclerviewAdapter.OnVideoClicked {
            override fun onVideoClicked(itemIndex: Int) {
                counter = Counter(0, itemIndex + 1, items.size)
                items[itemIndex].contentDetails?.videoId?.let { playerController.playVideo(it) }
            }
        })

        videos_list.layoutManager = LinearLayoutManager(applicationContext)
        videos_list.adapter = itemAdapter

        val liveData = viewModel.getPlaylistOverview(playlistId)
        liveData?.observe(this, androidx.lifecycle.Observer { playlistResultOverview ->
            if (playlistResultOverview != null) {
                items.addAll(playlistResultOverview.items)
                counter = Counter(0, 0, items.size)
                itemAdapter.notifyDataSetChanged()
                initializeYoutubePlayer(youtubePlayerView, items)
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

    private fun initializeYoutubePlayer(youtubePlayer: YouTubePlayerView, videoIds: ArrayList<Models.Item>) {
        youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                videoIds[counter.increment()].contentDetails?.videoId.let { youTubePlayer.loadVideo(it.toString(), 0F) }
                playerController = YoutubePlayerController(youTubePlayer)
            }

            override fun onStateChange(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer, state: PlayerConstants.PlayerState) {
                when (state) {
                    ENDED -> onReady(youTubePlayer)
                    else -> return
                }
            }

            override fun onError(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer, error: PlayerConstants.PlayerError) {
                Toast.makeText(applicationContext, "Error is: $error", Toast.LENGTH_LONG).show()
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

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

}
