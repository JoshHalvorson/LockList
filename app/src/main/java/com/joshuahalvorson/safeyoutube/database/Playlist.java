package com.joshuahalvorson.safeyoutube.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "playlist_id")
    public String playlistId;

    @ColumnInfo(name = "playlist_name")
    public String playlistName;

    @ColumnInfo(name = "playlist_video_count")
    public int playlistVideoCount;

    public Playlist(String playlistId, String playlistName, int playlistVideoCount) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistVideoCount = playlistVideoCount;
    }
}
