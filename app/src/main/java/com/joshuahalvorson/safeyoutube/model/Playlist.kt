package com.joshuahalvorson.safeyoutube.model

import com.joshuahalvorson.safeyoutube.database.RemotePlaylist

open class Playlist(val playlistId: String,
                    val playlistName: String?,
                    val playlistVideoCount: Int?,
                    val playlistThumbnail: String?,
                    val privacyStatus: String?,
                    val isRemote: Boolean)