package com.joshuahalvorson.locklist.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_playlist")
data class LocalPlaylist(
        @ColumnInfo(name = "playlist_id") @PrimaryKey var playlistId: String,
        @ColumnInfo(name = "playlist_name") var playlistName: String?,
        @ColumnInfo(name = "playlist_video_count") var playlistVideoCount: Int?,
        @ColumnInfo(name = "playlist_thumbnail") var playlistThumbnail: String?,
        @ColumnInfo(name = "playlist_status") var privacyStatus: String?) {
    var id: Int = 0
}

