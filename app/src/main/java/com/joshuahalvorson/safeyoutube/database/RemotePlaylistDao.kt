package com.joshuahalvorson.safeyoutube.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RemotePlaylistDao {
    @Query("SELECT * FROM remote_playlist")
    fun getAllPlaylists(): Array<RemotePlaylist>

    @Query("SELECT * FROM remote_playlist WHERE playlist_id = (:playlistId)")
    fun getPlaylistById(playlistId: String): Boolean

    @Query("DELETE FROM remote_playlist WHERE playlist_id = (:playlistId)")
    fun deletePlaylistById(playlistId: String)

    @Insert
    fun insertAll(vararg remotePlaylists: RemotePlaylist)

    @Query("DELETE FROM remote_playlist")
    fun deleteAll()
}