# Castix R8 / ProGuard rules
# Keep the rules intentionally focused on runtime/reflection/IPC entry points.

# -----------------------------------------------------------------------------
# Xposed / LSPosed / LSPatch
# -----------------------------------------------------------------------------
-keep class de.robv.android.xposed.** { *; }
-keep interface de.robv.android.xposed.** { *; }
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage { *; }
-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit { *; }
-keep class me.elhizazi.Castix.xposed.CastixXposedHook { *; }

# Xposed entry-point metadata and callback signatures must remain discoverable.
-keepattributes *Annotation*,AnnotationDefault,InnerClasses,EnclosingMethod,Signature
-keep class de.robv.android.xposed.callbacks.** { *; }

# -----------------------------------------------------------------------------
# Shizuku / Dhizuku Binder IPC
# -----------------------------------------------------------------------------
-keep class rikka.** { *; }
-keep class dev.rikka.** { *; }
-keep class com.rosan.dhizuku.** { *; }
-keep interface rikka.** { *; }
-keep interface dev.rikka.** { *; }
-keep interface com.rosan.dhizuku.** { *; }

# Android Binder interfaces / generated Stub and Proxy implementations.
-keep class * extends android.os.IInterface { *; }
-keep interface * extends android.os.IInterface { *; }
-keep class * extends android.os.Binder { *; }
-keep class **$Stub { *; }
-keep class **$Stub$Proxy { *; }

# -----------------------------------------------------------------------------
# Root / libsu
# -----------------------------------------------------------------------------
# Castix currently does not declare libsu, but preserve this namespace if it is
# introduced by a future build variant/dependency.
-keep class com.topjohnwu.superuser.** { *; }

# -----------------------------------------------------------------------------
# Reflection / runtime lookup
# -----------------------------------------------------------------------------
# Preserve annotation/signature metadata used by Kotlin, Moshi, Room and
# framework reflection.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault,Signature,InnerClasses,EnclosingMethod

# Classes explicitly located by name at runtime in Castix.
-keep class org.lsposed.lspatch.Loader { *; }

# -----------------------------------------------------------------------------
# Moshi / Retrofit
# -----------------------------------------------------------------------------
# Moshi codegen and Retrofit are already R8-aware, but these rules protect
# reflective adapters/models if a model is not generated in a future variant.
-keepclasseswithmembers,includedescriptorclasses class * {
    @com.squareup.moshi.* <methods>;
}

# Retrofit service interfaces are instantiated reflectively by Retrofit.
-keep interface * {
    @retrofit2.http.* <methods>;
}
-keepattributes Exceptions

# -----------------------------------------------------------------------------
# Room
# -----------------------------------------------------------------------------
# Room normally supplies its own consumer rules. Preserve database/DAO contracts
# in case an application-side implementation is discovered reflectively.
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclasseswithmembers class * {
    @androidx.room.* <methods>;
}

# -----------------------------------------------------------------------------
# Application classes explicitly involved in system integration
# -----------------------------------------------------------------------------
# These are small, runtime-sensitive abstractions used by the privilege engines.
-keep class me.elhizazi.Castix.privilege.domain.engine.ShizukuEngine { *; }
-keep class me.elhizazi.Castix.privilege.domain.engine.DhizukuEngine { *; }
-keep class me.elhizazi.Castix.privilege.domain.engine.LSPosedEngine { *; }
-keep class me.elhizazi.Castix.privilege.domain.engine.LSPatchEngine { *; }
-keep class me.elhizazi.Castix.privilege.domain.engine.RootEngine { *; }

# Keep the Xposed init file/resource itself; it is discovered by the framework
# rather than by normal application code shrinking.

# Do not strip native JNI names if native integration is added later.
-keepclasseswithmembers,includedescriptorclasses class * {
    native <methods>;
}
