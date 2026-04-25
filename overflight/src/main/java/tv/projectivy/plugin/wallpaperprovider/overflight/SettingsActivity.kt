package tv.projectivy.plugin.wallpaperprovider.overflight

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperProviderContract

class SettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!packageManager.isApplicationInstalled(PROJECTIVY_PACKAGE_ID)) {
            Toast.makeText(this, R.string.projectivy_not_installed, Toast.LENGTH_LONG).show()
        }

        val fragment: GuidedStepSupportFragment = SettingsFragment()
        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content)
        }
    }

    // Call this method to inform Projectivy that it needs to request new wallpapers because the settings have changed
    // Note : if the settings have been opened from Projectivy Settings>Appearance>Wallpaper, this call is not needed.
    fun requestWallpaperUpdate() {
        val intent = Intent(WallpaperProviderContract.ACTION_WALLPAPER_PROVIDER_UPDATED).apply {
            `package` = PROJECTIVY_PACKAGE_ID

            putExtra(WallpaperProviderContract.EXTRA_PROVIDER_ID, R.string.plugin_uuid)
            putExtra(WallpaperProviderContract.EXTRA_UPDATE_REASON, WallpaperProviderContract.UpdateReason.PREFS_CHANGED)
        }

        sendBroadcast(intent)
    }

    fun PackageManager.isApplicationInstalled(packageName: String): Boolean {
        return try {
            getApplicationInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    companion object {
        private const val PROJECTIVY_PACKAGE_ID = "com.spocky.projengmenu"
    }
}