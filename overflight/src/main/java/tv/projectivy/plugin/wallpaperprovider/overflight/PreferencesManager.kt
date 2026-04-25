package tv.projectivy.plugin.wallpaperprovider.overflight

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.collections.all
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.mapTo

object PreferencesManager {
    lateinit var preferences: SharedPreferences
    const val DEFAULT_MEDIA_SOURCE_URL = "https://raw.githubusercontent.com/spocky/projectivy-plugin-wallpaper-overflight/refs/heads/main/videos.json"
    const val DEFAULT_CACHE_DURATION_HOURS = 24

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            allowTrailingComma = true
        }
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) =
        when (value) {
            is String? -> preferences.edit { it.putString(key, value) }
            is Int -> preferences.edit { it.putInt(key, value) }
            is Boolean -> preferences.edit { it.putBoolean(key, value) }
            is Float -> preferences.edit { it.putFloat(key, value) }
            is Long -> preferences.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T =
        when (T::class) {
            String::class -> preferences.getString(key, defaultValue as String? ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    var video_4k: Boolean
        get() = PreferencesManager["video-4k", false]
        set(value) { PreferencesManager["video-4k"]=value }

    var video_hdr: Boolean
        get() = PreferencesManager["video-hdr", false]
        set(value) { PreferencesManager["video-hdr"]=value }

    var fallback: Boolean
        get() = PreferencesManager["fallback", true]
        set(value) { PreferencesManager["fallback"]=value }

    var mediaSourceUrl: String
        get() = PreferencesManager["video-source-url", DEFAULT_MEDIA_SOURCE_URL].takeIf { it.isNotEmpty() } ?: DEFAULT_MEDIA_SOURCE_URL
        set(value) { PreferencesManager["video-source-url"]=value }

    var cacheDurationHours: Int
        get() = PreferencesManager["cache-duration-hours", DEFAULT_CACHE_DURATION_HOURS].takeIf { it>= 0 } ?: DEFAULT_CACHE_DURATION_HOURS
        set(value) { PreferencesManager["cache-duration-hours"]=value }

    fun export(): String {
        return convertSharedPreferencesToJson(preferences)
    }

    fun import(prefs: String): Boolean {
        try {
            importPreferencesFromJson(preferences, prefs)
        } catch (e: Exception) {
            Log.e("import", "Error importing preferences", e)
            return false
        }
        return true
    }

    private fun convertSharedPreferencesToJson(sharedPreferences: SharedPreferences): String {
        val prefsJsonObject = buildJsonObject {
            sharedPreferences.all.forEach { (key, value) ->
                when (value) {
                    is Int -> put(key, JsonPrimitive(value))
                    is Long -> put(key, JsonPrimitive(value))
                    is Float -> put(key, JsonPrimitive(value))
                    is Boolean -> put(key, JsonPrimitive(value))
                    is String -> put(key, JsonPrimitive(value))
                    is Set<*> -> if (value.all { it is String }) {
                        put(key, JsonArray(value.map { JsonPrimitive(it as String) }))
                    } else {
                        throw IllegalArgumentException("Unsupported set type")
                    }
                    else -> throw IllegalArgumentException("Unsupported preference type ${value?.javaClass} for key $key")
                }
            }
        }
        return prefsJsonObject.toString()
    }

    private fun importPreferencesFromJson(sharedPreferences: SharedPreferences, jsonString: String) {
        val jsonElement = json.parseToJsonElement(jsonString)

        if (jsonElement is JsonObject) {
            val editor = sharedPreferences.edit()
            jsonElement.forEach { (key, value) ->
                when (value) {
                    is JsonPrimitive -> {
                        when {
                            value.isString -> editor.putString(key, value.content)
                            value.booleanOrNull != null -> editor.putBoolean(key, value.boolean)
                            value.intOrNull != null -> editor.putInt(key, value.int)
                            value.floatOrNull != null -> editor.putFloat(key, value.float)
                            value.longOrNull != null -> editor.putLong(key, value.long)
                        }
                    }

                    is JsonArray if value.all { it is JsonPrimitive && it.isString } -> {
                        val set = value.mapTo(mutableSetOf()) { it.jsonPrimitive.content }
                        editor.putStringSet(key, set)
                    }

                    else -> throw IllegalArgumentException("Unsupported JSON element type for key $key")
                }
            }
            editor.apply()
        } else {
            throw IllegalArgumentException("Expected JSON object for preferences import")
        }
    }
}