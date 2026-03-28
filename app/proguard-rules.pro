# ============================================================
# WorkManager — keep worker classes from R8 renaming/removal.
# Workers are resolved by class name at runtime; stripping them
# causes ClassNotFoundException that only appears in release builds.
# ============================================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.WorkerParameters

# Hilt-generated WorkManager factory
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keepclasseswithmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}

# ============================================================
# Room — keep entity and DAO classes
# ============================================================
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Entity class * { *; }

# ============================================================
# Kotlin serialization (Ktor / Supabase)
# ============================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ============================================================
# Supabase / Ktor — keep data transfer objects
# ============================================================
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# ============================================================
# Coroutines
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ============================================================
# Jetpack Compose
# ============================================================
-keep class androidx.compose.** { *; }

# ============================================================
# General: preserve line numbers for crash reports
# ============================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
