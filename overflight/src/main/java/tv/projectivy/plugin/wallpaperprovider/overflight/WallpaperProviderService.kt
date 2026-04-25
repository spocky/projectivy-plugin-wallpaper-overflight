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
        val jsonString = loadJson(PreferencesManager.mediaSourceUrl)
        return parseJson(jsonString)
    }

    suspend fun loadJson(url: String): String? {
        return withContext(Dispatchers.IO) {
            NetClientManager.request(url)
        }
    }

    suspend fun parseJson(jsonString: String?): List<Wallpaper> {
        return withContext(Dispatchers.Default) {
            try {
                if (jsonString.isNullOrBlank()) {
                    return@withContext emptyList()
                }

                // Parse the JSON into a list of Media objects
                json.decodeFromString<List<Media?>?>(jsonString)
                    ?.let { medialist ->
                        // Filter out any null objects (ex: due to trailing commas in JSON)
                        medialist.filterNotNull()
                            // Convert the list of Media objects into a list of Wallpaper objects
                            .map { media ->
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
                            .filter { it.uri.isNotBlank() }
                    } ?: emptyList()
            } catch (e: Exception) {
                Log.e("JSON Parsing", "Error parsing JSON: $e")
                emptyList()
            }
        }
    }
}