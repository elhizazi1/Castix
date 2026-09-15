package me.elhizazi.Castix.privilege.domain.model

/**
 * Model representing an installed or popular multimedia application target
 * for which the Castix Floating Trigger and Background Bypasses can be enabled.
 */
data class TargetApp(
    val packageName: String,
    val appName: String,
    val isSelected: Boolean = false,
    val isMediaApp: Boolean = false
)

enum class FloatingButtonMode {
    ONLY_SELECTED_APPS,
    ALWAYS_VISIBLE
}
