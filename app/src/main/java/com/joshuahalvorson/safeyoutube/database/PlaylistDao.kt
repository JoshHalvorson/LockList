package com.joshuahalvorson.safeyoutube.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylists(): List<Playlist>

    @Query("SELECT * FROM playlist WHERE playlist_id = (:playlistId)")
    fun getPlaylistById(playlistId: String): Boolean

    @Query("DELETE FROM playlist WHERE playlist_id = (:playlistId)")
    fun deletePlaylistById(playlistId: String)

    @Insert
    fun insertAll(vararg playlists: Playlist)

    @Delete
    fun delete(playlist: Playlist)
}