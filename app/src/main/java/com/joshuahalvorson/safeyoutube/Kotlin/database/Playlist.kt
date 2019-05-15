package com.joshuahalvorson.safeyoutube.Kotlin.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Playlist(
        @PrimaryKey(autoGenerate = true)
        private val id: Int,
        @ColumnInfo(name = "playlist_id")
        private val playlistId: String,
        @ColumnInfo(name = "playlist_name")
        private val playlistName: String,
        @ColumnInfo(name = "playlist_video_count")
        private val playlistVideoCount: Int,
        @ColumnInfo(name = "playlist_thumbnail")
        private val playlistThumbnail: String)