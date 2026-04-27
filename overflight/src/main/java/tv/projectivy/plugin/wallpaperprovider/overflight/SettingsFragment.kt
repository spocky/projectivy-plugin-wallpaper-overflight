package tv.projectivy.plugin.wallpaperprovider.overflight

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance(
            getString(R.string.plugin_short_name),
            "v${BuildConfig.VERSION_NAME}\n\n${getString(R.string.plugin_description)}",
            getString(R.string.settings),
            AppCompatResources.getDrawable(requireActivity(), R.mipmap.ic_banner)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        PreferencesManager.init(requireContext())
        NetClientManager.init(requireContext())

        GuidedAction.Builder(context)
            .id(ACTION_ID_VIDEO_4K)
            .title(getString(R.string.setting_video_4k))
            .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
            .checked(PreferencesManager.video_4k)
            .build()
            .also { actions.add(it) }

        GuidedAction.Builder(context)
            .id(ACTION_ID_VIDEO_HDR)
            .title(getString(R.string.setting_video_hdr))
            .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
            .checked(PreferencesManager.video_hdr)
            .build()
            .also { actions.add(it) }

        GuidedAction.Builder(context)
            .id(ACTION_ID_FALLBACK)
            .title(getString(R.string.setting_fallback))
            .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
            .checked(PreferencesManager.fallback)
            .build()
            .also { actions.add(it) }

        val currentVideoSourceUrl = PreferencesManager.mediaSourceUrl
        GuidedAction.Builder(context)
            .id(ACTION_ID_MEDIA_SOURCE_URL)
            .title(getString(R.string.setting_media_source))
            .description(currentVideoSourceUrl)
            .editDescription(currentVideoSourceUrl)
            .descriptionEditable(true)
            .build()
            .also { actions.add(it) }

        GuidedAction.Builder(context)
            .id(ACTION_ID_CACHE_DURATION)
            .title(getString(R.string.setting_cache_duration))
            .description(PreferencesManager.cacheDurationHours.toString())
            .editDescription(PreferencesManager.cacheDurationHours.toString())
            .descriptionEditable(true)
            .build()
            .also { actions.add(it) }

        GuidedAction.Builder(context)
            .id(ACTION_ID_WALLPAPER_COUNT)
            .title(getString(R.string.setting_wallpaper_count))
            .description(getString(R.string.loading))
            .infoOnly(true)
            .build()
            .also { actions.add(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateWallpaperCount()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_ID_VIDEO_4K -> {
                PreferencesManager.video_4k = action.isChecked
                updateWallpaperCount()
            }
            ACTION_ID_VIDEO_HDR -> {
                PreferencesManager.video_hdr = action.isChecked
                updateWallpaperCount()
            }
            ACTION_ID_FALLBACK -> {
                PreferencesManager.fallback = action.isChecked
                updateWallpaperCount()
            }
            ACTION_ID_MEDIA_SOURCE_URL -> {
                val params: CharSequence? = action.editDescription
                PreferencesManager.mediaSourceUrl = (params?: PreferencesManager.DEFAULT_MEDIA_SOURCE_URL).toString()
                findActionById(ACTION_ID_MEDIA_SOURCE_URL)?.description = PreferencesManager.mediaSourceUrl
                notifyActionChanged(findActionPositionById(ACTION_ID_MEDIA_SOURCE_URL))
                updateWallpaperCount()
            }
            ACTION_ID_CACHE_DURATION -> {
                val params: CharSequence? = action.editDescription
                PreferencesManager.cacheDurationHours = (params?.toString()?.toIntOrNull() ?: PreferencesManager.DEFAULT_CACHE_DURATION_HOURS).coerceAtLeast(0)
                findActionById(ACTION_ID_CACHE_DURATION)?.description = PreferencesManager.cacheDurationHours.toString()
                notifyActionChanged(findActionPositionById(ACTION_ID_CACHE_DURATION))
            }
        }
    }

    private fun updateWallpaperCount() {
        lifecycleScope.launch {
            val countAction = findActionById(ACTION_ID_WALLPAPER_COUNT) ?: return@launch
            countAction.description = getString(R.string.loading)
            notifyActionChanged(findActionPositionById(ACTION_ID_WALLPAPER_COUNT))

            val wallpapers = WallpaperProviderService.fetchWallpapers()
            countAction.description = wallpapers.size.toString()
            notifyActionChanged(findActionPositionById(ACTION_ID_WALLPAPER_COUNT))
        }
    }

    companion object {
        private const val ACTION_ID_VIDEO_4K = 1L
        private const val ACTION_ID_VIDEO_HDR= 2L
        private const val ACTION_ID_FALLBACK= 3L
        private const val ACTION_ID_MEDIA_SOURCE_URL= 4L
        private const val ACTION_ID_CACHE_DURATION = 5L
        private const val ACTION_ID_WALLPAPER_COUNT = 6L
    }
}
