package com.joshuahalvorson.safeyoutube.Kotlin.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Playlist::class], version = 1)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}