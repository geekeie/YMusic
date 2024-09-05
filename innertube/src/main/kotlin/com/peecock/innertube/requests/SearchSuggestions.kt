package com.peecock.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import com.peecock.innertube.Innertube
import com.peecock.innertube.Innertube.getBestQuality
import com.peecock.innertube.models.GetSearchSuggestionsResponse
import com.peecock.innertube.models.MusicResponsiveListItemRenderer
import com.peecock.innertube.models.NavigationEndpoint
import com.peecock.innertube.models.SearchSuggestionsResponse
import com.peecock.innertube.models.bodies.SearchSuggestionsBody
import com.peecock.innertube.models.oddElements
import com.peecock.innertube.models.splitBySeparator
import com.peecock.innertube.utils.runCatchingNonCancellable

suspend fun Innertube.searchSuggestions(body: SearchSuggestionsBody) = runCatchingNonCancellable {
    val response = client.post(searchSuggestions) {
        setBody(body)
        mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
    }.body<SearchSuggestionsResponse>()

    response
        .contents
        ?.firstOrNull()
        ?.searchSuggestionsSectionRenderer
        ?.contents
        ?.mapNotNull { content ->
            content
                .searchSuggestionRenderer
                ?.navigationEndpoint
                ?.searchEndpoint
                ?.query
        }
}

suspend fun Innertube.searchSuggestionsWithItems(body: SearchSuggestionsBody) = runCatchingNonCancellable {
    val response = client.post(searchSuggestions) {
        setBody(body)
        //mask("contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query")
    }.body<GetSearchSuggestionsResponse>()

    val queries = response.contents?.getOrNull(0)?.searchSuggestionsSectionRenderer?.contents?.mapNotNull { content ->
        content.searchSuggestionRenderer?.suggestion?.runs?.joinToString(separator = "") { it.text.toString() }
    }.orEmpty()

    val recommendedItems =
        response.contents?.getOrNull(1)?.searchSuggestionsSectionRenderer?.contents?.mapNotNull {
            it.musicResponsiveListItemRenderer?.let { renderer ->
                SearchSuggestionPage.fromMusicResponsiveListItemRenderer(renderer)
            }
        }.orEmpty()

    println("mediaItem Innertube.searchSuggestionsWithItems queries $queries")
    println("mediaItem Innertube.searchSuggestionsWithItems recommendedItems $recommendedItems")

    Innertube.SearchSuggestions(
        queries = queries,
        recommendedSong = recommendedItems.filterIsInstance<Innertube.SongItem>().firstOrNull(),
        recommendedPlaylist = recommendedItems.filterIsInstance<Innertube.PlaylistItem>().firstOrNull(),
        recommendedAlbum = recommendedItems.filterIsInstance<Innertube.AlbumItem>().firstOrNull(),
        recommendedArtist = recommendedItems.filterIsInstance<Innertube.ArtistItem>().firstOrNull(),
        recommendedVideo = recommendedItems.filterIsInstance<Innertube.VideoItem>().firstOrNull(),
    )

}

object SearchSuggestionPage {
    fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): Innertube.Item? {
        return when {
            /*
            renderer.isVideo -> {
                VideoItem(
                    id = renderer.playlistItemData?.videoId ?: return null,
                    title = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: return null,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                    thumbnails = renderer.thumbnail.musicThumbnailRenderer.thumbnail,
                    explicit = renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null,
                    artists = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                        ?.getOrNull(1)?.oddElements()?.map {
                            Artist(
                                name = it.text,
                                id = it.navigationEndpoint?.browseEndpoint?.browseId
                            )
                        } ?: return null,
                    album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.let {
                        Album(
                            name = it.text,
                            id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return null
                        )
                    },
                    duration = null,
                    view = null
                )
            }
             */
            renderer.isSong -> {
                val explicitBadge = if (renderer.badges?.find {
                        it.musicInlineBadgeRenderer.icon.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null) "e:" else ""
                Innertube.SongItem(
                    info = Innertube.Info(
                        name = "${explicitBadge}${renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text}",
                        endpoint = renderer.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Watch
                    ),
                    authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                        ?.getOrNull(1)?.oddElements()?.map {
                            Innertube.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                            )
                        } ?: return null,
                    album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                        ?.let {
                            Innertube.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                            )
                        },
                    durationText = null,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null,
                    explicit = renderer.badges?.find {
                        it.musicInlineBadgeRenderer.icon.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null
                )
            }
            renderer.isArtist -> {
                Innertube.ArtistItem(
                    info = Innertube.Info(
                        name = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: return null,
                        endpoint = renderer.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                    ),
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null,
                    subscribersCountText = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text
                )
            }
            renderer.isAlbum -> {
                val secondaryLine = renderer.flexColumns.getOrNull(1)
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator() ?: return null
                Innertube.AlbumItem(
                    info = Innertube.Info(
                        name = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text ?: return null,
                        endpoint = renderer.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                    ),
                    authors = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                        ?.getOrNull(1)?.oddElements()?.map {
                            Innertube.Info(
                                name = it.text,
                                endpoint = it.navigationEndpoint?.endpoint as NavigationEndpoint.Endpoint.Browse
                            )
                        } ?: return null,
                    year = secondaryLine.lastOrNull()?.firstOrNull()?.text,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                        ?: return null
                )
            }
            else -> null
        }
    }
}
