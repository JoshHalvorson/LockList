package com.joshuahalvorson.safeyoutube.Kotlin.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Playlist(
        @ColumnInfo(name = "playlist_id") var playlistId: String,
        @ColumnInfo(name = "playlist_name") var playlistName: String,
        @ColumnInfo(name = "playlist_video_count") var playlistVideoCount: Int,
        @ColumnInfo(name = "playlist_thumbnail") var playlistThumbnail: String){
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0
}


