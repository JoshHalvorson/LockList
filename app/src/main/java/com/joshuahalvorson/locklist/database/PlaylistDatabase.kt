package com.joshuahalvorson.locklist.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RemotePlaylist::class, LocalPlaylist::class], version = 1)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun remotePlaylistDao(): RemotePlaylistDao
    abstract fun localPlaylistDao(): LocalPlaylistDao
}