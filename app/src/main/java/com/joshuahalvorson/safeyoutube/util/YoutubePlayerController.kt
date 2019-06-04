package com.joshuahalvorson.safeyoutube.util

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class YoutubePlayerController(
        private val playerView: YouTubePlayerView,
        private var time: Float = 0f) {

    private lateinit var youtubePlayer: YouTubePlayer

    init {
        initialize()
    }

    private fun initialize(){
        playerView.addYouTubePlayerListener(object: AbstractYouTubePlayerListener(){
            override fun onReady(youTubePlayer: YouTubePlayer) {
               youtubePlayer = youTubePlayer
            }

        })
    }

    fun playVideo(videoId: String, startTime: Float = 0f) {
        youtubePlayer.loadVideo(videoId, startTime)
    }

    fun saveTime(currentTime: Float){
        time = currentTime
    }

    fun getTime(): Float = time
}