package com.joshuahalvorson.safeyoutube.kotlin.model

object Models {
    class ContentDetails {
        var duration: String? = null
        var dimension: String? = null
        var definition: String? = null
        var caption: String? = null
        var licensedContent: Boolean? = null
        var projection: String? = null
        var videoId: String? = null
    }

    class Default {
        var url: String? = null
        var width: Int? = null
        var height: Int? = null
    }

    class High {
        var url: String? = null
        var width: Int? = null
        var height: Int? = null
    }

    class Item {
        var kind: String? = null
        var etag: String? = null
        var id: String? = null
        var contentDetails: ContentDetails? = null
        var snippet: Snippet? = null
        var status: Status? = null
    }

    class Maxres {
        var url: String? = null
        var width: Int? = null
        var height: Int? = null
    }

    class Medium {
        var url: String? = null
        var width: Int? = null
        var height: Int? = null
    }

    class PageInfo {
        var totalResults: Int? = null
        var resultsPerPage: Int? = null
    }

    class PlaylistResultOverview {
        var kind: String? = null
        var etag: String? = null
        var nextPageToken: String? = null
        var pageInfo: PageInfo? = null
        var items: List<Item> = listOf()
    }

    class ResourceId {
        var kind: String? = null
        var videoId: String? = null
    }

    class Snippet {
        var publishedAt: String? = null
        var channelId: String? = null
        var title: String? = null
        var description: String? = null
        var thumbnails: Thumbnails? = null
        var channelTitle: String? = null
        var playlistId: String? = null
        var position: Int? = null
        var resourceId: ResourceId? = null
    }

    class Standard {
        var url: String? = null
        var width: Int? = null
        var height: Int? = null
    }

    class Thumbnails {
        var _default: Default? = null
        var medium: Medium? = null
        var high: High? = null
        var standard: Standard? = null
        var maxres: Maxres? = null
    }

    class VideoInfo {
        var kind: String? = null
        var etag: String? = null
        var pageInfo: PageInfo? = null
        var items: List<Item>? = null
    }

    class Status {
        var privacyStatus: String? = null
    }
}