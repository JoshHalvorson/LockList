package com.joshuahalvorson.safeyoutube.util

class YoutubePlayerController(private val youtubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
    fun playVideo(videoId: String) {
        youtubePlayer.loadVideo(videoId, 0F)
    }
}