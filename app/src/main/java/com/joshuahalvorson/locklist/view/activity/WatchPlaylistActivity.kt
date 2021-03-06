package com.joshuahalvorson.locklist.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.PlayerUiController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_watch_playlist.*

class WatchPlaylistActivity : AppCompatActivity() {

    private var items: ArrayList<Models.Item> = ArrayList()
    private var ageValue: Int? = 0

    private var currentPlaylistId: String = ""

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
        if (currentAge != ageValue) {
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
        progress_circle.visibility = View.VISIBLE
        val currentPlaylistString = sharedPrefsHelper.get(SharedPrefsHelper.CURRENT_PLAYLIST_KEY, null)
        val currentPlaylistParts = currentPlaylistString?.split(", ")
        currentPlaylistParts?.get(0)?.let { id ->
            currentPlaylistId = id
            no_playlist_text.visibility = View.GONE
            viewModel.getPlaylistOverview(id)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe {
                        items.clear()
                        items.addAll(it.items)
                        itemAdapter.notifyDataSetChanged()
                        youtube_player_view.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                progress_circle.visibility = View.GONE
                                counter.maxValue = items.size - 1
                                items[counter.current].contentDetails?.videoId?.let { id ->
                                    youTubePlayer.loadVideo(id, playerController.getTime())
                                    showViews()
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
        }
        val currentPlaylistName = currentPlaylistParts?.get(1)
        currentPlaylistName?.let {
            toolbar.title = it
        }

        if (currentPlaylistId == "") {
            items.clear()
            youtube_player_view.visibility = View.GONE
            no_playlist_text.visibility = View.VISIBLE
            current_video_title_text.text = ""
            video_title_container.visibility = View.GONE
            videos_list.visibility = View.GONE
            progress_circle.visibility = View.GONE
        }

        /*if (currentPlaylistId != null) {
            no_playlist_text.visibility = View.GONE
            viewModel.getPlaylistOverview(currentPlaylistId)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe {
                        items.clear()
                        items.addAll(it.items)
                        itemAdapter.notifyDataSetChanged()
                        youtube_player_view.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                                progress_circle.visibility = View.GONE
                                counter.maxValue = items.size - 1
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
            videos_list.visibility = View.GONE
            progress_circle.visibility = View.GONE
        }*/
    }

    private fun showViews() {
        progress_circle.visibility = View.GONE
        no_playlist_text.visibility = View.GONE
        videos_list.visibility = View.VISIBLE
        youtube_player_view.visibility = View.VISIBLE
        video_title_container.visibility = View.VISIBLE
        if (videos_list.visibility != View.VISIBLE && youtube_player_view.visibility != View.VISIBLE && video_title_container.visibility != View.VISIBLE) {
            YoYo.with(Techniques.FadeIn)
                    .repeat(0)
                    .duration(400)
                    .playOn(youtube_player_view)
            YoYo.with(Techniques.FadeIn)
                    .repeat(0)
                    .duration(400)
                    .playOn(video_title_container)
            YoYo.with(Techniques.FadeIn)
                    .repeat(0)
                    .duration(400)
                    .playOn(videos_list)
        }

    }

    private fun initializeYoutubePlayer(youtubePlayer: YouTubePlayerView) {
        youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                if (currentPlaylistId != "") {
                   showViews()
                }
            }

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
            youtube_player_view.enterFullScreen()
            uiController.showFullscreenButton(false)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
            toolbar.visibility = View.VISIBLE
            youtube_player_view.exitFullScreen()
            uiController.showFullscreenButton(true)
        }
    }

    private fun hideSystemUI() {
        toolbar.visibility = View.GONE

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                LoginFragment().show(supportFragmentManager, "login_fragment")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
