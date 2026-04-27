package tv.projectivy.plugin.wallpaperprovider.overflight

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperDisplayMode
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType

class WallpaperProviderService: Service() {

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            allowTrailingComma = true
        }
    }

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
        NetClientManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> {
            // Don't care about the event : as the updateMode was declared as update_mode_time_elapsed
            // We will only receive this event, so no need to filter out other events
            return runBlocking {
                fetchWallpapers()
            }
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }

    }

    suspend fun fetchWallpapers(): List<Wallpaper> {
        val mediaSourceString = loadMediaSource(PreferencesManager.mediaSourceUrl)
        return parseMediaSource(mediaSourceString?.trim())
    }

    suspend fun loadMediaSource(url: String): String? {
        return withContext(Dispatchers.IO) {
            NetClientManager.request(url)
        }
    }

    suspend fun parseMediaSource(mediasourceString: String?): List<Wallpaper> {
        return when {
            mediasourceString.isNullOrBlank() -> emptyList()
            // If the media source string starts with a "[" character, we will consider it as a JSON string
            mediasourceString.startsWith("[") -> parseJson(mediasourceString)
            // If the media source string starts with a "#" character, we will consider it as an M3U string
            mediasourceString.startsWith("#") -> parseM3U(mediasourceString)
            else -> emptyList()
        }
    }

    suspend fun parseM3U(m3uString: String): List<Wallpaper> {
        return withContext(Dispatchers.Default) {
            try {
                val wallpapers = mutableListOf<Wallpaper>()
                var currentTitle: String? = null
                var currentSource: String? = null
                val groupTitleRegex = Regex("group-title=\"([^\"]+)\"")
                val videoRegex = Regex("\\.(mp4|mkv|webm|mov|m4v|3gp|ts|m3u8)(\\?.*)?$", RegexOption.IGNORE_CASE)

                m3uString.lineSequence().forEach { line ->
                    if (wallpapers.size >= MAX_WALLPAPERS_COUNT) return@withContext wallpapers

                    val trimmedLine = line.trim()
                    if (trimmedLine.isEmpty()) return@forEach

                    if (trimmedLine.startsWith("#EXTINF:")) {
                        // Extract title: it's everything after the last comma
                        val commaIndex = trimmedLine.lastIndexOf(',')
                        if (commaIndex != -1) {
                            currentTitle = trimmedLine.substring(commaIndex + 1).trim()
                        }
                        // Extract group-title as source if present
                        val groupTitleMatch = groupTitleRegex.find(trimmedLine)
                        currentSource = groupTitleMatch?.groupValues?.get(1)
                    } else if (!trimmedLine.startsWith("#")) {
                        // This should be the URI
                        val type = if (line.contains(videoRegex)) {
                            WallpaperType.VIDEO
                        } else {
                            WallpaperType.IMAGE
                        }

                        wallpapers.add(
                            Wallpaper(
                                uri = trimmedLine,
                                type = type,
                                displayMode = WallpaperDisplayMode.DEFAULT,
                                title = currentTitle,
                                source = currentSource,
                                author = null
                            )
                        )
                        // Reset for next entry
                        currentTitle = null
                        currentSource = null
                    }
                }
                wallpapers
            } catch (e: Exception) {
                Log.e("M3U Parsing", "Error parsing M3U: $e")
                emptyList()
            }
        }
    }

    suspend fun parseJson(jsonString: String): List<Wallpaper> {
        return withContext(Dispatchers.Default) {
            try {
                // Parse the JSON into a list of Media objects
                json.decodeFromString<List<Media?>?>(jsonString)
                    ?.asSequence()
                    ?.filterNotNull()
                    // Convert the list of Media objects into a list of Wallpaper objects
                    ?.map { media ->
                        Wallpaper(
                            media.preferredMediaUri,
                            media.mediaType,
                            WallpaperDisplayMode.DEFAULT,
                            media.title,
                            media.location,
                            media.author
                        )
                    }
                    // Filter out any wallpapers with an empty URI
                    ?.filter { it.uri.isNotBlank() }
                    ?.take(MAX_WALLPAPERS_COUNT)
                    ?.toList() ?: emptyList()
            } catch (e: Exception) {
                Log.e("JSON Parsing", "Error parsing JSON: $e")
                emptyList()
            }
        }
    }

    companion object {
        private const val MAX_WALLPAPERS_COUNT = 1000
    }
}