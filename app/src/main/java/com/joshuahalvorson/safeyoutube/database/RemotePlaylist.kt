package com.joshuahalvorson.safeyoutube.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_playlist")
data class RemotePlaylist(
        @ColumnInfo(name = "playlist_id") @PrimaryKey var playlistId: String,
        @ColumnInfo(name = "playlist_name") var playlistName: String?,
        @ColumnInfo(name = "playlist_video_count") var playlistVideoCount: Int?,
        @ColumnInfo(name = "playlist_thumbnail") var playlistThumbnail: String?,
        @ColumnInfo(name = "playlist_status") var privacyStatus: String?) {
    var id: Int = 0
}


