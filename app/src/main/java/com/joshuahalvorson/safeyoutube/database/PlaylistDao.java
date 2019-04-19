package com.joshuahalvorson.safeyoutube.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    List<Playlist> getAll();

    @Query("SELECT * FROM playlist WHERE playlist_id = (:playlistId)")
    Playlist getPlaylistId(String playlistId);

    @Insert
    void insertAll(Playlist... playlists);

    @Delete
    void delete(Playlist playlist);
}

