package com.joshuahalvorson.safeyoutube.Kotlin.model

object Models{
    data class ContentDetails(private var duration: String? = null,
                              private var dimension: String? = null,
                              private var definition: String? = null,
                              private var caption: String? = null,
                              private var licensedContent: Boolean? = null,
                              private var projection: String? = null)

    data class Default (private var url: String? = null,
                        private var width: Int? = null,
                        private var height: Int? = null)

    data class High(private var url: String? = null,
                    private var width: Int? = null,
                    private var height: Int? = null)

    data class Item(private var kind: String? = null,
                    private var etag: String? = null,
                    private var id: String? = null,
                    private var snippet: Snippet? = null,
                    private var contentDetails: ContentDetails? = null)

    data class Maxres(private var url: String? = null,
                      private var width: Int? = null,
                      private var height: Int? = null)

    data class Medium(private var url: String? = null,
                      private var width: Int? = null,
                      private var height: Int? = null)

    data class PageInfo(private var totalResults: Int? = null,
                        private var resultsPerPage: Int? = null)

    data class PlaylistResultOverview(private var kind: String? = null,
                                      private var etag: String? = null,
                                      private var nextPageToken: String? = null,
                                      private var pageInfo: PageInfo? = null,
                                      private var items: List<Item>? = null)

    data class ResourceId(private var kind: String? = null,
                          private var videoId: String? = null)

    data class Snippet(private var publishedAt: String? = null,
                       private var channelId: String? = null,
                       private var title: String? = null,
                       private var description: String? = null,
                       private var thumbnails: Thumbnails? = null,
                       private var channelTitle: String? = null,
                       private var playlistId: String? = null,
                       private var position: Int? = null,
                       private var resourceId: ResourceId? = null)

    data class Standard(private var url: String? = null,
                        private var width: Int? = null,
                        private var height: Int? = null)

    data class Thumbnails(private var _default: Default? = null,
                          private var medium: Medium? = null,
                          private var high: High? = null,
                          private var standard: Standard? = null,
                          private var maxres: Maxres? = null)

    data class VideoInfo(private var kind: String? = null,
                         private var etag: String? = null,
                         private var pageInfo: PageInfo? = null,
                         private var items: List<Item>? = null)
}