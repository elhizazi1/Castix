package me.elhizazi.Castix.xposed

/** Shared package list; safe to load without the Xposed framework. */
object HookTargets {
    val SUPPORTED_PACKAGES = setOf(
            "me.elhizazi.Castix",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.instagram.barcelona",
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.spotify.music",
            "com.soundcloud.android",
            "com.netflix.mediaclient"
        )
}
