package com.joshuahalvorson.locklist.model

open class Playlist(val playlistId: String,
                    val playlistName: String?,
                    val playlistVideoCount: Int?,
                    val playlistThumbnail: String?,
                    val privacyStatus: String?,
                    val isRemote: Boolean)