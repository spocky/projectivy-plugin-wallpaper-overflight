# Keep Wallpaper class and its Parcelable implementation for AIDL/IPC
-keep class tv.projectivy.plugin.wallpaperprovider.api.Wallpaper { *; }

# Keep Event class hierarchy (uses reflection for CREATOR field lookup)
-keep class tv.projectivy.plugin.wallpaperprovider.api.Event { *; }
-keep class tv.projectivy.plugin.wallpaperprovider.api.Event$* { *; }
