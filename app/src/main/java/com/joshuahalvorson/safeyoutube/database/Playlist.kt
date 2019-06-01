package com.joshuahalvorson.safeyoutube.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
        @ColumnInfo(name = "playlist_id") var playlistId: String,
        @ColumnInfo(name = "playlist_name") var playlistName: String?,
        @ColumnInfo(name = "playlist_video_count") var playlistVideoCount: Int?,
        @ColumnInfo(name = "playlist_thumbnail") var playlistThumbnail: String?,
        @ColumnInfo(name = "playlist_status") var privacyStatus: String?) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


