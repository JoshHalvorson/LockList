package com.joshuahalvorson.safeyoutube.Kotlin.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylists(): List<Playlist>

    @Query("SELECT * FROM playlist WHERE playlist_id = (:playlistId)")
    fun getPlaylistById(playlistId: Int): Playlist

    @Insert
    fun insertAll(vararg playlists: Playlist)

    @Delete
    fun delete(playlist: Playlist)
}