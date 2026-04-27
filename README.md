# Projectivy Wallpaper Plugin : Overflight

Provides Apple TV's Aerials screen savers as video wallpapers for Projectivy Launcher.
Links source : https://docs.google.com/spreadsheets/d/1bboTohF06r-fafrImTExAPqM9m6h2m2lgJyAkQuYVJI

But there's more to it ! You can also customize the URL to the media source, which means you can use this plugin to provide Projectivy any video (or picture) list you want (trailers, live cams...).
Supported formats : JSON, M3U

Feel free to share your custom lists !

# Usage
- Install the plugin to an Android Tv device with Projectivy Launcher installed
- Go to Projectivy settings > Appearance > Wallpaper and choose "Overflight" in the dropdown list

# Screensaver
This is not a screensaver but a wallpaper provider dedicated to Projectivy Launcher. For a total "Aerials" experience, don't forget to install [this full-featured "Aerials" screensaver](https://github.com/theothernt/AerialViews)

# JSON format
```
[
    {
        "location": "Media source",
        "title": "Media title",
        "author": "Media author",
        "url_img": "http://url-to-image.jpg",
        "url_1080p": "http://url-to-1080p-video.mp4",
        "url_1080p_hdr": "http://url-to-1080p-hdr-video.mp4",
        "url_4k": "http://url-to-4k-video.mp4",
        "url_4k_hdr": "http://url-to-4k-hdr-video.mp4"
    },
]
```
None of the fields are mandatory but you will need to set at least one url_* so that a wallpaper is loaded.

# Note
This plugin is provided as is, and is not affiliated with Aerials or Apple Inc.
If you're a developer and want to create your own wallpaper plugin, check [the sample project](https://github.com/spocky/projectivy-plugin-wallpaper-provider) or use this repository as a base.
Found a bug or want to update the video links ? PR are welcome.
