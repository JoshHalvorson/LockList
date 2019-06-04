package com.joshuahalvorson.safeyoutube.util

class YoutubePlayerController(
        private val youtubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer,
        private var time: Float = 0f) {

    fun playVideo(videoId: String, startTime: Float = 0f) {
        youtubePlayer.loadVideo(videoId, startTime)
    }

    fun saveTime(currentTime: Float){
        time = currentTime
    }

    fun getTime(): Float = time
}