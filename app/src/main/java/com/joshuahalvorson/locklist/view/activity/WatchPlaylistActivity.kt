package com.joshuahalvorson.locklist.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.stetho.Stetho
import com.joshuahalvorson.locklist.R
import com.joshuahalvorson.locklist.adapter.ItemsRecyclerviewAdapter
import com.joshuahalvorson.locklist.model.Models
import com.joshuahalvorson.locklist.network.YoutubeDataApiViewModel
import com.joshuahalvorson.locklist.util.Counter
import com.joshuahalvorson.locklist.util.SharedPrefsHelper
import com.joshuahalvorson.locklist.util.YoutubePlayerController
import com.joshuahalvorson.locklist.view.fragment.LoginFragment
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
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_watch_playlist.*
import kotlinx.android.synthetic.main.activity_watch_playlist.toolbar

class WatchPlaylistActivity : AppCompatActivity() {

    private var items: ArrayList<Models.Item> = ArrayList()
    private var ageValue: Int? = 0

    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private lateinit var itemAdapter: ItemsRecyclerviewAdapter
    private lateinit var counter: Counter
    private lateinit var playerController: YoutubePlayerController
    private lateinit var uiController: PlayerUiController
    private lateinit var viewModel: YoutubeDataApiViewModel
    private lateinit var disposable: CompositeDisposable

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_playlist)
        setSupportActionBar(toolbar)

        Stetho.initializeWithDefaults(applicationContext)

        disposable = CompositeDisposable()

        sharedPrefsHelper = SharedPrefsHelper(getSharedPreferences(
                SharedPrefsHelper.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE))

        viewModel = ViewModelProviders.of(this).get(YoutubeDataApiViewModel::class.java)

        val youtubePlayerView = findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        lifecycle.addObserver(youtubePlayerView)
        uiController = youtubePlayerView.getPlayerUiController()

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
                current_video_title_text.text = items[counter.current].snippet?.title
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

        counter = Counter()

        initializeYoutubePlayer(youtube_player_view)
        playerController = YoutubePlayerController(youtube_player_view)
    }

    override fun onResume() {
        super.onResume()
        loadPlaylist()
        val currentAge = sharedPrefsHelper.get(SharedPrefsHelper.AGE_RANGE_KEY, 0)
        if (currentAge != ageValue){
            currentAge?.let { setPlayerOptions(it) }
        }
    }

    private fun setPlayerOptions(age: Int) {
        resetPlayerOptions()
        when (age) {
            0 -> {
                uiController.showUi(false)
                ageValue = 0
            }
            1 -> {
                uiController.showMenuButton(false)
                uiController.showYouTubeButton(false)
                uiController.showFullscreenButton(false)
                ageValue = 1
            }
            2 -> {
                uiController.showMenuButton(false)
                ageValue = 2
            }
        }
    }

    private fun resetPlayerOptions() {
        uiController.showUi(true)
        uiController.showMenuButton(true)
        uiController.showYouTubeButton(true)
        uiController.showFullscreenButton(true)
        uiController.showMenuButton(true)
    }

    private fun loadPlaylist() {
        val currentPlaylistString = sharedPrefsHelper.get(SharedPrefsHelper.CURRENT_PLAYLIST_KEY, null)

        val currentPlaylistParts = currentPlaylistString?.split(", ")
        val currentPlaylistId = currentPlaylistParts?.get(0)
        val currentPlaylistName = currentPlaylistParts?.get(1)

        currentPlaylistName?.let {
            toolbar.title = it
        }

        if (currentPlaylistId != null) {
            viewModel.getPlaylistOverview(currentPlaylistId)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe {
                        items.clear()
                        items.addAll(it.items)
                        itemAdapter.notifyDataSetChanged()
                        no_playlist_text.visibility = View.GONE
                        video_title_container.visibility = View.VISIBLE
                        youtube_player_view.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                counter.maxValue = items.size - 1
                                youtube_player_view.visibility = View.VISIBLE
                                items[counter.current].contentDetails?.videoId?.let { id ->
                                    youTubePlayer.loadVideo(id, playerController.getTime())
                                }
                                items[counter.current].snippet?.title?.let { title ->
                                    current_video_title_text.text = title
                                }
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
            current_video_title_text.text = ""
            video_title_container.visibility = View.GONE
        }
    }

    private fun initializeYoutubePlayer(youtubePlayer: YouTubePlayerView) {
        youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                when (state) {
                    ENDED -> {
                        youTubePlayer.loadVideo(items[counter.increment()].contentDetails?.videoId.toString(), 0F)
                        current_video_title_text.text = items[counter.current].snippet?.title
                    }
                    else -> return
                }
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                playerController.saveTime(second)
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
        Log.i("asdas", newConfig.toString())
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
            youtube_player_view.enterFullScreen()
            uiController.showFullscreenButton(false)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
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

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}
