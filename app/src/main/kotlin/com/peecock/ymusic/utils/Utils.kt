package com.peecock.ymusic.utils

//import com.peecock.ymusic.BuildConfig
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import com.peecock.innertube.Innertube
import com.peecock.innertube.models.bodies.ContinuationBody
import com.peecock.innertube.requests.playlistPage
import com.peecock.innertube.utils.ProxyPreferences
import com.peecock.ymusic.Database
import com.peecock.ymusic.models.Album
import com.peecock.ymusic.models.Song
import com.peecock.ymusic.models.SongEntity
import com.peecock.ymusic.query
import com.peecock.ymusic.service.LOCAL_KEY_PREFIX
import com.peecock.ymusic.service.isLocal
import com.peecock.ymusic.ui.components.themed.NewVersionDialog
import com.peecock.ymusic.ui.items.EXPLICIT_PREFIX
import com.peecock.ymusic.ui.screens.home.MODIFIED_PREFIX
import com.peecock.ymusic.ui.screens.home.PINNED_PREFIX
import com.peecock.ymusic.ui.screens.home.PIPED_PREFIX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Calendar
import java.util.Date
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.time.Duration.Companion.minutes


fun cleanPrefix(text: String): String {
    var cleanText = text.substringAfter(PINNED_PREFIX)
    cleanText = cleanText.substringAfter(MONTHLY_PREFIX)
    cleanText = cleanText.substringAfter(PIPED_PREFIX)
    cleanText = cleanText.substringAfter(EXPLICIT_PREFIX)
    cleanText = cleanText.substringAfter(MODIFIED_PREFIX)
    return cleanText
}

fun getDateTimeAsFormattedString(dateAsLongInMs: Long): String? {
    try {
        return SimpleDateFormat("dd/MM/yyyy").format(Date(dateAsLongInMs))
    } catch (e: Exception) {
        return null // parsing exception
    }
}

fun getTimestampFromDate(date: String): Long {
    return try {
        SimpleDateFormat("dd-MM-yyyy").parse(date).time
    } catch (e: Exception) {
        return 0
    }
}

fun songToggleLike( song: Song ) {
    query {
        if (Database.songExist(song.asMediaItem.mediaId) == 0)
            Database.insert(song.asMediaItem, Song::toggleLike)
        //else {
            if (Database.songliked(song.asMediaItem.mediaId) == 0)
                Database.like(
                    song.asMediaItem.mediaId,
                    System.currentTimeMillis()
                )
            else Database.like(
                song.asMediaItem.mediaId,
                null
            )
        //}
    }
}

fun mediaItemToggleLike( mediaItem: MediaItem ) {
    query {
        if (Database.songExist(mediaItem.mediaId) == 0)
            Database.insert(mediaItem, Song::toggleLike)
        //else {
            if (Database.songliked(mediaItem.mediaId) == 0)
                Database.like(
                    mediaItem.mediaId,
                    System.currentTimeMillis()
                )
            else Database.like(
                mediaItem.mediaId,
                null
            )
        //}
    }
}

fun albumItemToggleBookmarked( albumItem: Innertube.AlbumItem ) {
    query {
        //if (Database.albumExist(albumItem.key) == 0)
        //    Database.insert(albumItem.asAlbum, Album::toggleLike)
        //else {
        if (Database.albumBookmarked(albumItem.key) == 0)
            Database.bookmarkAlbum(
                albumItem.key,
                System.currentTimeMillis()
            )
        else Database.bookmarkAlbum(
            albumItem.key,
            null
        )
        //}
    }
}

val Innertube.AlbumItem.asAlbum: Album
    get() = Album (
        id = key,
        title = info?.name,
        thumbnailUrl = thumbnail?.url,
        year = year,
        authorsText = authors?.joinToString("") { it.name ?: "" },
        //shareUrl =
    )

val Innertube.Podcast.EpisodeItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(videoId)
        .setUri(videoId)
        .setCustomCacheKey(videoId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(author.toString())
                .setAlbumTitle(title)
                .setArtworkUri(thumbnail.firstOrNull()?.url?.toUri())
                .setExtras(
                    bundleOf(
                        //"albumId" to album?.endpoint?.browseId,
                        "durationText" to durationString,
                        "artistNames" to author,
                        //"artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )

                .build()
        )
        .build()

val Innertube.SongItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString("") { it.name ?: "" })
                .setAlbumTitle(album?.name)
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "albumId" to album?.endpoint?.browseId,
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                    )
                )
                .build()
        )
        .build()

val Innertube.SongItem.asSong: Song
    @UnstableApi
    get() = Song (
        id = key,
        title = info?.name ?: "",
        artistsText = authors?.joinToString("") { it.name ?: "" },
        durationText = durationText,
        thumbnailUrl = thumbnail?.url
    )

val Innertube.VideoItem.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaId(key)
        .setUri(key)
        .setCustomCacheKey(key)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(info?.name)
                .setArtist(authors?.joinToString("") { it.name ?: "" })
                .setArtworkUri(thumbnail?.url?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to durationText,
                        "artistNames" to authors?.filter { it.endpoint != null }
                            ?.mapNotNull { it.name },
                        "artistIds" to authors?.mapNotNull { it.endpoint?.browseId },
                        "isOfficialMusicVideo" to isOfficialMusicVideo,
                        "isUserGeneratedContent" to isUserGeneratedContent
                        // "artistNames" to if (isOfficialMusicVideo) authors?.filter { it.endpoint != null }?.mapNotNull { it.name } else null,
                        // "artistIds" to if (isOfficialMusicVideo) authors?.mapNotNull { it.endpoint?.browseId } else null,
                    )
                )
                .build()
        )
        .build()


val Song.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistsText)
                .setArtworkUri(thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to durationText
                    )
                )
                .build()
        )
        .setMediaId(id)
        .setUri(
            if (isLocal) ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id.substringAfter(LOCAL_KEY_PREFIX).toLong()
            ) else id.toUri()
        )
        .setCustomCacheKey(id)
        .build()

val SongEntity.asMediaItem: MediaItem
    @UnstableApi
    get() = MediaItem.Builder()
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artistsText)
                .setAlbumTitle(albumTitle)
                .setArtworkUri(song.thumbnailUrl?.toUri())
                .setExtras(
                    bundleOf(
                        "durationText" to song.durationText
                    )
                )
                .build()
        )
        .setMediaId(song.id)
        .setUri(
            if (song.isLocal) ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.id.substringAfter(LOCAL_KEY_PREFIX).toLong()
            ) else song.id.toUri()
        )
        .setCustomCacheKey(song.id)
        .build()

val MediaItem.asSong: Song
    @UnstableApi
    get() = Song (
        id = mediaId,
        title = mediaMetadata.title.toString(),
        artistsText = mediaMetadata.artist.toString(),
        durationText = mediaMetadata.extras?.getString("durationText"),
        thumbnailUrl = mediaMetadata.artworkUri.toString()
    )

fun String.resize(
    width: Int? = null,
    height: Int? = null,
): String {
    if (width == null && height == null) return this
    "https://lh3\\.googleusercontent\\.com/.*=w(\\d+)-h(\\d+).*".toRegex().matchEntire(this)?.groupValues?.let { group ->
        val (W, H) = group.drop(1).map { it.toInt() }
        var w = width
        var h = height
        if (w != null && h == null) h = (w / W) * H
        if (w == null && h != null) w = (h / H) * W
        return "${split("=w")[0]}=w$w-h$h-p-l90-rj"
    }
    if (this matches "https://yt3\\.ggpht\\.com/.*=s(\\d+)".toRegex()) {
        return "$this-s${width ?: height}"
    }
    return this
}

fun String?.thumbnail(size: Int): String? {
    return when {
        this?.startsWith("https://lh3.googleusercontent.com") == true -> "$this-w$size-h$size"
        this?.startsWith("https://yt3.ggpht.com") == true -> "$this-w$size-h$size-s$size"
        else -> this
    }
}
fun String?.thumbnail(): String? {
    return this
}
fun Uri?.thumbnail(size: Int): Uri? {
    return toString().thumbnail(size)?.toUri()
}

fun formatAsDuration(millis: Long) = DateUtils.formatElapsedTime(millis / 1000).removePrefix("0")
fun durationToMillis(duration: String): Long {
    val parts = duration.split(":")
    val hours = parts[0].toLong()
    val minutes = parts[1].toLong()
    return hours * 3600000 + minutes * 60000
}

fun durationTextToMillis(duration: String): Long {
    return try {
        durationToMillis(duration)
    } catch (e: Exception) {
        0L
    }
}


fun formatAsTime(millis: Long): String {
    //if (millis == 0L) return ""
    val timePart1 = Duration.ofMillis(millis / 60).toMinutes().minutes
    val timePart2 = Duration.ofMillis(millis / 60).seconds % 60

    return "${timePart1} ${timePart2}s"
}

fun formatTimelineSongDurationToTime(millis: Long) =
    Duration.ofMillis(millis*1000).toMinutes().minutes.toString()

fun TimeToString(timeMs: Int): String {
    val mFormatBuilder = StringBuilder()
    val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
    val totalSeconds = timeMs / 1000
    //  videoDurationInSeconds = totalSeconds % 60;
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    mFormatBuilder.setLength(0)
    return if (hours > 0) {
        mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    } else {
        mFormatter.format("%02d:%02d", minutes, seconds).toString()
    }
}

@SuppressLint("SimpleDateFormat")
fun getCalculatedMonths( month: Int): String? {
    val c: Calendar = GregorianCalendar()
    c.add(Calendar.MONTH, -month)
    val sdfr = SimpleDateFormat("yyyy-MM")
    return sdfr.format(c.time).toString()
}

/*
// NEW RESULT PLAYLIST OR ALBUM PAGE WITH TEMPORARILY WORKING WORKAROUND
suspend fun Result<Innertube.PlaylistOrAlbumPage>.completed(): Result<Innertube.PlaylistOrAlbumPage>? {

    var playlistPage = getOrNull() ?: return null
    var songs = playlistPage.songsPage?.items.orEmpty().toMutableList()

    var continuationS = playlistPage.songsPage?.continuation

    //println("mediaItem 1 continuation $continuationS songs ${songs.size}")

    while (continuationS != null) {
        val newSongs = Innertube.playlistPage(
            body = ContinuationBody(continuation = continuationS)
        ).getOrNull()

        continuationS = newSongs?.continuation
        //println("mediaItem 1 loop continuation $continuationS songs ${songs.size}")
    }

    if (songs.size <= 100) {
        //println("mediaItem 2 continuation ${playlistPage.songsPage?.continuation} songs ${songs.size}")

        var continuation = playlistPage.songsPage?.continuation
        while (continuation != null) {
            val otherPlaylistPageResult =
                Innertube.playlistPageLong(ContinuationBody(continuation = continuation))

            //if (otherPlaylistPageResult.isFailure) break

            otherPlaylistPageResult.getOrNull()?.let { otherSongsPage ->
                /*
                playlistPage =
                    playlistPage.copy(songsPage = playlistPage.songsPage + otherSongsPage)
                 */
                otherSongsPage.items?.forEach {
                    songs.add(it)
                }
                continuation = otherSongsPage.continuation
            }

            if (songs.size > 5000) break
            //println("mediaItem 2 loop continuation ${continuation} songs ${songs.size}")
        }
    }
    /* **** */


    return Result.success(
        playlistPage.copy(
            songsPage = Innertube.ItemsPage(
                items = songs.distinct().toList(),
                continuation = null
            )
        )
    )

    //return Result.success(playlistPage)

}
*/
suspend fun Result<Innertube.PlaylistOrAlbumPage>.completed(
    maxDepth: Int =  Int.MAX_VALUE
) = runCatching {
    val page = getOrThrow()
    val songs = page.songsPage?.items.orEmpty().toMutableList()
    var continuation = page.songsPage?.continuation

    var depth = 0
    var continuationsList = arrayOf<String>()
    //continuationsList += continuation.orEmpty()

    while (continuation != null && depth++ < maxDepth) {
        val newSongs = Innertube
            .playlistPage(
                body = ContinuationBody(continuation = continuation)
            )
            ?.getOrNull()
            ?.takeUnless { it.items.isNullOrEmpty() } ?: break

        newSongs.items?.let { songs += it.filter { it !in songs } }
        continuation = newSongs.continuation

        //println("mediaItem loop $depth continuation founded ${continuationsList.contains(continuation)} $continuation")
        if (continuationsList.contains(continuation)) break

        continuationsList += continuation.orEmpty()
        //println("mediaItem loop continuationList size ${continuationsList.size}")
    }

    page.copy(songsPage = Innertube.ItemsPage(items = songs, continuation = null))
}.also { it.exceptionOrNull()?.printStackTrace() }

@Composable
fun CheckAvailableNewVersion(
    onDismiss: () -> Unit,
    updateAvailable: (Boolean) -> Unit
) {
    var updatedProductName = ""
    var updatedVersionName = ""
    var updatedVersionCode = 0
    val file = File(LocalContext.current.filesDir, "RiMusicUpdatedVersionCode.ver")
    if (file.exists()) {
        val dataText = file.readText().substring(0, file.readText().length - 1).split("-")
        updatedVersionCode =
            try {
                dataText.first().toInt()
            } catch (e: Exception) {
                0
            }
        updatedVersionName = if(dataText.size == 3) dataText[1] else ""
        updatedProductName =  if(dataText.size == 3) dataText[2] else ""
    }

    if (updatedVersionCode > getVersionCode()) {
        //if (updatedVersionCode > BuildConfig.VERSION_CODE)
        NewVersionDialog(
            updatedVersionName = updatedVersionName,
            updatedVersionCode = updatedVersionCode,
            updatedProductName = updatedProductName,
            onDismiss = onDismiss
        )
        updateAvailable(true)
    } else {
        updateAvailable(false)
        onDismiss()
    }
}

@Composable
fun isAvailableUpdate(): String {
    var newVersion = ""
    val file = File(LocalContext.current.filesDir, "RiMusicUpdatedVersion.ver")
    if (file.exists()) {
        newVersion = file.readText().substring(0, file.readText().length - 1)
        //Log.d("updatedVersion","${file.readText().length.toString()} ${file.readText().substring(0,file.readText().length-1)}")
        //Log.d("updatedVersion","${file.readText().length} ${newVersion.length}")
    } else newVersion = ""

    return if (newVersion == getVersionName() || newVersion == "") "" else newVersion
    //return if (newVersion == BuildConfig.VERSION_NAME || newVersion == "") "" else newVersion
}

@Composable
fun checkInternetConnection(): Boolean {
    val client = OkHttpClient()
    val request = OkHttpRequest(client)
    val coroutineScope = CoroutineScope(Dispatchers.Main)
    val url = "https://raw.githubusercontent.com/fast4x/RiMusic/master/updatedVersion/updatedVersionCode.ver"

    var check by remember {
        mutableStateOf("")
    }

    request.GET(url, object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            coroutineScope.launch {
                try {
                    responseData.let { check = it.toString() }
                    //Log.d("CheckInternet",check.substring(0,5))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onFailure(call: Call, e: java.io.IOException) {
            //Log.d("CheckInternet","Check failure")
        }
    })

    //Log.d("CheckInternetRet",check)
    return check.isNotEmpty()
}


fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkInfo = cm.activeNetwork
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null
    } else {
        return try {
            if (cm.activeNetworkInfo == null) {
                false
            } else {
                cm.activeNetworkInfo?.isConnected!!
            }
        } catch (e: Exception) {
            false
        }
    }

}

@Composable
fun isNetworkAvailableComposable(): Boolean {
    val context = LocalContext.current
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkInfo = cm.activeNetwork
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null
    } else {
        return try {
            if (cm.activeNetworkInfo == null) {
                false
            } else {
                cm.activeNetworkInfo?.isConnected!!
            }
        } catch (e: Exception) {
            false
        }
    }
}

fun getHttpClient() = HttpClient() {
    install(UserAgent) {
        agent = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0"
    }
    engine {
        ProxyPreferences.preference?.let{
            proxy = Proxy(it.proxyMode, InetSocketAddress(it.proxyHost, it.proxyPort))
        }

    }
}

@Composable
fun getVersionName(): String {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun getLongVersionCode(): Long {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.longVersionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return 0L
}


@Composable
fun getVersionCode(): Int {
    val context = LocalContext.current
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return 0
}


inline val isAtLeastAndroid6
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

inline val isAtLeastAndroid8
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

inline val isAtLeastAndroid10
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

inline val isAtLeastAndroid11
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

inline val isAtLeastAndroid12
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

inline val isAtLeastAndroid13
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

inline val isAtLeastAndroid14
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
