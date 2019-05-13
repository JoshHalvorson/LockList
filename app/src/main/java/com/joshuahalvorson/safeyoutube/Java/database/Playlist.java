package com.joshuahalvorson.safeyoutube.Java.database;

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

    @ColumnInfo(name = "playlist_thumbnail")
    public String playlistThumbnail;

    public Playlist(String playlistId, String playlistName, int playlistVideoCount, String playlistThumbnail) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistVideoCount = playlistVideoCount;
        this.playlistThumbnail = playlistThumbnail;
    }
}
