package com.joshuahalvorson.safeyoutube.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.joshuahalvorson.safeyoutube.R
import com.joshuahalvorson.safeyoutube.adapter.ItemsRecyclerviewAdapter
import com.joshuahalvorson.safeyoutube.model.Models
import com.joshuahalvorson.safeyoutube.network.YoutubeDataApiViewModel
import com.joshuahalvorson.safeyoutube.util.Counter
import com.joshuahalvorson.safeyoutube.view.fragment.LoginFragment
import com.joshuahalvorson.safeyoutube.util.YoutubePlayerController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.PlayerUiController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_watch_playlist.*

class WatchPlaylistActivity : AppCompatActivity(), DialogInterface.OnDismissListener {
    override fun onDismiss(dialog: DialogInterface?) {
        hideSystemUI()
    }

    private var items: ArrayList<Models.Item> = ArrayList()
    private var ageValue: Int? = 0

    private lateinit var itemAdapter: ItemsRecyclerviewAdapter
    private lateinit var sharedPref: SharedPreferences
    private lateinit var counter: Counter
    private lateinit var playerController: YoutubePlayerController
    private lateinit var uiController: PlayerUiController
    private lateinit var viewModel: YoutubeDataApiViewModel
    private lateinit var disposable: CompositeDisposable

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_playlist)

        disposable = CompositeDisposable()

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        val youtubePlayerView = findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        lifecycle.addObserver(youtubePlayerView)
        uiController = youtubePlayerView.getPlayerUiController()
        uiController.showMenuButton(false)
        uiController.showYouTubeButton(false)
        youtubePlayerView.addFullScreenListener(object : YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                settings_fab.hide()
            }

            override fun onYouTubePlayerExitFullScreen() {
                settings_fab.show()
            }
        })

        settings_fab.setOnClickListener {
            LoginFragment().show(supportFragmentManager, "login_fragment")
        }

        itemAdapter = ItemsRecyclerviewAdapter(items, object : ItemsRecyclerviewAdapter.OnVideoClicked {
            override fun onVideoClicked(itemIndex: Int) {
                counter = Counter(0, itemIndex, items.size - 1)
                items[itemIndex].contentDetails?.videoId?.let {
                    playerController.playVideo(it)
                }
            }
        })

        videos_list.apply {
            videos_list.layoutManager = LinearLayoutManager(applicationContext)
            videos_list.adapter = itemAdapter
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
        hideSystemUI()
        val currentPlaylistId = sharedPref.getString(getString(R.string.current_playlist_key), null)
        if (currentPlaylistId != null) {
            viewModel.getPlaylistOverview(currentPlaylistId)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe {
                        Log.i("asda", it.items[0].snippet?.title)
                        items.clear()
                        items.addAll(it.items)
                        counter = Counter(0, 0, items.size - 1)
                        itemAdapter.notifyDataSetChanged()
                        no_playlist_text.visibility = View.GONE
                        youtube_player_view.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                counter = Counter(0, 0, items.size - 1)
                                youtube_player_view.visibility = View.VISIBLE
                                items[0].contentDetails?.videoId?.let { youTubePlayer.cueVideo(it, 0F) }
                                playerController = YoutubePlayerController(youTubePlayer)
                                initializeYoutubePlayer(youtube_player_view)
                            }
                        })
                    }?.let { subscribe ->
                        disposable.add(
                                subscribe
                        )
                    }

        } else {
            items.clear()
            youtube_player_view.visibility = View.GONE
            no_playlist_text.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun initializeYoutubePlayer(youtubePlayer: YouTubePlayerView) {
        youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                when (state) {
                    ENDED -> {
                        youTubePlayer.loadVideo(items[counter.increment()].contentDetails?.videoId.toString(), 0F)
                    }
                    else -> return
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
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
            youtube_player_view.enterFullScreen()
            uiController.showFullscreenButton(false)
        } else {
            youtube_player_view.exitFullScreen()
            uiController.showFullscreenButton(true)
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onStop() {
        super.onStop()
        sharedPref.edit().remove(getString(R.string.current_playlist_key)).apply()
    }
}
