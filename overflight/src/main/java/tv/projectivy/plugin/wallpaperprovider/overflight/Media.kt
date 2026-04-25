package tv.projectivy.plugin.wallpaperprovider.overflight

import kotlinx.serialization.Serializable
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import tv.projectivy.plugin.wallpaperprovider.overflight.PreferencesManager.fallback
import tv.projectivy.plugin.wallpaperprovider.overflight.PreferencesManager.video_4k
import tv.projectivy.plugin.wallpaperprovider.overflight.PreferencesManager.video_hdr

// Default values keep parsing resilient when some JSON fields are missing.
@Serializable
data class Media(val location: String="",
                 val title: String="",
                 val url_img: String="",
                 val url_1080p: String="",
                 val url_1080p_hdr: String="",
                 val url_4k: String="",
                 val url_4k_hdr: String="",
                 val author: String="") {

    val mediaType: Int
        get() = when {
            preferredMediaUriWithFallback==url_img -> WallpaperType.IMAGE
            else -> WallpaperType.VIDEO
        }

    val preferredMediaUri: String
        get() = when {
            fallback -> preferredMediaUriWithFallback
            !video_4k && !video_hdr -> url_1080p
            !video_4k && video_hdr -> url_1080p_hdr
            video_4k && !video_hdr -> url_4k
            video_4k && video_hdr -> url_4k_hdr
            else -> url_1080p
        }

    private val preferredMediaUriWithFallback: String
        get() = listOf(
            url_4k_hdr.takeIf { video_4k && video_hdr },
            url_4k.takeIf { video_4k },
            url_1080p_hdr.takeIf { video_hdr },
            url_1080p,
            url_img
        ).firstOrNull { !it.isNullOrEmpty() } ?: url_1080p
}