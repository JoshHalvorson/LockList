package com.joshuahalvorson.safeyoutube.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocalPlaylistDao {
    @Query("SELECT * FROM local_playlist")
    fun getAllPlaylists(): Array<LocalPlaylist>

    @Query("SELECT * FROM local_playlist WHERE playlist_id = (:playlistId)")
    fun getPlaylistById(playlistId: String): Boolean

    @Query("DELETE FROM local_playlist WHERE playlist_id = (:playlistId)")
    fun deletePlaylistById(playlistId: String)

    @Insert
    fun insertAll(vararg localPlaylists: LocalPlaylist)

    @Query("DELETE FROM local_playlist")
    fun deleteAll()
}