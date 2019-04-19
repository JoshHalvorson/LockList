package com.joshuahalvorson.safeyoutube.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "playlist_id")
    public String playlistId;

    public Playlist(String playlistId) {
        this.playlistId = playlistId;
    }
}
