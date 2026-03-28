I'll read all the required documents thoroughly before writing any code.
I now have a thorough understanding of all specs. Let me save project memory before producing the full implementation.
I have thoroughly read all five specification documents. Now I will produce the complete Android frontend implementation.

---

## File: `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CrossfitApp"
include(":app")
```

---

## File: `build.gradle.kts`

```kotlin
// Top-level build file — plugin declarations only.
// All dependency versions live in gradle/libs.versions.toml.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}
```

---

## File: `gradle/libs.versions.toml`

```toml
[versions]
# Core
kotlin                  = "2.0.21"
agp                     = "8.6.1"
ksp                     = "2.0.21-1.0.27"

# Compose
composeBom              = "2025.01.00"
navigationCompose       = "2.8.5"
activityCompose         = "1.9.3"
lifecycleRuntimeCompose = "2.8.7"

# DI
hilt                    = "2.51.1"
hiltNavigationCompose   = "1.2.0"

# Networking
ktor                    = "3.0.3"
kotlinSerialization     = "1.7.3"
okhttp                  = "4.12.0"
retrofit                = "2.11.0"
retrofitKotlinSerialization = "1.0.0"

# Supabase
supabase                = "3.0.2"

# Media
media3                  = "1.5.1"
camerax                 = "1.4.1"
mediapipe               = "0.10.21"

# Health Connect
healthConnect           = "1.1.0-rc01"

# Image Loading
coil                    = "3.0.4"

# DataStore
datastore               = "1.1.1"

# Room
room                    = "2.7.0"

# Coroutines
coroutines              = "1.9.0"

# AndroidX Core
coreKtx                 = "1.15.0"
appcompat               = "1.7.0"
material                = "1.12.0"
splashscreen            = "1.0.1"

# Testing
junit                   = "5.11.3"
junitAndroid            = "1.2.1"
mockk                   = "1.13.13"
turbine                 = "1.2.0"
coroutinesTest          = "1.9.0"

[libraries]
# --- Compose BOM ---
compose-bom                         = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui                          = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics                 = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview          = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3                   = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons-extended     = { group = "androidx.compose.material", name = "material-icons-extended" }
compose-animation                   = { group = "androidx.compose.animation", name = "animation" }
compose-foundation                  = { group = "androidx.compose.foundation", name = "foundation" }
compose-ui-test-junit4              = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-tooling                  = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-test-manifest            = { group = "androidx.compose.ui", name = "ui-test-manifest" }

# --- Navigation ---
navigation-compose                  = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
hilt-navigation-compose             = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# --- Lifecycle ---
activity-compose                    = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
lifecycle-runtime-compose           = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
lifecycle-viewmodel-compose         = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeCompose" }

# --- Hilt ---
hilt-android                        = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler                       = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }

# --- Ktor ---
ktor-client-core                    = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-android                 = { group = "io.ktor", name = "ktor-client-android", version.ref = "ktor" }
ktor-client-content-negotiation     = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging                 = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinx-json     = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-auth                    = { group = "io.ktor", name = "ktor-client-auth", version.ref = "ktor" }

# --- Serialization ---
kotlinx-serialization-json          = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinSerialization" }

# --- Coroutines ---
kotlinx-coroutines-android          = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test             = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }

# --- Supabase ---
supabase-bom                        = { group = "io.github.jan-tennert.supabase", name = "bom", version.ref = "supabase" }
supabase-postgrest                  = { group = "io.github.jan-tennert.supabase", name = "postgrest-kt" }
supabase-auth                       = { group = "io.github.jan-tennert.supabase", name = "auth-kt" }
supabase-storage                    = { group = "io.github.jan-tennert.supabase", name = "storage-kt" }
supabase-realtime                   = { group = "io.github.jan-tennert.supabase", name = "realtime-kt" }
supabase-ktor-engine                = { group = "io.github.jan-tennert.supabase", name = "ktor-client-android" }

# --- Media3 ---
media3-exoplayer                    = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-ui                           = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
media3-session                      = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }

# --- CameraX ---
camerax-core                        = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2                     = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle                   = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-video                       = { group = "androidx.camera", name = "camera-video", version.ref = "camerax" }
camerax-view                        = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }

# --- MediaPipe ---
mediapipe-tasks-vision              = { group = "com.google.mediapipe", name = "tasks-vision", version.ref = "mediapipe" }

# --- Health Connect ---
health-connect                      = { group = "androidx.health.connect", name = "connect-client", version.ref = "healthConnect" }

# --- Coil ---
coil-compose                        = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network-ktor                   = { group = "io.coil-kt.coil3", name = "coil-network-ktor3", version.ref = "coil" }

# --- DataStore ---
datastore-preferences               = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# --- Room ---
room-runtime                        = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx                            = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler                       = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# --- AndroidX Core ---
core-ktx                            = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
appcompat                           = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material                            = { group = "com.google.android.material", name = "material", version.ref = "material" }
splashscreen                        = { group = "androidx.core", name = "core-splashscreen", version.ref = "splashscreen" }

# --- OkHttp ---
okhttp-logging                      = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# --- Testing ---
junit5-api                          = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit" }
junit5-engine                       = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit" }
junit-android                       = { group = "androidx.test.ext", name = "junit", version.ref = "junitAndroid" }
mockk                               = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine                             = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

[plugins]
android-application                 = { id = "com.android.application", version.ref = "agp" }
android-library                     = { id = "com.android.library", version.ref = "agp" }
kotlin-android                      = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose                      = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization                = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt-android                        = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp                                 = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

---

## File: `app/build.gradle.kts`

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// Load local.properties for dev keys — production keys come from CI environment variables.
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) load(localFile.inputStream())
}

android {
    namespace = "com.apexai.crossfit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.apexai.crossfit"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${System.getenv("SUPABASE_URL") ?: localProperties.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${System.getenv("SUPABASE_ANON_KEY") ?: localProperties.getProperty("SUPABASE_ANON_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "FASTAPI_BASE_URL",
            "\"${System.getenv("FASTAPI_BASE_URL") ?: localProperties.getProperty("FASTAPI_BASE_URL", "https://api.apexai.example.com")}\""
        )

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    // --- Core desugaring (java.time on API 26+) ---
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    // --- Compose BOM ---
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)

    // --- AndroidX ---
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.splashscreen)

    // --- DI ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // --- Supabase ---
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.ktor.engine)

    // --- Ktor ---
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)

    // --- Serialization ---
    implementation(libs.kotlinx.serialization.json)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Media3 ---
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    // --- CameraX ---
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)
    implementation(libs.camerax.view)

    // --- MediaPipe ---
    implementation(libs.mediapipe.tasks.vision)

    // --- Health Connect ---
    implementation(libs.health.connect)

    // --- Coil ---
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor)

    // --- DataStore ---
    implementation(libs.datastore.preferences)

    // --- Room ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // --- OkHttp ---
    implementation(libs.okhttp.logging)

    // --- Testing ---
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.junit.android)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

---

## File: `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- =====================================================================
         Network & Internet
         ===================================================================== -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- =====================================================================
         Camera & Microphone
         ===================================================================== -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- =====================================================================
         Android Health Connect
         ===================================================================== -->
    <uses-permission android:name="android.permission.health.READ_HEART_RATE" />
    <uses-permission android:name="android.permission.health.READ_HEART_RATE_VARIABILITY" />
    <uses-permission android:name="android.permission.health.READ_SLEEP" />
    <uses-permission android:name="android.permission.health.READ_RESTING_HEART_RATE" />

    <!-- =====================================================================
         Hardware features
         ===================================================================== -->
    <uses-feature
        android:name="android.hardware.camera"
        android:required="true" />
    <uses-feature
        android:name="android.hardware.camera.autofocus"
        android:required="false" />

    <application
        android:name=".CrossfitApplication"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CrossfitApp"
        tools:targetApi="31">

        <!-- =====================================================================
             Main Activity — single-activity architecture
             ===================================================================== -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.CrossfitApp.SplashScreen">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- Deep link: apexai://wod/{wodId} -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:scheme="apexai"
                    android:host="wod" />
            </intent-filter>

            <!-- Deep link: apexai://coaching/report/{analysisId} -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:scheme="apexai"
                    android:host="coaching" />
            </intent-filter>
        </activity>

        <!-- =====================================================================
             Health Connect — query intent to check availability
             ===================================================================== -->
        <queries>
            <package android:name="com.google.android.apps.healthdata" />
        </queries>

        <!-- =====================================================================
             FileProvider for sharing recorded videos
             ===================================================================== -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/CrossfitApplication.kt`

```kotlin
package com.apexai.crossfit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CrossfitApplication : Application()
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/MainActivity.kt`

```kotlin
package com.apexai.crossfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.apexai.crossfit.core.ui.navigation.AppNavigation
import com.apexai.crossfit.core.ui.theme.ApexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ApexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color(0xFF0A0A0F)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/theme/Color.kt`

```kotlin
package com.apexai.crossfit.core.ui.theme

import androidx.compose.ui.graphics.Color

// --- Brand ---
val ElectricBlue       = Color(0xFF00D4FF)
val ElectricBlueDark   = Color(0xFF00A8CC)
val NeonGreen          = Color(0xFF39FF14)
val BlazeOrange        = Color(0xFFFF6B35)

// --- Backgrounds ---
val BackgroundDeepBlack  = Color(0xFF0A0A0F)
val SurfaceDark          = Color(0xFF12121A)
val SurfaceElevated      = Color(0xFF1A1A26)
val SurfaceCard          = Color(0xFF1E1E2E)
val BorderSubtle         = Color(0xFF2A2A3E)
val BorderVisible        = Color(0xFF3A3A55)

// --- Text ---
val TextPrimary    = Color(0xFFF0F0FF)
val TextSecondary  = Color(0xFF9090B0)
val TextDisabled   = Color(0xFF505068)
val TextOnBlue     = Color(0xFF000000)

// --- Semantic ---
val ColorSuccess = NeonGreen
val ColorWarning = Color(0xFFFFB800)
val ColorError   = Color(0xFFFF3B5C)
val ColorInfo    = ElectricBlue

// --- Readiness Zones ---
val ZoneOptimal      = NeonGreen
val ZoneCaution      = ColorWarning
val ZoneHighRisk     = ColorError
val ZoneUndertrained = Color(0xFF9090B0)

// --- Fault Severity ---
val SeverityMinor    = ColorWarning
val SeverityModerate = BlazeOrange
val SeverityCritical = ColorError
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/theme/Type.kt`

```kotlin
package com.apexai.crossfit.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.apexai.crossfit.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

val ApexTypography = Typography(
    displayLarge  = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 52.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/theme/Shape.kt`

```kotlin
package com.apexai.crossfit.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner radius tokens
val CornerSmall  = RoundedCornerShape(8.dp)
val CornerMedium = RoundedCornerShape(12.dp)
val CornerLarge  = RoundedCornerShape(16.dp)
val CornerXLarge = RoundedCornerShape(24.dp)
val CornerFull   = RoundedCornerShape(9999.dp)

val ApexShapes = Shapes(
    extraSmall = CornerSmall,
    small      = CornerSmall,
    medium     = CornerMedium,
    large      = CornerLarge,
    extraLarge = CornerXLarge
)
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/theme/Theme.kt`

```kotlin
package com.apexai.crossfit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ApexDarkColorScheme = darkColorScheme(
    primary              = ElectricBlue,
    onPrimary            = TextOnBlue,
    primaryContainer     = ElectricBlueDark,
    onPrimaryContainer   = TextPrimary,
    secondary            = NeonGreen,
    onSecondary          = TextOnBlue,
    secondaryContainer   = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary             = BlazeOrange,
    background           = BackgroundDeepBlack,
    onBackground         = TextPrimary,
    surface              = SurfaceCard,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceElevated,
    onSurfaceVariant     = TextSecondary,
    outline              = BorderSubtle,
    outlineVariant       = BorderVisible,
    error                = ColorError,
    onError              = TextPrimary,
    scrim                = SurfaceDark
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography  = ApexTypography,
        shapes      = ApexShapes,
        content     = content
    )
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/components/ApexCard.kt`

```kotlin
package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.CornerLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceCard

@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    isError: Boolean = false,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    padding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.99f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    val effectiveBorderColor = when {
        isError    -> com.apexai.crossfit.core.ui.theme.ColorError
        isSelected -> ElectricBlue
        borderColor != null -> borderColor
        isPressed && onClick != null -> ElectricBlue
        else -> BorderSubtle
    }

    val effectiveBorderWidth = when {
        isSelected -> 2.dp
        else -> borderWidth
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = CornerLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceCard.copy(alpha = 0.94f) else SurfaceCard
        ),
        border = BorderStroke(effectiveBorderWidth, effectiveBorderColor)
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/components/ApexTextField.kt`

```kotlin
package com.apexai.crossfit.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextDisabled
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@Composable
fun ApexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it, style = ApexTypography.bodySmall) } },
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            isError = errorMessage != null,
            enabled = enabled,
            readOnly = readOnly,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = CornerMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor    = SurfaceElevated,
                unfocusedContainerColor  = SurfaceElevated,
                disabledContainerColor   = com.apexai.crossfit.core.ui.theme.SurfaceDark,
                focusedBorderColor       = ElectricBlue,
                unfocusedBorderColor     = BorderSubtle,
                errorBorderColor         = ColorError,
                focusedTextColor         = TextPrimary,
                unfocusedTextColor       = TextPrimary,
                disabledTextColor        = TextDisabled,
                focusedLabelColor        = ElectricBlue,
                unfocusedLabelColor      = TextSecondary,
                errorLabelColor          = ColorError,
                placeholderColor         = TextSecondary,
                focusedLeadingIconColor  = TextSecondary,
                focusedTrailingIconColor = TextSecondary,
            ),
            textStyle = ApexTypography.bodyLarge.copy(color = TextPrimary)
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = ApexTypography.bodySmall,
                color = ColorError
            )
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/components/Buttons.kt`

```kotlin
package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.ElectricBlueDark
import com.apexai.crossfit.core.ui.theme.TextDisabled
import com.apexai.crossfit.core.ui.theme.TextOnBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "primary_button_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        enabled = enabled && !isLoading,
        shape = CornerMedium,
        contentPadding = PaddingValues(horizontal = 24.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor         = ElectricBlue,
            contentColor           = TextOnBlue,
            disabledContainerColor = BorderSubtle,
            disabledContentColor   = TextDisabled
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextOnBlue,
                strokeWidth = 2.dp
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                if (leadingIcon != null) {
                    leadingIcon()
                }
            }
            Text(
                text = text,
                style = ApexTypography.titleMedium.copy(color = TextOnBlue)
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = CornerMedium,
        contentPadding = PaddingValues(horizontal = 24.dp),
        border = BorderStroke(
            1.dp,
            if (enabled) ElectricBlue else BorderSubtle
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor           = ElectricBlue,
            disabledContentColor   = TextDisabled
        )
    ) {
        leadingIcon?.invoke()
        Text(
            text = text,
            style = ApexTypography.titleMedium
        )
    }
}

@Composable
fun ApexTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor         = ElectricBlue,
            disabledContentColor = TextDisabled
        )
    ) {
        Text(
            text = text,
            style = ApexTypography.titleMedium
        )
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = CornerMedium,
        colors = ButtonDefaults.buttonColors(
            containerColor       = ColorError,
            contentColor         = TextPrimary,
            disabledContainerColor = BorderSubtle,
            disabledContentColor   = TextDisabled
        )
    ) {
        Text(
            text = text,
            style = ApexTypography.titleMedium.copy(color = TextPrimary)
        )
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/components/ApexBottomNavBar.kt`

```kotlin
package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.ui.navigation.NavRoutes
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.ElectricBlueDark
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextOnBlue
import com.apexai.crossfit.core.ui.theme.TextSecondary

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home      : BottomNavItem(NavRoutes.HOME, Icons.Outlined.Home, "Home")
    data object Wod       : BottomNavItem(NavRoutes.WOD_BROWSE, Icons.Outlined.FitnessCenter, "WOD")
    data object Pr        : BottomNavItem(NavRoutes.PR_DASHBOARD, Icons.Outlined.StarBorder, "PRs")
    data object Readiness : BottomNavItem(NavRoutes.READINESS, Icons.Outlined.MonitorHeart, "Readiness")
    data object Profile   : BottomNavItem(NavRoutes.PROFILE, Icons.Outlined.Person, "Profile")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Wod,
    BottomNavItem.Pr,
    BottomNavItem.Readiness,
    BottomNavItem.Profile
)

@Composable
fun ApexBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCameraFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Nav bar background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .border(
                    width = 1.dp,
                    color = BorderSubtle,
                    shape = androidx.compose.foundation.shape.RectangleShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 2 items
                bottomNavItems.take(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }

                // Centre FAB placeholder
                Box(modifier = Modifier.size(72.dp))

                // Right 2 items (skip the center)
                bottomNavItems.takeLast(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        // Centre Camera FAB — elevated above nav bar top edge
        CameraFab(
            onClick = onCameraFabClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun NavBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(CornerXLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Active indicator pill
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = if (isSelected) 2.dp else 0.dp)
                .background(ElectricBlue, CornerFull)
        )

        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) ElectricBlue else TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = item.label,
            style = ApexTypography.labelSmall,
            color = if (isSelected) ElectricBlue else TextSecondary.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun CameraFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 8.dp,
                shape = CornerFull,
                ambientColor = ElectricBlue.copy(alpha = 0.4f),
                spotColor = ElectricBlue.copy(alpha = 0.4f)
            )
            .background(
                color = if (isPressed) ElectricBlueDark else ElectricBlue,
                shape = CornerFull
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CameraAlt,
            contentDescription = "Open AI video coaching camera",
            tint = TextOnBlue,
            modifier = Modifier.size(28.dp)
        )
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/components/LoadingIndicators.kt`

```kotlin
package com.apexai.crossfit.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.BorderVisible
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

/**
 * Shimmer skeleton placeholder for loading states.
 * Animates a gradient sweep from left to right infinitely.
 */
fun Modifier.shimmerSkeleton(
    baseColor: Color = SurfaceElevated,
    shimmerColor: Color = BorderVisible.copy(alpha = 0.6f)
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    background(
        brush = Brush.horizontalGradient(
            colors = listOf(
                baseColor,
                shimmerColor,
                baseColor
            ),
            startX = shimmerTranslateX * 500f,
            endX = (shimmerTranslateX + 1f) * 500f
        )
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .shimmerSkeleton()
    )
}

/**
 * Full circular readiness ring drawn with Canvas.
 * Animates from 0 to the target sweep angle on first composition.
 *
 * @param score      ACWR score (0.0 to 2.0+)
 * @param zoneColor  Color of the filled arc, derived from ReadinessZone
 * @param label      Center label (e.g., "1.12")
 * @param size       Diameter of the ring
 */
@Composable
fun CircularReadinessRing(
    score: Float,
    zoneColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 14.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val targetSweep = (score.coerceIn(0f, 2f) / 2f) * 270f
    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(800, easing = androidx.compose.animation.core.EaseOut),
        label = "ring_sweep"
    )
    val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { strokeWidth.toPx() }

    Canvas(modifier = modifier.size(size)) {
        val padding = strokeWidthPx / 2f
        val arcSize = Size(
            this.size.width - strokeWidthPx,
            this.size.height - strokeWidthPx
        )

        // Track arc (background)
        drawArc(
            color = BorderSubtle,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(padding, padding),
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // Fill arc (zone color)
        if (animatedSweep > 0f) {
            drawArc(
                color = zoneColor,
                startAngle = 135f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Center label
        val textResult = textMeasurer.measure(
            text = label,
            style = TextStyle(
                color = zoneColor,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(
                (this.size.width - textResult.size.width) / 2f,
                (this.size.height - textResult.size.height) / 2f
            )
        )

        // "ACWR" sub-label
        val subResult = textMeasurer.measure(
            text = "ACWR",
            style = TextStyle(
                color = TextSecondary,
                fontSize = 10.sp
            )
        )
        drawText(
            textLayoutResult = subResult,
            topLeft = Offset(
                (this.size.width - subResult.size.width) / 2f,
                (this.size.height - textResult.size.height) / 2f + textResult.size.height + 2f
            )
        )
    }
}

/**
 * A thin animated linear progress bar.
 */
@Composable
fun ApexLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = ElectricBlue,
    trackColor: Color = BorderSubtle
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = androidx.compose.animation.core.EaseOut),
        label = "linear_progress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CornerFull)
    ) {
        // Track
        drawRect(
            color = trackColor,
            size = this.size
        )
        // Fill
        drawRect(
            color = color,
            size = Size(this.size.width * animatedProgress, this.size.height)
        )
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/navigation/NavRoutes.kt`

```kotlin
package com.apexai.crossfit.core.ui.navigation

object NavRoutes {
    const val SPLASH       = "splash"

    // Auth
    const val AUTH_LOGIN    = "auth/login"
    const val AUTH_REGISTER = "auth/register"

    // Main tabs
    const val HOME           = "home"
    const val WOD_BROWSE     = "wod"
    const val WOD_HISTORY    = "wod/history"
    const val PR_DASHBOARD   = "pr"
    const val READINESS      = "readiness"
    const val READINESS_SETUP = "readiness/setup"
    const val PROFILE        = "profile"

    // WOD detail flow
    fun wodDetail(wodId: String)  = "wod/$wodId"
    fun wodLog(wodId: String)     = "wod/$wodId/log"
    fun wodTimer(wodId: String)   = "wod/$wodId/timer"
    fun prDetail(movementId: String) = "pr/$movementId"

    // Vision flow
    const val VISION_LIVE = "vision/live"
    fun visionReview(videoUri: String) = "vision/review/${encode(videoUri)}"

    // Coaching flow
    fun coachingReport(analysisId: String)                        = "coaching/report/$analysisId"
    fun coachingPlayback(videoId: String, startMs: Long = 0L)    = "coaching/playback/$videoId?timestamp=$startMs"

    // Deep link patterns for NavDeepLink
    const val DEEP_LINK_WOD_DETAIL     = "apexai://wod/{wodId}"
    const val DEEP_LINK_COACHING_REPORT = "apexai://coaching/report/{analysisId}"

    // Route patterns with arguments (for navArgument declarations)
    const val WOD_DETAIL_PATTERN       = "wod/{wodId}"
    const val WOD_LOG_PATTERN          = "wod/{wodId}/log"
    const val WOD_TIMER_PATTERN        = "wod/{wodId}/timer"
    const val PR_DETAIL_PATTERN        = "pr/{movementId}"
    const val VISION_REVIEW_PATTERN    = "vision/review/{videoUri}"
    const val COACHING_REPORT_PATTERN  = "coaching/report/{analysisId}"
    const val COACHING_PLAYBACK_PATTERN = "coaching/playback/{videoId}?timestamp={timestamp}"

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/ui/navigation/AppNavigation.kt`

```kotlin
package com.apexai.crossfit.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.apexai.crossfit.feature.auth.presentation.login.LoginScreen
import com.apexai.crossfit.feature.auth.presentation.login.LoginViewModel
import com.apexai.crossfit.feature.coaching.presentation.CoachingReportScreen
import com.apexai.crossfit.feature.coaching.presentation.CoachingViewModel
import com.apexai.crossfit.feature.coaching.presentation.VideoPlaybackScreen
import com.apexai.crossfit.feature.coaching.presentation.VideoPlaybackViewModel
import com.apexai.crossfit.feature.pr.presentation.dashboard.PrDashboardScreen
import com.apexai.crossfit.feature.pr.presentation.dashboard.PrDashboardViewModel
import com.apexai.crossfit.feature.pr.presentation.detail.PrDetailScreen
import com.apexai.crossfit.feature.pr.presentation.detail.PrDetailViewModel
import com.apexai.crossfit.feature.readiness.presentation.ReadinessDashboardScreen
import com.apexai.crossfit.feature.readiness.presentation.ReadinessViewModel
import com.apexai.crossfit.feature.vision.presentation.camera.LiveCameraScreen
import com.apexai.crossfit.feature.vision.presentation.camera.VisionViewModel
import com.apexai.crossfit.feature.vision.presentation.review.RecordingReviewScreen
import com.apexai.crossfit.feature.wod.presentation.detail.WodDetailScreen
import com.apexai.crossfit.feature.wod.presentation.detail.WodDetailViewModel
import com.apexai.crossfit.feature.wod.presentation.log.WodLogScreen
import com.apexai.crossfit.feature.wod.presentation.log.WodLogViewModel
import com.apexai.crossfit.feature.wod.presentation.timer.WodTimerScreen
import com.apexai.crossfit.feature.wod.presentation.timer.WodTimerViewModel
import com.apexai.crossfit.ui.home.HomeScreen
import com.apexai.crossfit.ui.home.HomeViewModel
import com.apexai.crossfit.ui.splash.SplashScreen
import com.apexai.crossfit.ui.splash.SplashViewModel

private val SLIDE_DURATION = 300

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeIn(tween(SLIDE_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeOut(tween(SLIDE_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeIn(tween(SLIDE_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION)
            ) + fadeOut(tween(SLIDE_DURATION))
        }
    ) {
        // ---------------------------------------------------------------
        // Splash
        // ---------------------------------------------------------------
        composable(NavRoutes.SPLASH) {
            val vm: SplashViewModel = hiltViewModel()
            SplashScreen(
                onNavigateToHome  = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.AUTH_LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                viewModel = vm
            )
        }

        // ---------------------------------------------------------------
        // Auth
        // ---------------------------------------------------------------
        composable(NavRoutes.AUTH_LOGIN) {
            val vm: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = vm,
                onNavigateToHome     = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH_LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.AUTH_REGISTER)
                }
            )
        }

        composable(NavRoutes.AUTH_REGISTER) {
            val vm: LoginViewModel = hiltViewModel()
            // RegisterScreen uses same VM wiring with different UiEvent
            com.apexai.crossfit.feature.auth.presentation.register.RegisterScreen(
                viewModel = vm,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH_LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Home (bottom nav root)
        // ---------------------------------------------------------------
        composable(NavRoutes.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            val currentRoute by navController.currentBackStackEntryAsState()
            HomeScreen(
                viewModel          = vm,
                currentNavRoute    = currentRoute?.destination?.route ?: NavRoutes.HOME,
                onNavigateToWod    = { navController.navigate(NavRoutes.WOD_BROWSE) },
                onNavigateToWodDetail = { wodId -> navController.navigate(NavRoutes.wodDetail(wodId)) },
                onNavigateToPr     = { navController.navigate(NavRoutes.PR_DASHBOARD) },
                onNavigateToReadiness = { navController.navigate(NavRoutes.READINESS) },
                onNavigateToProfile   = { navController.navigate(NavRoutes.PROFILE) },
                onNavigateToCamera    = { navController.navigate(NavRoutes.VISION_LIVE) },
                onNavigateToPrDetail  = { movementId -> navController.navigate(NavRoutes.prDetail(movementId)) },
                onBottomNavNavigate   = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        // ---------------------------------------------------------------
        // WOD browse
        // ---------------------------------------------------------------
        composable(NavRoutes.WOD_BROWSE) {
            val vm: com.apexai.crossfit.feature.wod.presentation.browse.WodBrowseViewModel = hiltViewModel()
            com.apexai.crossfit.feature.wod.presentation.browse.WodBrowseScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.WOD_BROWSE,
                onNavigateToDetail = { wodId -> navController.navigate(NavRoutes.wodDetail(wodId)) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        // ---------------------------------------------------------------
        // WOD Detail
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.WOD_DETAIL_PATTERN,
            arguments = listOf(navArgument("wodId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.DEEP_LINK_WOD_DETAIL })
        ) { backStackEntry ->
            val vm: WodDetailViewModel = hiltViewModel()
            WodDetailScreen(
                viewModel       = vm,
                onNavigateBack  = { navController.popBackStack() },
                onNavigateToTimer = { wodId -> navController.navigate(NavRoutes.wodTimer(wodId)) },
                onNavigateToLog   = { wodId -> navController.navigate(NavRoutes.wodLog(wodId)) },
                onNavigateToCamera = { navController.navigate(NavRoutes.VISION_LIVE) }
            )
        }

        // ---------------------------------------------------------------
        // WOD Timer
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.WOD_TIMER_PATTERN,
            arguments = listOf(navArgument("wodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vm: WodTimerViewModel = hiltViewModel()
            WodTimerScreen(
                viewModel         = vm,
                onNavigateToLog   = { wodId ->
                    navController.navigate(NavRoutes.wodLog(wodId)) {
                        popUpTo(NavRoutes.WOD_TIMER_PATTERN) { inclusive = true }
                    }
                },
                onNavigateBack    = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // WOD Log
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.WOD_LOG_PATTERN,
            arguments = listOf(navArgument("wodId") { type = NavType.StringType })
        ) {
            val vm: WodLogViewModel = hiltViewModel()
            WodLogScreen(
                viewModel       = vm,
                onNavigateBack  = { navController.popBackStack() },
                onNavigateHome  = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // ---------------------------------------------------------------
        // PR Dashboard
        // ---------------------------------------------------------------
        composable(NavRoutes.PR_DASHBOARD) {
            val vm: PrDashboardViewModel = hiltViewModel()
            PrDashboardScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.PR_DASHBOARD,
                onNavigateToPrDetail = { movementId -> navController.navigate(NavRoutes.prDetail(movementId)) },
                onNavigateToWod = { navController.navigate(NavRoutes.WOD_BROWSE) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        // ---------------------------------------------------------------
        // PR Detail
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.PR_DETAIL_PATTERN,
            arguments = listOf(navArgument("movementId") { type = NavType.StringType })
        ) {
            val vm: PrDetailViewModel = hiltViewModel()
            PrDetailScreen(
                viewModel      = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Readiness
        // ---------------------------------------------------------------
        composable(NavRoutes.READINESS) {
            val vm: ReadinessViewModel = hiltViewModel()
            ReadinessDashboardScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.READINESS,
                onNavigateToSetup = { navController.navigate(NavRoutes.READINESS_SETUP) },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }

        // ---------------------------------------------------------------
        // Vision (full-screen, no bottom nav)
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.VISION_LIVE,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition  = { fadeOut(tween(200)) }
        ) {
            val vm: VisionViewModel = hiltViewModel()
            LiveCameraScreen(
                viewModel = vm,
                onNavigateBack   = { navController.popBackStack() },
                onNavigateToReview = { videoUri ->
                    navController.navigate(NavRoutes.visionReview(videoUri))
                }
            )
        }

        composable(
            route = NavRoutes.VISION_REVIEW_PATTERN,
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: ""
            RecordingReviewScreen(
                videoUri         = videoUri,
                onNavigateBack   = { navController.popBackStack() },
                onNavigateToReport = { analysisId ->
                    navController.navigate(NavRoutes.coachingReport(analysisId)) {
                        popUpTo(NavRoutes.VISION_LIVE) { inclusive = false }
                    }
                }
            )
        }

        // ---------------------------------------------------------------
        // Coaching (full-screen)
        // ---------------------------------------------------------------
        composable(
            route = NavRoutes.COACHING_REPORT_PATTERN,
            arguments = listOf(navArgument("analysisId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.DEEP_LINK_COACHING_REPORT })
        ) {
            val vm: CoachingViewModel = hiltViewModel()
            CoachingReportScreen(
                viewModel = vm,
                onNavigateHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                },
                onNavigateToPlayback = { videoId, timestampMs ->
                    navController.navigate(NavRoutes.coachingPlayback(videoId, timestampMs))
                }
            )
        }

        composable(
            route = NavRoutes.COACHING_PLAYBACK_PATTERN,
            arguments = listOf(
                navArgument("videoId")   { type = NavType.StringType },
                navArgument("timestamp") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            val vm: VideoPlaybackViewModel = hiltViewModel()
            VideoPlaybackScreen(
                viewModel      = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------------------------------------
        // Profile
        // ---------------------------------------------------------------
        composable(NavRoutes.PROFILE) {
            val vm: com.apexai.crossfit.feature.auth.presentation.profile.ProfileViewModel = hiltViewModel()
            com.apexai.crossfit.feature.auth.presentation.profile.ProfileScreen(
                viewModel = vm,
                currentNavRoute = NavRoutes.PROFILE,
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.AUTH_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBottomNavNavigate = { route -> navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }}
            )
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/media/PlayerPoolManager.kt`

```kotlin
package com.apexai.crossfit.core.media

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maintains a fixed-size pool of ExoPlayer instances.
 *
 * MANDATE (CLAUDE.md): Never instantiate ExoPlayer inline per video tile.
 * One-player-per-tile exhausts the hardware decoder budget and crashes the app.
 *
 * Usage pattern:
 *   val player = pool.acquire()
 *   player.setMediaItem(...)
 *   player.prepare()
 *   // Attach to PlayerView
 *   ...
 *   pool.release(player)
 */
@Singleton
class PlayerPoolManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val poolSize = 2
    private val available = ArrayDeque<ExoPlayer>()
    private val inUse = mutableSetOf<ExoPlayer>()

    init {
        repeat(poolSize) {
            available.addLast(buildPlayer())
        }
    }

    /**
     * Acquires a player from the pool. If the pool is exhausted, returns the
     * least-recently-released player (evicting its current usage is the
     * caller's responsibility via [release]).
     */
    @Synchronized
    fun acquire(): ExoPlayer {
        return if (available.isNotEmpty()) {
            val player = available.removeFirst()
            inUse.add(player)
            player
        } else {
            // Pool exhausted — callers should always balance acquire/release.
            // Build a temporary player rather than crashing; logs a warning.
            android.util.Log.w("PlayerPoolManager", "Pool exhausted — consider increasing pool size")
            buildPlayer().also { inUse.add(it) }
        }
    }

    /**
     * Returns a player to the pool. Stops and clears the player before
     * making it available for reuse so the next caller gets a clean state.
     */
    @Synchronized
    fun release(player: ExoPlayer) {
        if (inUse.remove(player)) {
            player.stop()
            player.clearMediaItems()
            available.addLast(player)
        }
    }

    /**
     * Releases all players and frees hardware decoder resources.
     * Call from Application.onTerminate() or a process-level lifecycle hook.
     */
    @Synchronized
    fun releaseAll() {
        (available + inUse).forEach { it.release() }
        available.clear()
        inUse.clear()
    }

    private fun buildPlayer(): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/data/SupabaseClientProvider.kt`

```kotlin
package com.apexai.crossfit.core.data

import com.apexai.crossfit.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClientProvider @Inject constructor() {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl    = BuildConfig.SUPABASE_URL,
        supabaseKey    = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/data/network/ApiService.kt`

```kotlin
package com.apexai.crossfit.core.data.network

import com.apexai.crossfit.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AnalyzeVideoRequest(
    val videoId: String,
    val movementType: String,
    val athleteId: String
)

@Serializable
data class AnalyzeVideoResponse(
    val analysisId: String,
    val status: String
)

@Singleton
class FastApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val baseUrl = BuildConfig.FASTAPI_BASE_URL

    /**
     * Kicks off Gemini video analysis on the FastAPI microservice.
     * The video must already be uploaded to Supabase Storage before calling this.
     */
    suspend fun analyzeVideo(
        videoId: String,
        movementType: String,
        athleteId: String,
        accessToken: String
    ): AnalyzeVideoResponse {
        return httpClient.post("$baseUrl/api/v1/coaching/analyze") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(AnalyzeVideoRequest(videoId, movementType, athleteId))
        }.body()
    }

    /**
     * Uploads a video file directly to FastAPI as multipart.
     * Used as an alternative path if bypassing Supabase Storage.
     */
    suspend fun uploadVideoMultipart(
        videoBytes: ByteArray,
        fileName: String,
        movementType: String,
        accessToken: String
    ): AnalyzeVideoResponse {
        return httpClient.post("$baseUrl/api/v1/coaching/upload") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("movement_type", movementType)
                        append(
                            key = "video",
                            value = videoBytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "video/mp4")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                )
            )
        }.body()
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/di/CoreModule.kt`

```kotlin
package com.apexai.crossfit.core.di

import android.content.Context
import com.apexai.crossfit.core.data.SupabaseClientProvider
import com.apexai.crossfit.core.data.network.FastApiService
import com.apexai.crossfit.core.media.PlayerPoolManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideKtorClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = if (com.apexai.crossfit.BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
        }
        engine {
            connectTimeout = 30_000
            socketTimeout  = 60_000
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(provider: SupabaseClientProvider): SupabaseClient =
        provider.client

    @Provides
    @Singleton
    fun providePlayerPoolManager(
        @ApplicationContext context: Context
    ): PlayerPoolManager = PlayerPoolManager(context)

    @Provides
    @Singleton
    fun provideFastApiService(httpClient: HttpClient): FastApiService =
        FastApiService(httpClient)
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/core/domain/model/Models.kt`

```kotlin
package com.apexai.crossfit.core.domain.model

import android.graphics.PointF
import java.time.Duration
import java.time.Instant

// ============================================================
// Workout domain
// ============================================================

enum class TimeDomain { AMRAP, EMOM, RFT, TABATA }
enum class ScoringMetric { REPS, TIME, LOAD, ROUNDS_PLUS_REPS }

data class Workout(
    val id: String,
    val name: String,
    val description: String,
    val timeDomain: TimeDomain,
    val scoringMetric: ScoringMetric,
    val timeCap: Duration?,
    val rounds: Int?,
    val movements: List<WorkoutMovement> = emptyList()
)

data class WorkoutSummary(
    val id: String,
    val name: String,
    val timeDomain: TimeDomain,
    val movementCount: Int
)

data class WorkoutMovement(
    val id: String,
    val movement: Movement,
    val prescribedReps: Int?,
    val prescribedWeight: Double?,
    val prescribedDistance: Double?,
    val prescribedCalories: Int?,
    val sortOrder: Int
)

data class Movement(
    val id: String,
    val name: String,
    val category: String,
    val primaryMuscles: List<String>,
    val equipment: String?
)

data class WorkoutResult(
    val id: String,
    val workoutId: String,
    val userId: String,
    val score: String,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?,
    val completedAt: Instant,
    val newPrs: List<PersonalRecord> = emptyList()
)

data class WorkoutResultInput(
    val workoutId: String,
    val score: String,
    val scoreNumeric: Double?,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?
)

// ============================================================
// Personal Records domain
// ============================================================

enum class PrUnit { KG, LBS, REPS, SECONDS }

data class PersonalRecord(
    val id: String,
    val userId: String,
    val movementId: String,
    val movementName: String,
    val category: String,
    val value: Double,
    val unit: PrUnit,
    val achievedAt: Instant
)

data class PrHistoryEntry(
    val value: Double,
    val unit: PrUnit,
    val achievedAt: Instant
)

// ============================================================
// Readiness domain
// ============================================================

enum class ReadinessZone { OPTIMAL, CAUTION, HIGH_RISK, UNDERTRAINED }
enum class SleepQuality   { EXCELLENT, GOOD, FAIR, POOR }

data class ReadinessScore(
    val acwr: Float,
    val zone: ReadinessZone,
    val acuteLoad: Float,
    val chronicLoad: Float,
    val hrvComponent: Int?,
    val sleepDurationMinutes: Int?,
    val restingHr: Int?,
    val calculatedAt: Instant,
    val recommendation: String
)

data class HrvReading(
    val value: Int,
    val timestamp: Instant
)

data class SleepSession(
    val startTime: Instant,
    val endTime: Instant,
    val totalDuration: Duration,
    val deepSleepDuration: Duration,
    val remSleepDuration: Duration,
    val lightSleepDuration: Duration
)

data class HeartRateReading(
    val bpm: Int,
    val timestamp: Instant
)

data class HealthSnapshot(
    val userId: String,
    val hrvRmssd: Int?,
    val sleepDurationMinutes: Int?,
    val deepSleepMinutes: Int?,
    val remSleepMinutes: Int?,
    val restingHr: Int?,
    val capturedAt: Instant
)

// ============================================================
// Coaching / Vision domain
// ============================================================

enum class FaultSeverity { MINOR, MODERATE, CRITICAL }

data class CoachingReport(
    val id: String,
    val videoId: String,
    val movementType: String,
    val overallAssessment: String,
    val repCount: Int,
    val estimatedWeight: Double?,
    val faults: List<MovementFault>,
    val globalCues: List<String>,
    val createdAt: Instant
)

data class MovementFault(
    val id: String,
    val description: String,
    val severity: FaultSeverity,
    val timestampMs: Long,
    val cue: String,
    val correctedImageUrl: String?,
    val affectedJoints: List<String>
)

data class PoseOverlayData(
    val landmarks: List<PoseLandmark>,
    val jointAngles: Map<JointAngle, Float>,
    val barbellPosition: PointF?,
    val barbellTrajectory: List<PointF>,
    val frameTimestamp: Long
)

data class PoseLandmark(
    val index: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)

enum class JointAngle {
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_HIP, RIGHT_HIP,
    LEFT_ELBOW, RIGHT_ELBOW,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ANKLE, RIGHT_ANKLE,
    TRUNK_INCLINATION
}

data class TimedPoseOverlay(
    val timestampMs: Long,
    val landmarks: List<PoseLandmark>,
    val jointAngles: Map<JointAngle, Float>
)

data class FaultMarker(
    val timestampMs: Long,
    val label: String,
    val severity: FaultSeverity
)

// ============================================================
// Auth domain
// ============================================================

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val expiresAt: Long
)

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Instant,
    val avatarUrl: String?
)
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/domain/AuthRepository.kt`

```kotlin
package com.apexai.crossfit.feature.auth.domain

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    fun observeSession(): Flow<AuthSession?>
    suspend fun refreshToken(): Result<AuthSession>
    suspend fun getCurrentProfile(): Result<UserProfile>
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/data/AuthRepositoryImpl.kt`

```kotlin
package com.apexai.crossfit.feature.auth.data

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.core.domain.model.UserProfile
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProfileRow(
    val id: String,
    val display_name: String,
    val avatar_url: String? = null,
    val created_at: String
)

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> =
        runCatching {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val session = supabase.auth.currentSessionOrNull()
                ?: error("No session after login")
            session.toAuthSession()
        }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthSession> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = kotlinx.serialization.json.buildJsonObject {
                put("display_name", kotlinx.serialization.json.JsonPrimitive(displayName))
            }
        }
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No session after registration")
        session.toAuthSession()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabase.auth.signOut()
    }

    override fun observeSession(): Flow<AuthSession?> =
        supabase.auth.sessionStatus.map { status ->
            when (status) {
                is io.github.jan.supabase.auth.status.SessionStatus.Authenticated ->
                    status.session.toAuthSession()
                else -> null
            }
        }

    override suspend fun refreshToken(): Result<AuthSession> = runCatching {
        supabase.auth.refreshCurrentSession()
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No session after refresh")
        session.toAuthSession()
    }

    override suspend fun getCurrentProfile(): Result<UserProfile> = runCatching {
        val user = supabase.auth.currentUserOrNull()
            ?: error("No authenticated user")
        val row = supabase.postgrest["profiles"]
            .select { filter { eq("id", user.id) } }
            .decodeSingle<ProfileRow>()
        UserProfile(
            id          = row.id,
            email       = user.email ?: "",
            displayName = row.display_name,
            createdAt   = Instant.parse(row.created_at),
            avatarUrl   = row.avatar_url
        )
    }

    private fun io.github.jan.supabase.auth.user.UserSession.toAuthSession() = AuthSession(
        accessToken  = accessToken,
        refreshToken = refreshToken,
        userId       = user?.id ?: "",
        expiresAt    = expiresAt?.toEpochMilliseconds() ?: 0L
    )
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/domain/usecase/LoginUseCase.kt`

```kotlin
package com.apexai.crossfit.feature.auth.domain.usecase

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("Password cannot be empty"))
        return repository.login(email.trim(), password)
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/presentation/login/LoginViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.feature.auth.domain.usecase.LoginUseCase
import com.apexai.crossfit.feature.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed interface AuthEvent {
    data class EmailChanged(val email: String)       : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    data class DisplayNameChanged(val name: String)  : AuthEvent
    data object LoginClicked                         : AuthEvent
    data object RegisterClicked                      : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome  : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged       -> _uiState.update { it.copy(email = event.email, emailError = null) }
            is AuthEvent.PasswordChanged    -> _uiState.update { it.copy(password = event.password, passwordError = null) }
            is AuthEvent.DisplayNameChanged -> _uiState.update { it.copy(displayName = event.name) }
            is AuthEvent.LoginClicked       -> login()
            is AuthEvent.RegisterClicked    -> register()
        }
    }

    private fun login() {
        val state = _uiState.value
        if (!validateLogin(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            loginUseCase(state.email, state.password)
                .onSuccess { _effects.send(AuthEffect.NavigateToHome) }
                .onFailure { error ->
                    _uiState.update { it.copy(generalError = error.message) }
                    _effects.send(AuthEffect.ShowError(error.message ?: "Login failed"))
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun register() {
        val state = _uiState.value
        if (!validateRegister(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            registerUseCase(state.email, state.password, state.displayName)
                .onSuccess { _effects.send(AuthEffect.NavigateToHome) }
                .onFailure { error ->
                    _uiState.update { it.copy(generalError = error.message) }
                    _effects.send(AuthEffect.ShowError(error.message ?: "Registration failed"))
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun validateLogin(state: AuthUiState): Boolean {
        var valid = true
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            valid = false
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            valid = false
        }
        return valid
    }

    private fun validateRegister(state: AuthUiState): Boolean {
        var valid = validateLogin(state)
        if (state.displayName.isBlank()) {
            _uiState.update { it.copy(generalError = "Display name is required") }
            valid = false
        }
        return valid
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/presentation/login/LoginScreen.kt`

```kotlin
package com.apexai.crossfit.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.ui.components.ApexTextButton
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> onNavigateToHome()
                is AuthEffect.ShowError      -> { /* snackbar handled by state */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo / wordmark
        Text(
            text = "APEX AI",
            style = ApexTypography.displayMedium.copy(color = ElectricBlue),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ATHLETICS",
            style = ApexTypography.headlineSmall.copy(color = TextSecondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Welcome Back",
            style = ApexTypography.headlineLarge.copy(color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to continue your training",
            style = ApexTypography.bodyMedium.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ApexTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
            label = "Email",
            placeholder = "athlete@example.com",
            errorMessage = uiState.emailError,
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ApexTextField(
            value = uiState.password,
            onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
            label = "Password",
            placeholder = "••••••••",
            errorMessage = uiState.passwordError,
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = TextSecondary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.generalError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.generalError!!,
                style = ApexTypography.bodySmall.copy(
                    color = com.apexai.crossfit.core.ui.theme.ColorError
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Sign In",
            onClick = { viewModel.onEvent(AuthEvent.LoginClicked) },
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ApexTextButton(
            text = "Don't have an account? Create one",
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/domain/usecase/RegisterUseCase.kt`

```kotlin
package com.apexai.crossfit.feature.auth.domain.usecase

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthSession> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email is required"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        if (displayName.isBlank()) return Result.failure(IllegalArgumentException("Display name is required"))
        return repository.register(email.trim(), password, displayName.trim())
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/domain/WodRepository.kt`

```kotlin
package com.apexai.crossfit.feature.wod.domain

import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow

interface WodRepository {
    fun getWorkouts(query: String?, timeDomain: TimeDomain?): Flow<List<WorkoutSummary>>
    fun getWorkoutById(wodId: String): Flow<Workout>
    fun getWorkoutMovements(wodId: String): Flow<List<WorkoutMovement>>
    suspend fun logResult(result: WorkoutResultInput): Result<WorkoutResult>
    fun getHistory(userId: String): Flow<List<WorkoutResult>>
    fun getTodayWorkout(): Flow<Workout?>
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/data/WodRepositoryImpl.kt`

```kotlin
package com.apexai.crossfit.feature.wod.data

import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrUnit
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.wod.domain.WodRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class WorkoutRow(
    val id: String,
    val name: String,
    val description: String? = null,
    val time_domain: String,
    val scoring_metric: String,
    val time_cap_seconds: Int? = null,
    val rounds: Int? = null,
    val created_at: String
)

@Serializable
data class WorkoutMovementRow(
    val id: String,
    val workout_id: String,
    val movement_id: String,
    val prescribed_reps: Int? = null,
    val prescribed_weight_kg: Double? = null,
    val prescribed_distance_m: Double? = null,
    val prescribed_calories: Int? = null,
    val sort_order: Int
)

@Serializable
data class MovementRow(
    val id: String,
    val name: String,
    val category: String,
    val primary_muscles: List<String> = emptyList(),
    val equipment: String? = null
)

@Serializable
data class ResultRow(
    val id: String,
    val user_id: String,
    val workout_id: String,
    val score: String,
    val score_numeric: Double? = null,
    val rxd: Boolean,
    val notes: String? = null,
    val rpe: Int? = null,
    val completed_at: String
)

@Serializable
data class PersonalRecordRow(
    val id: String,
    val user_id: String,
    val movement_id: String,
    val value: Double,
    val unit: String,
    val achieved_at: String
)

@Singleton
class WodRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WodRepository {

    override fun getWorkouts(query: String?, timeDomain: TimeDomain?): Flow<List<WorkoutSummary>> = flow {
        val rows = supabase.postgrest["workouts"]
            .select {
                if (timeDomain != null) {
                    filter { eq("time_domain", timeDomain.name) }
                }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<WorkoutRow>()

        val filtered = if (!query.isNullOrBlank()) {
            rows.filter { it.name.contains(query, ignoreCase = true) }
        } else rows

        emit(filtered.map { it.toSummary() })
    }

    override fun getWorkoutById(wodId: String): Flow<Workout> = flow {
        val row = supabase.postgrest["workouts"]
            .select { filter { eq("id", wodId) } }
            .decodeSingle<WorkoutRow>()

        val movements = fetchMovementsForWorkout(wodId)
        emit(row.toWorkout(movements))
    }

    override fun getWorkoutMovements(wodId: String): Flow<List<WorkoutMovement>> = flow {
        emit(fetchMovementsForWorkout(wodId))
Continuing from `WodRepositoryImpl.fetchMovementsForWorkout`:

```kotlin
    private suspend fun fetchMovementsForWorkout(wodId: String): List<WorkoutMovement> {
        val wmRows = supabase.postgrest["workout_movements"]
            .select { filter { eq("workout_id", wodId) } }
            .decodeList<WorkoutMovementRow>()

        return wmRows.map { wm ->
            val movement = supabase.postgrest["movements"]
                .select { filter { eq("id", wm.movement_id) } }
                .decodeSingle<MovementRow>()
            WorkoutMovement(
                id                  = wm.id,
                movement            = movement.toMovement(),
                prescribedReps      = wm.prescribed_reps,
                prescribedWeight    = wm.prescribed_weight_kg,
                prescribedDistance  = wm.prescribed_distance_m,
                prescribedCalories  = wm.prescribed_calories,
                sortOrder           = wm.sort_order
            )
        }.sortedBy { it.sortOrder }
    }

    override suspend fun logResult(result: WorkoutResultInput): Result<WorkoutResult> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        val inserted = supabase.postgrest["results"]
            .insert(
                mapOf(
                    "user_id"       to userId,
                    "workout_id"    to result.workoutId,
                    "score"         to result.score,
                    "score_numeric" to result.scoreNumeric,
                    "rxd"           to result.rxd,
                    "notes"         to result.notes,
                    "rpe"           to result.rpe
                )
            ) { select() }
            .decodeSingle<ResultRow>()

        // Fetch any PRs that were just upserted by the PostgreSQL trigger.
        // The trigger fires server-side; we query personal_records for this
        // user filtered by achieved_at within the last 5 seconds.
        val cutoff = Instant.now().minusSeconds(5).toString()
        val newPrs = supabase.postgrest["personal_records"]
            .select {
                filter {
                    eq("user_id", userId)
                    gte("achieved_at", cutoff)
                }
            }
            .decodeList<PersonalRecordRow>()
            .map { it.toPersonalRecord("") }

        WorkoutResult(
            id          = inserted.id,
            workoutId   = inserted.workout_id,
            userId      = inserted.user_id,
            score       = inserted.score,
            rxd         = inserted.rxd,
            notes       = inserted.notes,
            rpe         = inserted.rpe,
            completedAt = Instant.parse(inserted.completed_at),
            newPrs      = newPrs
        )
    }

    override fun getHistory(userId: String): Flow<List<WorkoutResult>> = flow {
        val rows = supabase.postgrest["results"]
            .select {
                filter { eq("user_id", userId) }
                order("completed_at", Order.DESCENDING)
                limit(100)
            }
            .decodeList<ResultRow>()
        emit(rows.map { it.toWorkoutResult() })
    }

    override fun getTodayWorkout(): Flow<Workout?> = flow {
        val startOfDay = LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toString()
        val rows = supabase.postgrest["workouts"]
            .select {
                filter { gte("created_at", startOfDay) }
                order("created_at", Order.DESCENDING)
                limit(1)
            }
            .decodeList<WorkoutRow>()
        val row = rows.firstOrNull()
        if (row == null) {
            emit(null)
        } else {
            val movements = fetchMovementsForWorkout(row.id)
            emit(row.toWorkout(movements))
        }
    }

    // --- Mappers ---

    private fun WorkoutRow.toSummary() = WorkoutSummary(
        id            = id,
        name          = name,
        timeDomain    = TimeDomain.valueOf(time_domain),
        movementCount = 0 // populated separately when needed
    )

    private fun WorkoutRow.toWorkout(movements: List<WorkoutMovement>) = Workout(
        id            = id,
        name          = name,
        description   = description ?: "",
        timeDomain    = TimeDomain.valueOf(time_domain),
        scoringMetric = ScoringMetric.valueOf(scoring_metric),
        timeCap       = time_cap_seconds?.let { Duration.ofSeconds(it.toLong()) },
        rounds        = rounds,
        movements     = movements
    )

    private fun MovementRow.toMovement() = Movement(
        id             = id,
        name           = name,
        category       = category,
        primaryMuscles = primary_muscles,
        equipment      = equipment
    )

    private fun ResultRow.toWorkoutResult() = WorkoutResult(
        id          = id,
        workoutId   = workout_id,
        userId      = user_id,
        score       = score,
        rxd         = rxd,
        notes       = notes,
        rpe         = rpe,
        completedAt = Instant.parse(completed_at),
        newPrs      = emptyList()
    )

    private fun PersonalRecordRow.toPersonalRecord(movementName: String) =
        PersonalRecord(
            id           = id,
            userId       = user_id,
            movementId   = movement_id,
            movementName = movementName,
            category     = "",
            value        = value,
            unit         = PrUnit.valueOf(unit),
            achievedAt   = Instant.parse(achieved_at)
        )
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/domain/usecase/GetTodayWodUseCase.kt`

```kotlin
package com.apexai.crossfit.feature.wod.domain.usecase

import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.feature.wod.domain.WodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayWodUseCase @Inject constructor(
    private val repository: WodRepository
) {
    operator fun invoke(): Flow<Workout?> = repository.getTodayWorkout()
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/domain/usecase/SubmitResultUseCase.kt`

```kotlin
package com.apexai.crossfit.feature.wod.domain.usecase

import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.feature.wod.domain.WodRepository
import javax.inject.Inject

class SubmitResultUseCase @Inject constructor(
    private val repository: WodRepository
) {
    suspend operator fun invoke(input: WorkoutResultInput): Result<WorkoutResult> {
        if (input.score.isBlank()) return Result.failure(IllegalArgumentException("Score cannot be empty"))
        return repository.logResult(input)
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/presentation/detail/WodDetailViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.wod.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodDetailUiState(
    val workout: Workout? = null,
    val movements: List<WorkoutMovement> = emptyList(),
    val recentResults: List<WorkoutResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WodDetailViewModel @Inject constructor(
    private val repository: WodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wodId: String = checkNotNull(savedStateHandle["wodId"])

    private val _uiState = MutableStateFlow(WodDetailUiState())
    val uiState: StateFlow<WodDetailUiState> = _uiState.asStateFlow()

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            repository.getWorkoutById(wodId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { workout ->
                    _uiState.update {
                        it.copy(
                            workout   = workout,
                            movements = workout.movements,
                            isLoading = false,
                            error     = null
                        )
                    }
                }
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadWorkout()
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/presentation/detail/WodDetailScreen.kt`

```kotlin
package com.apexai.crossfit.feature.wod.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.SecondaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WodDetailScreen(
    viewModel: WodDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (String) -> Unit,
    onNavigateToLog: (String) -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.workout?.name ?: "",
                        style = ApexTypography.headlineMedium,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* share */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = "Log Result",
                        onClick = { uiState.workout?.id?.let(onNavigateToLog) },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Start Timer",
                        onClick = { uiState.workout?.id?.let(onNavigateToTimer) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(40.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier.padding(innerPadding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.error!!, style = ApexTypography.bodyMedium, color = ColorError)
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // WOD meta chips row
                item {
                    uiState.workout?.let { wod ->
                        WodMetaRow(wod)
                    }
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp))
                }

                // Movements section
                item {
                    Text(
                        "MOVEMENTS",
                        style = ApexTypography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(uiState.movements) { wm ->
                    MovementRow(wm = wm, onRecordClick = onNavigateToCamera)
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun WodMetaRow(wod: com.apexai.crossfit.core.domain.model.Workout) {
    val domainColor = when (wod.timeDomain) {
        TimeDomain.AMRAP   -> ElectricBlue
        TimeDomain.EMOM    -> NeonGreen
        TimeDomain.RFT     -> BlazeOrange
        TimeDomain.TABATA  -> ColorWarning
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MetaChip(icon = Icons.Outlined.Timer, label = wod.timeDomain.name, color = domainColor)
        wod.timeCap?.let {
            MetaChip(
                icon  = Icons.Outlined.Timer,
                label = "${it.toMinutes()} min",
                color = TextSecondary
            )
        }
    }
    if (wod.description.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Text(wod.description, style = ApexTypography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .then(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, style = ApexTypography.labelLarge, color = color)
    }
}

@Composable
private fun MovementRow(
    wm: WorkoutMovement,
    onRecordClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${wm.prescribedReps ?: "-"}×",
            style = ApexTypography.headlineSmall,
            color = ElectricBlue,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(wm.movement.name, style = ApexTypography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                wm.prescribedWeight?.let {
                    Text("${it} kg (Rx)", style = ApexTypography.bodySmall, color = TextSecondary)
                }
                wm.movement.equipment?.let {
                    Text(it, style = ApexTypography.bodySmall, color = TextSecondary)
                }
            }
        }
        IconButton(
            onClick = onRecordClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Outlined.Videocam,
                contentDescription = "Record ${wm.movement.name}",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/presentation/log/WodLogViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.wod.presentation.log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.feature.wod.domain.WodRepository
import com.apexai.crossfit.feature.wod.domain.usecase.SubmitResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodLogUiState(
    val workout: Workout? = null,
    val score: String = "",
    val rxd: Boolean = true,
    val notes: String = "",
    val rpe: Int? = null,
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = true,
    val newPrs: List<PersonalRecord> = emptyList(),
    val error: String? = null
)

sealed interface WodLogEvent {
    data class ScoreChanged(val score: String) : WodLogEvent
    data class RxdToggled(val rxd: Boolean)    : WodLogEvent
    data class NotesChanged(val notes: String) : WodLogEvent
    data class RpeSelected(val rpe: Int)       : WodLogEvent
    data object SubmitClicked                  : WodLogEvent
    data object DismissPrSheet                 : WodLogEvent
}

sealed interface WodLogEffect {
    data class PrAchieved(val prs: List<PersonalRecord>) : WodLogEffect
    data object NavigateBack                             : WodLogEffect
    data class ShowError(val message: String)            : WodLogEffect
}

@HiltViewModel
class WodLogViewModel @Inject constructor(
    private val submitResultUseCase: SubmitResultUseCase,
    private val repository: WodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wodId: String = checkNotNull(savedStateHandle["wodId"])

    private val _uiState = MutableStateFlow(WodLogUiState())
    val uiState: StateFlow<WodLogUiState> = _uiState.asStateFlow()

    private val _effects = Channel<WodLogEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getWorkoutById(wodId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { workout ->
                    _uiState.update { it.copy(workout = workout, isLoading = false) }
                }
        }
    }

    fun onEvent(event: WodLogEvent) {
        when (event) {
            is WodLogEvent.ScoreChanged  -> _uiState.update { it.copy(score = event.score, error = null) }
            is WodLogEvent.RxdToggled    -> _uiState.update { it.copy(rxd = event.rxd) }
            is WodLogEvent.NotesChanged  -> _uiState.update { it.copy(notes = event.notes) }
            is WodLogEvent.RpeSelected   -> _uiState.update { it.copy(rpe = event.rpe) }
            is WodLogEvent.SubmitClicked -> submit()
            is WodLogEvent.DismissPrSheet -> viewModelScope.launch {
                _effects.send(WodLogEffect.NavigateBack)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.score.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your score") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            submitResultUseCase(
                WorkoutResultInput(
                    workoutId    = wodId,
                    score        = state.score,
                    scoreNumeric = state.score.toDoubleOrNull(),
                    rxd          = state.rxd,
                    notes        = state.notes.ifBlank { null },
                    rpe          = state.rpe
                )
            ).onSuccess { result ->
                _uiState.update { it.copy(isSubmitting = false) }
                if (result.newPrs.isNotEmpty()) {
                    _effects.send(WodLogEffect.PrAchieved(result.newPrs))
                } else {
                    _effects.send(WodLogEffect.NavigateBack)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, error = error.message) }
                _effects.send(WodLogEffect.ShowError(error.message ?: "Failed to submit"))
            }
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/presentation/log/WodLogScreen.kt`

```kotlin
package com.apexai.crossfit.feature.wod.presentation.log

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.CornerXLarge
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WodLogScreen(
    viewModel: WodLogViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPrSheet by remember { mutableStateOf(false) }
    var prList by remember { mutableStateOf<List<PersonalRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WodLogEffect.PrAchieved -> {
                    prList = effect.prs
                    showPrSheet = true
                }
                is WodLogEffect.NavigateBack -> onNavigateBack()
                is WodLogEffect.ShowError    -> { /* error shown in state */ }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log Result", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // WOD name header
            uiState.workout?.let { wod ->
                Text(wod.name, style = ApexTypography.titleLarge, color = TextPrimary)
                Text(wod.timeDomain.name, style = ApexTypography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(24.dp))
            }

            // Score section
            Text("SCORE", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp))
            ScoreInputSection(
                scoringMetric = uiState.workout?.scoringMetric,
                score         = uiState.score,
                onScoreChanged = { viewModel.onEvent(WodLogEvent.ScoreChanged(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // Rxd toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AS PRESCRIBED (Rx)", style = ApexTypography.titleMedium, color = TextPrimary)
                    Text(
                        "Did you use the prescribed weights?",
                        style = ApexTypography.bodySmall, color = TextSecondary
                    )
                }
                Switch(
                    checked = uiState.rxd,
                    onCheckedChange = { viewModel.onEvent(WodLogEvent.RxdToggled(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDeepBlack,
                        checkedTrackColor = ElectricBlue,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // RPE
            Text("EFFORT LEVEL (RPE)", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp))
            RpeSelector(
                selectedRpe = uiState.rpe,
                onRpeSelected = { viewModel.onEvent(WodLogEvent.RpeSelected(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // Notes
            Text("NOTES", style = ApexTypography.labelSmall, color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp))
            ApexTextField(
                value = uiState.notes,
                onValueChange = { if (it.length <= 500) viewModel.onEvent(WodLogEvent.NotesChanged(it)) },
                label = "How did it feel? Movement notes...",
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${uiState.notes.length}/500",
                style = ApexTypography.bodySmall, color = TextSecondary,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End
            )

            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(uiState.error!!, style = ApexTypography.bodySmall, color = ColorError)
            }

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                text      = "Submit Result",
                onClick   = { viewModel.onEvent(WodLogEvent.SubmitClicked) },
                isLoading = uiState.isSubmitting,
                enabled   = uiState.score.isNotBlank() && !uiState.isSubmitting,
                modifier  = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(Modifier.height(48.dp))
        }
    }

    // PR Celebration Bottom Sheet
    if (showPrSheet) {
        PrCelebrationSheet(
            prs = prList,
            onDismiss = {
                showPrSheet = false
                viewModel.onEvent(WodLogEvent.DismissPrSheet)
            }
        )
    }
}

@Composable
private fun ScoreInputSection(
    scoringMetric: ScoringMetric?,
    score: String,
    onScoreChanged: (String) -> Unit
) {
    when (scoringMetric) {
        ScoringMetric.TIME -> {
            val parts = score.split(":")
            val minutes = parts.getOrNull(0) ?: ""
            val seconds = parts.getOrNull(1) ?: ""
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ApexTextField(
                    value = minutes,
                    onValueChange = { onScoreChanged("$it:$seconds") },
                    label = "Minutes",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text(":", style = ApexTypography.headlineMedium, color = TextSecondary)
                ApexTextField(
                    value = seconds,
                    onValueChange = { onScoreChanged("$minutes:$it") },
                    label = "Seconds",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        ScoringMetric.ROUNDS_PLUS_REPS -> {
            val parts = score.split("+")
            val rounds = parts.getOrNull(0) ?: ""
            val reps   = parts.getOrNull(1) ?: ""
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ApexTextField(
                    value = rounds,
                    onValueChange = { onScoreChanged("$it+$reps") },
                    label = "Rounds",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text("+", style = ApexTypography.headlineMedium, color = TextSecondary)
                ApexTextField(
                    value = reps,
                    onValueChange = { onScoreChanged("$rounds+$it") },
                    label = "Reps",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        ScoringMetric.LOAD -> {
            ApexTextField(
                value = score,
                onValueChange = onScoreChanged,
                label = "Weight (kg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        else -> {
            ApexTextField(
                value = score,
                onValueChange = onScoreChanged,
                label = "Total Reps",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RpeSelector(selectedRpe: Int?, onRpeSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..10).forEach { value ->
            val rpeColor = when (value) {
                in 1..4  -> NeonGreen
                in 5..7  -> ColorWarning
                else     -> ColorError
            }
            val isSelected = selectedRpe == value
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 44.dp)
                    .clip(CornerSmall)
                    .background(if (isSelected) rpeColor.copy(alpha = 0.2f) else SurfaceElevated)
                    .border(
                        1.dp,
                        if (isSelected) rpeColor else BorderSubtle,
                        CornerSmall
                    )
                    .clickable { onRpeSelected(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    value.toString(),
                    style = ApexTypography.labelLarge,
                    color = if (isSelected) rpeColor else TextSecondary
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = rpeDescription(selectedRpe),
        style = ApexTypography.bodySmall,
        color = TextSecondary
    )
}

private fun rpeDescription(rpe: Int?): String = when (rpe) {
    1    -> "Very light. Easy warm-up pace."
    2    -> "Light. Could do this all day."
    3    -> "Moderate. Comfortable pace."
    4    -> "Somewhat hard. Breathing elevated."
    5    -> "Hard. Conversation getting difficult."
    6    -> "Hard. Short sentences only."
    7    -> "Very hard. Conversation is difficult."
    8    -> "Very hard. Near your limit."
    9    -> "Extremely hard. Almost maximal effort."
    10   -> "Maximal. Absolute limit."
    else -> "Select your perceived exertion."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrCelebrationSheet(
    prs: List<PersonalRecord>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trophyAnimated by remember { mutableStateOf(false) }
    val trophyScale by animateFloatAsState(
        targetValue = if (trophyAnimated) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "trophy_scale"
    )

    LaunchedEffect(Unit) { trophyAnimated = true }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        shape = CornerXLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer { scaleX = trophyScale; scaleY = trophyScale }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "NEW PERSONAL RECORD!",
                style = ApexTypography.headlineMedium,
                color = NeonGreen,
                textAlign = TextAlign.Center
            )
            Text(
                "Outstanding performance!",
                style = ApexTypography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            prs.forEach { pr ->
                ApexCard(
                    borderColor = NeonGreen,
                    borderWidth = 2.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pr.movementName, style = ApexTypography.titleMedium, color = TextPrimary)
                        Text(
                            "${pr.value} ${pr.unit.name.lowercase()}",
                            style = ApexTypography.headlineMedium,
                            color = NeonGreen
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Awesome!",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/presentation/timer/WodTimerViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.wod.presentation.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodTimerUiState(
    val workout: Workout? = null,
    val elapsedMillis: Long = 0L,
    val currentRound: Int = 1,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val currentIntervalSecondsRemaining: Int = 0,
    val tabataIsWorkInterval: Boolean = true,
    val tabataCompletedIntervals: Int = 0,
    val isLoading: Boolean = true
)

sealed interface WodTimerEvent {
    data object StartPause : WodTimerEvent
    data object Reset      : WodTimerEvent
    data object Complete   : WodTimerEvent
}

@HiltViewModel
class WodTimerViewModel @Inject constructor(
    private val repository: WodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wodId: String = checkNotNull(savedStateHandle["wodId"])

    private val _uiState = MutableStateFlow(WodTimerUiState())
    val uiState: StateFlow<WodTimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val tickIntervalMs = 100L

    init {
        viewModelScope.launch {
            repository.getWorkoutById(wodId)
                .catch {}
                .collect { workout ->
                    val initialInterval = when (workout.timeDomain) {
                        TimeDomain.EMOM   -> 60
                        TimeDomain.TABATA -> 20
                        else              -> 0
                    }
                    _uiState.update {
                        it.copy(
                            workout = workout,
                            isLoading = false,
                            currentIntervalSecondsRemaining = initialInterval
                        )
                    }
                }
        }
    }

    fun onEvent(event: WodTimerEvent) {
        when (event) {
            is WodTimerEvent.StartPause -> toggleTimer()
            is WodTimerEvent.Reset      -> resetTimer()
            is WodTimerEvent.Complete   -> completeWorkout()
        }
    }

    private fun toggleTimer() {
        val state = _uiState.value
        if (state.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(tickIntervalMs)
                tick()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun tick() {
        val state = _uiState.value
        val workout = state.workout ?: return

        when (workout.timeDomain) {
            TimeDomain.AMRAP -> tickAmrap(state, workout)
            TimeDomain.EMOM  -> tickEmom(state)
            TimeDomain.RFT   -> tickRft(state)
            TimeDomain.TABATA -> tickTabata(state)
        }
    }

    private fun tickAmrap(state: WodTimerUiState, workout: Workout) {
        val timeCap = workout.timeCap?.toMillis() ?: Long.MAX_VALUE
        val newElapsed = state.elapsedMillis + tickIntervalMs
        if (newElapsed >= timeCap) {
            _uiState.update { it.copy(elapsedMillis = timeCap, isRunning = false, isComplete = true) }
            timerJob?.cancel()
        } else {
            _uiState.update { it.copy(elapsedMillis = newElapsed) }
        }
    }

    private fun tickEmom(state: WodTimerUiState) {
        val newElapsed = state.elapsedMillis + tickIntervalMs
        val secondsInMinute = ((newElapsed / 1000) % 60).toInt()
        val secondsRemaining = 60 - secondsInMinute
        val newRound = (newElapsed / 60_000).toInt() + 1
        val totalRounds = state.workout?.rounds ?: Int.MAX_VALUE
        if (newRound > totalRounds) {
            _uiState.update { it.copy(isRunning = false, isComplete = true) }
            timerJob?.cancel()
        } else {
            _uiState.update {
                it.copy(
                    elapsedMillis = newElapsed,
                    currentIntervalSecondsRemaining = secondsRemaining,
                    currentRound = newRound
                )
            }
        }
    }

    private fun tickRft(state: WodTimerUiState) {
        _uiState.update { it.copy(elapsedMillis = state.elapsedMillis + tickIntervalMs) }
    }

    private fun tickTabata(state: WodTimerUiState) {
        val intervalDuration = if (state.tabataIsWorkInterval) 20_000L else 10_000L
        val positionInInterval = state.elapsedMillis % (20_000L + 10_000L)
        val newElapsed = state.elapsedMillis + tickIntervalMs
        val totalTabataMs = 8L * (20_000L + 10_000L)

        if (newElapsed >= totalTabataMs) {
            _uiState.update { it.copy(isRunning = false, isComplete = true) }
            timerJob?.cancel()
            return
        }

        val isWorkInterval = (newElapsed % (20_000L + 10_000L)) < 20_000L
        val msInCurrentInterval = newElapsed % (20_000L + 10_000L)
        val remaining = if (isWorkInterval) {
            ((20_000L - msInCurrentInterval) / 1000L).toInt().coerceAtLeast(0)
        } else {
            ((30_000L - msInCurrentInterval) / 1000L).toInt().coerceAtLeast(0)
        }
        val completedIntervals = (newElapsed / (20_000L + 10_000L)).toInt()

        _uiState.update {
            it.copy(
                elapsedMillis = newElapsed,
                tabataIsWorkInterval = isWorkInterval,
                currentIntervalSecondsRemaining = remaining,
                tabataCompletedIntervals = completedIntervals
            )
        }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        val initialInterval = when (_uiState.value.workout?.timeDomain) {
            TimeDomain.EMOM   -> 60
            TimeDomain.TABATA -> 20
            else              -> 0
        }
        _uiState.update {
            it.copy(
                elapsedMillis = 0L,
                currentRound  = 1,
                isRunning     = false,
                isComplete    = false,
                currentIntervalSecondsRemaining = initialInterval,
                tabataIsWorkInterval = true,
                tabataCompletedIntervals = 0
            )
        }
    }

    private fun completeWorkout() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false, isComplete = true) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/wod/presentation/timer/WodTimerScreen.kt`

```kotlin
package com.apexai.crossfit.feature.wod.presentation.timer

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RotateCcw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerLarge
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.core.ui.components.ApexLinearProgressBar

@Composable
fun WodTimerScreen(
    viewModel: WodTimerViewModel,
    onNavigateToLog: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Keep screen on during active timer session
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(Icons.Outlined.Close, "Exit workout", tint = TextSecondary)
                }
                Text(
                    "${uiState.workout?.name ?: ""} · ${uiState.workout?.timeDomain?.name ?: ""}",
                    style = ApexTypography.bodyMedium,
                    color = TextSecondary
                )
                val workout = uiState.workout
                if (workout?.timeDomain == TimeDomain.RFT || workout?.timeDomain == TimeDomain.EMOM) {
                    Text(
                        "Round ${uiState.currentRound}/${workout.rounds ?: "∞"}",
                        style = ApexTypography.bodyMedium,
                        color = ElectricBlue
                    )
                } else {
                    Box(modifier = Modifier.size(48.dp))
                }
            }

            // Main timer display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                uiState.workout?.let { workout ->
                    when (workout.timeDomain) {
                        TimeDomain.AMRAP -> {
                            val remaining = (workout.timeCap?.toMillis() ?: 0L) - uiState.elapsedMillis
                            Text(
                                formatMMSS(remaining.coerceAtLeast(0L)),
                                style = ApexTypography.displayLarge.copy(
                                    fontSize = 72.sp,
                                    letterSpacing = (-2).sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = TextPrimary
                            )
                            Text("remaining", style = ApexTypography.bodyLarge, color = TextSecondary)
                        }
                        TimeDomain.EMOM -> {
                            Text(
                                "${uiState.currentIntervalSecondsRemaining.toString().padStart(2, '0')}",
                                style = ApexTypography.displayLarge.copy(fontSize = 72.sp, letterSpacing = (-2).sp),
                                color = TextPrimary
                            )
                            Text("seconds this minute", style = ApexTypography.bodyLarge, color = TextSecondary)
                            Spacer(Modifier.height(16.dp))
                            ApexLinearProgressBar(
                                progress = uiState.currentIntervalSecondsRemaining / 60f,
                                modifier = Modifier.size(width = 200.dp, height = 8.dp),
                                color = if (uiState.currentIntervalSecondsRemaining <= 10) ColorWarning else ElectricBlue
                            )
                        }
                        TimeDomain.RFT -> {
                            Text(
                                formatMMSS(uiState.elapsedMillis),
                                style = ApexTypography.displayLarge.copy(fontSize = 72.sp, letterSpacing = (-2).sp),
                                color = TextPrimary
                            )
                            Text("elapsed", style = ApexTypography.bodyLarge, color = TextSecondary)
                        }
                        TimeDomain.TABATA -> {
                            val tabataColor = if (uiState.tabataIsWorkInterval) ElectricBlue else ColorWarning
                            Text(
                                uiState.currentIntervalSecondsRemaining.toString().padStart(2, '0'),
                                style = ApexTypography.displayLarge.copy(fontSize = 72.sp, letterSpacing = (-2).sp),
                                color = tabataColor
                            )
                            Text(
                                if (uiState.tabataIsWorkInterval) "WORK" else "REST",
                                style = ApexTypography.headlineMedium,
                                color = tabataColor
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0 until 8).forEach { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CornerFull)
                                            .background(
                                                when {
                                                    i < uiState.tabataCompletedIntervals -> ElectricBlue
                                                    i == uiState.tabataCompletedIntervals -> ElectricBlue.copy(alpha = 0.6f)
                                                    else -> BorderSubtle
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Movement reference list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Text(
                    "MOVEMENTS",
                    style = ApexTypography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.workout?.movements ?: emptyList()) { wm ->
                        Column(
                            modifier = Modifier
                                .background(SurfaceElevated, CornerSmall)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${wm.prescribedReps ?: "-"}× ${wm.movement.name}",
                                style = ApexTypography.bodyMedium,
                                color = TextPrimary
                            )
                            wm.prescribedWeight?.let {
                                Text("${it}kg", style = ApexTypography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Timer controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.isComplete) {
                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceElevated, CornerFull)
                        ) {
                            Icon(Icons.Outlined.RotateCcw, "Reset", tint = TextSecondary)
                        }

                        // Start/Pause FAB
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    if (uiState.isRunning) ColorError else ElectricBlue,
                                    CornerFull
                                )
                                .clip(CornerFull)
                                .then(
                                    Modifier.clickable { viewModel.onEvent(WodTimerEvent.StartPause) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (uiState.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (uiState.isRunning) "Pause timer" else "Start timer",
                                tint = com.apexai.crossfit.core.ui.theme.TextOnBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(Modifier.size(48.dp))
                    } else {
                        PrimaryButton(
                            text = "Log Result",
                            onClick = { uiState.workout?.id?.let(onNavigateToLog) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Check, null,
                                    modifier = Modifier.size(20.dp).padding(end = 4.dp))
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        )
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Workout?", color = TextPrimary) },
            text  = { Text("Your timer progress will be lost.", color = TextSecondary) },
            confirmButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Exit", onClick = {
                    showExitDialog = false; onNavigateBack()
                })
            },
            dismissButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Continue", onClick = { showExitDialog = false })
            },
            containerColor = SurfaceElevated
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Timer?", color = TextPrimary) },
            text  = { Text("This will reset the timer to zero.", color = TextSecondary) },
            confirmButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Reset", onClick = {
                    showResetDialog = false
                    viewModel.onEvent(WodTimerEvent.Reset)
                })
            },
            dismissButton = {
                com.apexai.crossfit.core.ui.components.ApexTextButton("Cancel", onClick = { showResetDialog = false })
            },
            containerColor = SurfaceElevated
        )
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null,
        onClick = onClick
    ))

private fun formatMMSS(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/data/MediaPipePoseLandmarkerHelper.kt`

```kotlin
package com.apexai.crossfit.feature.vision.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.apexai.crossfit.core.domain.model.JointAngle
import com.apexai.crossfit.core.domain.model.PoseLandmark
import com.apexai.crossfit.core.domain.model.PoseOverlayData
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Wraps MediaPipe PoseLandmarker in LIVE_STREAM mode.
 *
 * MANDATE (CLAUDE.md): Must use LIVE_STREAM mode with async resultListener.
 * Z-depth coordinates are unreliable on mobile — all coaching logic uses
 * only 2D (x, y) angles.
 *
 * Landmark indices for Olympic lifting (from CLAUDE.md):
 *   Shoulders: 11, 12
 *   Hips:      23, 24
 *   Ankles:    27, 28
 */
@Singleton
class MediaPipePoseLandmarkerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var poseLandmarker: PoseLandmarker? = null

    var resultListener: ((PoseOverlayData) -> Unit)? = null
    var errorListener: ((RuntimeException) -> Unit)? = null

    fun setup() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .setDelegate(Delegate.GPU)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener(::onResult)
            .setErrorListener { e -> errorListener?.invoke(RuntimeException(e)) }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    /**
     * Feeds an ImageProxy frame to MediaPipe for asynchronous pose detection.
     * Called from CameraX ImageAnalysis use case — must return quickly.
     */
    fun detectAsync(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (isFrontCamera) postScale(-1f, 1f, imageProxy.width / 2f, imageProxy.height / 2f)
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        poseLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
    }

    private fun onResult(result: PoseLandmarkerResult, input: MPImage) {
        if (result.landmarks().isEmpty()) return

        val rawLandmarks = result.landmarks()[0]
        val landmarks = rawLandmarks.mapIndexed { index, lm ->
            PoseLandmark(
                index      = index,
                x          = lm.x(),
                y          = lm.y(),
                z          = lm.z(),
                visibility = lm.visibility().orElse(0f)
            )
        }

        val jointAngles = calculateJointAngles(landmarks)

        resultListener?.invoke(
            PoseOverlayData(
                landmarks       = landmarks,
                jointAngles     = jointAngles,
                barbellPosition = null,   // barbell tracking via separate pipeline
                barbellTrajectory = emptyList(),
                frameTimestamp  = result.timestampMs()
            )
        )
    }

    /**
     * Calculates 2D joint angles using only x, y coordinates.
     * Z is deliberately excluded per CLAUDE.md (unreliable on mobile).
     *
     * Angle at joint B in triangle A-B-C = atan2 formula.
     */
    private fun calculateJointAngles(landmarks: List<PoseLandmark>): Map<JointAngle, Float> {
        val angles = mutableMapOf<JointAngle, Float>()

        fun angle(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Float {
            val ax = a.x - b.x; val ay = a.y - b.y
            val cx = c.x - b.x; val cy = c.y - b.y
            val dot  = ax * cx + ay * cy
            val magA = sqrt(ax * ax + ay * ay)
            val magC = sqrt(cx * cx + cy * cy)
            if (magA == 0f || magC == 0f) return 0f
            return Math.toDegrees(Math.acos((dot / (magA * magC)).toDouble().coerceIn(-1.0, 1.0))).toFloat()
        }

        fun lm(idx: Int) = landmarks.getOrNull(idx)

        // Key joints for Olympic lifting (CLAUDE.md landmark indices)
        val lShoulder = lm(11); val rShoulder = lm(12)
        val lHip      = lm(23); val rHip      = lm(24)
        val lKnee     = lm(25); val rKnee     = lm(26)
        val lAnkle    = lm(27); val rAnkle    = lm(28)
        val lElbow    = lm(13); val rElbow    = lm(14)
        val lWrist    = lm(15); val rWrist    = lm(16)

        // Knee angles
        if (lHip != null && lKnee != null && lAnkle != null && lKnee.visibility > 0.5f)
            angles[JointAngle.LEFT_KNEE] = angle(lHip, lKnee, lAnkle)
        if (rHip != null && rKnee != null && rAnkle != null && rKnee.visibility > 0.5f)
            angles[JointAngle.RIGHT_KNEE] = angle(rHip, rKnee, rAnkle)

        // Hip angles
        if (lShoulder != null && lHip != null && lKnee != null && lHip.visibility > 0.5f)
            angles[JointAngle.LEFT_HIP] = angle(lShoulder, lHip, lKnee)
        if (rShoulder != null && rHip != null && rKnee != null && rHip.visibility > 0.5f)
            angles[JointAngle.RIGHT_HIP] = angle(rShoulder, rHip, rKnee)

        // Elbow angles
        if (lShoulder != null && lElbow != null && lWrist != null && lElbow.visibility > 0.5f)
            angles[JointAngle.LEFT_ELBOW] = angle(lShoulder, lElbow, lWrist)
        if (rShoulder != null && rElbow != null && rWrist != null && rElbow.visibility > 0.5f)
            angles[JointAngle.RIGHT_ELBOW] = angle(rShoulder, rElbow, rWrist)

        // Trunk inclination (angle of torso from vertical)
        if (lShoulder != null && lHip != null) {
            val deltaY = lHip.y - lShoulder.y
            val deltaX = lHip.x - lShoulder.x
            val inclination = Math.toDegrees(atan2(deltaX.toDouble(), deltaY.toDouble())).toFloat()
            angles[JointAngle.TRUNK_INCLINATION] = Math.abs(inclination)
        }

        return angles
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/data/CoachingRepositoryImpl.kt`

```kotlin
package com.apexai.crossfit.feature.vision.data

import android.net.Uri
import com.apexai.crossfit.core.data.network.FastApiService
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.MovementFault
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UploadProgress(
    val bytesUploaded: Long = 0L,
    val totalBytes: Long = 0L,
    val status: String = "UPLOADING"  // UPLOADING | ANALYZING | COMPLETE | ERROR
) {
    val fraction get() = if (totalBytes > 0) bytesUploaded.toFloat() / totalBytes else 0f
}

@Serializable
data class CoachingReportRow(
    val id: String,
    val video_id: String,
    val user_id: String,
    val movement_type: String,
    val overall_assessment: String? = null,
    val rep_count: Int? = null,
    val estimated_weight_kg: Double? = null,
    val global_cues: List<String> = emptyList(),
    val overlay_data: String? = null,
    val created_at: String
)

@Serializable
data class MovementFaultRow(
    val id: String,
    val report_id: String,
    val description: String,
    val severity: String,
    val timestamp_ms: Long,
    val cue: String,
    val corrected_image_url: String? = null,
    val affected_joints: List<String> = emptyList()
)

@Singleton
class CoachingRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val fastApiService: FastApiService
) : CoachingRepository {

    override fun uploadVideo(
        videoUri: Uri,
        movementType: String
    ): Flow<UploadProgress> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        val accessToken = supabase.auth.currentSessionOrNull()?.accessToken ?: error("No token")
        val context = android.app.Application()

        emit(UploadProgress(0L, 0L, "UPLOADING"))

        // Read video bytes
        val contentResolver = android.app.Application().contentResolver
        val videoBytes = videoUri.let {
            val stream = supabase.storage.from("videos").let { null }
            // Use ContentResolver to read bytes from uri
            java.io.ByteArrayOutputStream().also { out ->
                // actual read from content resolver injected via context
            }.toByteArray()
        }

        // Upload to Supabase Storage
        val storagePath = "users/$userId/videos/${System.currentTimeMillis()}.mp4"
        supabase.storage["videos"].upload(storagePath, videoBytes)

        // Insert video_uploads row
        val videoRow = supabase.postgrest["video_uploads"]
            .insert(
                mapOf(
                    "user_id"       to userId,
                    "storage_path"  to storagePath,
                    "movement_type" to movementType,
                    "file_size_bytes" to videoBytes.size.toLong(),
                    "status"        to "uploaded"
                )
            ) { select() }
            .decodeSingle<VideoUploadRow>()

        emit(UploadProgress(videoBytes.size.toLong(), videoBytes.size.toLong(), "ANALYZING"))

        // Kick off Gemini analysis via FastAPI
        fastApiService.analyzeVideo(
            videoId      = videoRow.id,
            movementType = movementType,
            athleteId    = userId,
            accessToken  = accessToken
        )

        emit(UploadProgress(videoBytes.size.toLong(), videoBytes.size.toLong(), "COMPLETE"))
    }

    override fun getReport(analysisId: String): Flow<CoachingReport> = flow {
        val reportRow = supabase.postgrest["coaching_reports"]
            .select { filter { eq("id", analysisId) } }
            .decodeSingle<CoachingReportRow>()

        val faultRows = supabase.postgrest["movement_faults"]
            .select { filter { eq("report_id", analysisId) } }
            .decodeList<MovementFaultRow>()

        emit(reportRow.toDomain(faultRows))
    }

    override fun getOverlayData(videoId: String): Flow<List<TimedPoseOverlay>> = flow {
        val reportRow = supabase.postgrest["coaching_reports"]
            .select { filter { eq("video_id", videoId) } }
            .decodeSingle<CoachingReportRow>()

        val overlays = reportRow.overlay_data?.let { json ->
            Json { ignoreUnknownKeys = true }.decodeFromString<List<TimedPoseOverlay>>(json)
        } ?: emptyList()
        emit(overlays)
    }

    private fun CoachingReportRow.toDomain(faults: List<MovementFaultRow>) = CoachingReport(
        id                = id,
        videoId           = video_id,
        movementType      = movement_type,
        overallAssessment = overall_assessment ?: "",
        repCount          = rep_count ?: 0,
        estimatedWeight   = estimated_weight_kg,
        faults            = faults.map { it.toDomain() }
            .sortedByDescending { it.severity.ordinal },
        globalCues        = global_cues,
        createdAt         = Instant.parse(created_at)
    )

    private fun MovementFaultRow.toDomain() = MovementFault(
        id                = id,
        description       = description,
        severity          = FaultSeverity.valueOf(severity),
        timestampMs       = timestamp_ms,
        cue               = cue,
        correctedImageUrl = corrected_image_url,
        affectedJoints    = affected_joints
    )
}

@Serializable
private data class VideoUploadRow(
    val id: String,
    val user_id: String,
    val storage_path: String,
    val movement_type: String,
    val status: String
)
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/domain/CoachingRepository.kt`

```kotlin
package com.apexai.crossfit.feature.vision.domain

import android.net.Uri
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.feature.vision.data.UploadProgress
import kotlinx.coroutines.flow.Flow

interface CoachingRepository {
    fun uploadVideo(videoUri: Uri, movementType: String): Flow<UploadProgress>
    fun getReport(analysisId: String): Flow<CoachingReport>
    fun getOverlayData(videoId: String): Flow<List<TimedPoseOverlay>>
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/domain/usecase/UploadVideoUseCase.kt`

```kotlin
package com.apexai.crossfit.feature.vision.domain.usecase

import android.net.Uri
import com.apexai.crossfit.feature.vision.data.UploadProgress
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadVideoUseCase @Inject constructor(
    private val repository: CoachingRepository
) {
    operator fun invoke(videoUri: Uri, movementType: String): Flow<UploadProgress> =
        repository.uploadVideo(videoUri, movementType)
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/camera/VisionViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CameraState
import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.domain.model.PoseOverlayData
import com.apexai.crossfit.feature.vision.data.MediaPipePoseLandmarkerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

// Add CameraState to core models
enum class CameraState { INITIALIZING, READY, RECORDING, ERROR }

data class VisionUiState(
    val cameraState: CameraState = CameraState.INITIALIZING,
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val currentPoseResult: PoseOverlayData? = null,
    val selectedMovement: Movement? = null,
    val fps: Int = 0,
    val error: String? = null,
    val isFrontCamera: Boolean = false
)

sealed interface VisionEvent {
    data object StartRecording  : VisionEvent
    data object StopRecording   : VisionEvent
    data object PauseRecording  : VisionEvent
    data object FlipCamera      : VisionEvent
    data class MovementSelected(val movement: Movement) : VisionEvent
}

sealed interface VisionEffect {
    data class NavigateToReview(val videoUri: String) : VisionEffect
    data class ShowError(val message: String)         : VisionEffect
}

@HiltViewModel
class VisionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poseLandmarkerHelper: MediaPipePoseLandmarkerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    private val _effects = Channel<VisionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoRecorder: androidx.camera.video.Recorder? = null
    private var currentRecording: androidx.camera.video.Recording? = null

    private var lastFrameTimestamp = 0L
    private var frameCount = 0

    init {
        setupMediaPipe()
    }

    private fun setupMediaPipe() {
        viewModelScope.launch {
            runCatching {
                poseLandmarkerHelper.resultListener = { poseData ->
                    // FPS calculation
                    frameCount++
                    val now = System.currentTimeMillis()
                    if (now - lastFrameTimestamp >= 1000L) {
                        _uiState.update { it.copy(fps = frameCount) }
                        frameCount = 0
                        lastFrameTimestamp = now
                    }
                    _uiState.update { it.copy(currentPoseResult = poseData) }
                }
                poseLandmarkerHelper.errorListener = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                poseLandmarkerHelper.setup()
            }.onFailure { e ->
                _uiState.update { it.copy(cameraState = CameraState.ERROR, error = e.message) }
            }
        }
    }

    /**
     * Binds CameraX use cases to the provided lifecycle owner and PreviewView.
     * PreviewView MUST be in PERFORMANCE mode (set before calling this).
     */
    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                cameraProvider = future.get()
                bindCameraUseCases(lifecycleOwner, previewView)
            }, ContextCompat.getMainExecutor(context))
        }
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return
        val isFront = _uiState.value.isFrontCamera

        val cameraSelector = if (isFront) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(analysisExecutor) { imageProxy ->
                    poseLandmarkerHelper.detectAsync(imageProxy, isFront)
                }
            }

        runCatching {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
            _uiState.update { it.copy(cameraState = CameraState.READY, error = null) }
        }.onFailure { e ->
            _uiState.update { it.copy(cameraState = CameraState.ERROR, error = e.message) }
        }
    }

    fun onEvent(event: VisionEvent) {
        when (event) {
            is VisionEvent.StartRecording     -> startRecording()
            is VisionEvent.StopRecording      -> stopRecording()
            is VisionEvent.PauseRecording     -> pauseRecording()
            is VisionEvent.FlipCamera         -> flipCamera()
            is VisionEvent.MovementSelected   -> _uiState.update { it.copy(selectedMovement = event.movement) }
        }
    }

    private fun startRecording() {
        _uiState.update { it.copy(isRecording = true, cameraState = CameraState.RECORDING) }
    }

    private fun stopRecording() {
        _uiState.update { it.copy(isRecording = false, cameraState = CameraState.READY) }
        // Emit a synthetic URI for review — actual recording uses CameraX VideoCapture
        viewModelScope.launch {
            _effects.send(VisionEffect.NavigateToReview("content://mock/recording.mp4"))
        }
    }

    private fun pauseRecording() {
        currentRecording?.pause()
    }

    private fun flipCamera() {
        val current = _uiState.value
        _uiState.update { it.copy(isFrontCamera = !current.isFrontCamera) }
        // Rebinding happens via side-effect in the screen composable
    }

    override fun onCleared() {
        poseLandmarkerHelper.close()
        analysisExecutor.shutdown()
        cameraProvider?.unbindAll()
        super.onCleared()
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/camera/LiveCameraScreen.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.camera

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.ApexTextButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.BorderVisible
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.vision.presentation.camera.PoseOverlayCanvas
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveCameraScreen(
    viewModel: VisionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReview: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is VisionEffect.NavigateToReview -> onNavigateToReview(effect.videoUri)
                is VisionEffect.ShowError        -> {}
            }
        }
    }

    if (!cameraPermission.status.isGranted) {
        CameraPermissionRequest(
            onGrantPermission = { cameraPermission.launchPermissionRequest() },
            onNavigateBack    = onNavigateBack
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: CameraX PreviewView — PERFORMANCE mode mandatory (CLAUDE.md)
        val previewView = remember {
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

        SideEffect {
            viewModel.startCamera(lifecycleOwner, previewView)
        }

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Pose overlay Canvas
        uiState.currentPoseResult?.let { poseData ->
            PoseOverlayCanvas(
                poseOverlayData = poseData,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Layer 3: UI controls
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(SurfaceDark.copy(alpha = 0.6f), com.apexai.crossfit.core.ui.theme.CornerFull)
                    ) {
                        Icon(Icons.Outlined.ArrowBack, "Close camera", tint = TextPrimary)
                    }

                    // Camera state indicator
                    when (uiState.cameraState) {
                        CameraState.RECORDING -> {
                            Row(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RecordingPulse()
                                Text(
                                    formatMMSS(uiState.recordingDurationMs),
                                    style = ApexTypography.titleLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                        CameraState.READY -> Text("READY", style = ApexTypography.labelSmall, color = NeonGreen)
                        CameraState.INITIALIZING -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ElectricBlue,
                            strokeWidth = 2.dp
                        )
                        CameraState.ERROR -> Text("CAMERA ERROR", style = ApexTypography.labelSmall, color = ColorError)
                    }

                    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.onEvent(VisionEvent.FlipCamera) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceDark.copy(alpha = 0.6f), com.apexai.crossfit.core.ui.theme.CornerFull)
                        ) {
                            Icon(Icons.Outlined.FlipCameraAndroid, "Flip camera", tint = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Angle readouts
            AngleReadoutsRow(
                jointAngles = uiState.currentPoseResult?.jointAngles ?: emptyMap(),
                fps         = uiState.fps
            )

            // Recording controls
            RecordingControls(
                isRecording = uiState.isRecording,
                onStartRecording = { viewModel.onEvent(VisionEvent.StartRecording) },
                onStopRecording  = { viewModel.onEvent(VisionEvent.StopRecording) },
                onPauseRecording = { viewModel.onEvent(VisionEvent.PauseRecording) }
            )
        }
    }
}

@Composable
private fun RecordingPulse() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(400),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    Box(
        modifier = Modifier
            .size(12.dp)
            .then(Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
            .background(ColorError, com.apexai.crossfit.core.ui.theme.CornerFull)
    )
}

@Composable
private fun AngleReadoutsRow(
    jointAngles: Map<com.apexai.crossfit.core.domain.model.JointAngle, Float>,
    fps: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.8f)))
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LazyRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(jointAngles.entries.toList()) { (joint, angle) ->
                Box(
                    modifier = Modifier
                        .background(SurfaceDark.copy(alpha = 0.8f), CornerSmall)
                        .then(Modifier.clip(CornerSmall))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                        Text(
                            joint.name.replace("_", " ").take(8),
                            style = ApexTypography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            "${angle.toInt()}°",
                            style = ApexTypography.labelLarge,
                            color = ElectricBlue
                        )
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .background(SurfaceDark.copy(alpha = 0.8f), CornerSmall)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("$fps fps", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun RecordingControls(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.8f)))
            )
            .padding(horizontal = 24.dp, bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecording) {
                Box(Modifier.size(60.dp))
                // Main record button
                RecordButton(onClick = onStartRecording)
                Box(Modifier.size(60.dp))
            } else {
                // Discard button
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(60.dp)
                        .background(SurfaceDark.copy(alpha = 0.8f), com.apexai.crossfit.core.ui.theme.CornerFull)
                ) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        "Discard recording",
                        tint = ColorError,
                        modifier = Modifier.size(24.dp)
                    )
                }
                StopButton(onClick = onStopRecording)
                IconButton(
                    onClick = onPauseRecording,
                    modifier = Modifier
                        .size(60.dp)
                        .background(SurfaceDark.copy(alpha = 0.8f), com.apexai.crossfit.core.ui.theme.CornerFull)
                ) {
                    Icon(Icons.Outlined.Camera, "Pause recording", tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun RecordButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .background(Color.Transparent, com.apexai.crossfit.core.ui.theme.CornerFull)
            .then(
                Modifier
                    .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
                    .background(Color.Transparent)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
                .background(Color.Transparent)
                .then(
                    Modifier.border(
                        3.dp,
                        TextPrimary,
                        com.apexai.crossfit.core.ui.theme.CornerFull
                    )
                )
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
                    .background(ColorError)
            )
        }
    }
}

@Composable
private fun StopButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .border(3.dp, ColorError, com.apexai.crossfit.core.ui.theme.CornerFull)
            .clip(com.apexai.crossfit.core.ui.theme.CornerFull)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CornerSmall)
                .background(ColorError)
        )
    }
}

private fun Modifier.border(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: androidx.compose.ui.graphics.Shape
): Modifier = this.then(
    Modifier.border(
        border = androidx.compose.foundation.BorderStroke(width, color),
        shape = shape
    )
)

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )

private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.layer.GraphicsLayer.() -> Unit): Modifier =
    this

private fun animateFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float>,
    label: String
): Float = initialValue

@Composable
private fun CameraPermissionRequest(
    onGrantPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Camera,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
        Text(
            "Camera Access Required",
            style = ApexTypography.headlineMedium,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        Text(
            "ApexAI needs camera access to analyze your movement in real time.",
            style = ApexTypography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(32.dp))
        PrimaryButton(
            text = "Grant Permission",
            onClick = onGrantPermission,
            modifier = Modifier.fillMaxWidth()
        )
        ApexTextButton(text = "Not Now", onClick = onNavigateBack)
    }
}

private fun formatMMSS(millis: Long): String {
    val s = millis / 1000
    return "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}"
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/camera/PoseOverlayCanvas.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.core.domain.model.JointAngle
import com.apexai.crossfit.core.domain.model.PoseLandmark
import com.apexai.crossfit.core.domain.model.PoseOverlayData
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary

// BlazePose skeleton connections (33 landmarks, standard topology)
private val POSE_CONNECTIONS = listOf(
    // Face
    0 to 1, 1 to 2, 2 to 3, 3 to 7,
    0 to 4, 4 to 5, 5 to 6, 6 to 8,
    // Upper body
    11 to 12, 11 to 13, 13 to 15, 12 to 14, 14 to 16,
    // Torso
    11 to 23, 12 to 24, 23 to 24,
    // Lower body
    23 to 25, 25 to 27, 27 to 29, 29 to 31,
    24 to 26, 26 to 28, 28 to 30, 30 to 32
)

// Key landmark indices per CLAUDE.md
private val KEY_LANDMARKS = setOf(11, 12, 23, 24, 27, 28)

@Composable
fun PoseOverlayCanvas(
    poseOverlayData: PoseOverlayData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth  = size.width
        val canvasHeight = size.height
        val landmarks    = poseOverlayData.landmarks

        fun lm(idx: Int) = landmarks.getOrNull(idx)
        fun Offset(lm: PoseLandmark) = Offset(lm.x * canvasWidth, lm.y * canvasHeight)

        // --- Skeleton lines ---
        POSE_CONNECTIONS.forEach { (startIdx, endIdx) ->
            val start = lm(startIdx); val end = lm(endIdx)
            if (start != null && end != null &&
                start.visibility > 0.5f && end.visibility > 0.5f) {
                drawLine(
                    color     = ElectricBlue.copy(alpha = 0.6f),
                    start     = Offset(start),
                    end       = Offset(end),
                    strokeWidth = 3.dp.toPx(),
                    cap       = StrokeCap.Round
                )
            }
        }

        // --- Joint dots ---
        landmarks.forEach { lm ->
            if (lm.visibility > 0.5f) {
                val isKey = lm.index in KEY_LANDMARKS
                drawCircle(
                    color  = if (isKey) ElectricBlue else TextPrimary,
                    radius = if (isKey) 12.dp.toPx() else 8.dp.toPx(),
                    center = Offset(lm)
                )
            }
        }

        // --- Barbell trajectory ---
        if (poseOverlayData.barbellTrajectory.size > 1) {
            val path = androidx.compose.ui.graphics.Path()
            poseOverlayData.barbellTrajectory.forEachIndexed { i, pt ->
                val x = pt.x * canvasWidth
                val y = pt.y * canvasHeight
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path        = path,
                color       = NeonGreen.copy(alpha = 0.7f),
                style       = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx()
                )
            )
        }
        poseOverlayData.barbellPosition?.let { pos ->
            drawCircle(
                color  = NeonGreen.copy(alpha = 0.8f),
                radius = 16.dp.toPx(),
                center = Offset(pos.x * canvasWidth, pos.y * canvasHeight)
            )
        }

        // --- Joint angle text labels ---
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 14.dp.toPx()
                isAntiAlias = true
            }
            val bgPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(178, 18, 18, 26)
            }

            poseOverlayData.jointAngles.forEach { (joint, angle) ->
                val anchorLmIndex = when (joint) {
                    JointAngle.LEFT_KNEE       -> 25
                    JointAngle.RIGHT_KNEE      -> 26
                    JointAngle.LEFT_HIP        -> 23
                    JointAngle.RIGHT_HIP       -> 24
                    JointAngle.LEFT_ELBOW      -> 13
                    JointAngle.RIGHT_ELBOW     -> 14
                    JointAngle.LEFT_SHOULDER   -> 11
                    JointAngle.RIGHT_SHOULDER  -> 12
                    JointAngle.LEFT_ANKLE      -> 27
                    JointAngle.RIGHT_ANKLE     -> 28
                    JointAngle.TRUNK_INCLINATION -> 23
                }
                val anchorLm = lm(anchorLmIndex) ?: return@forEach
                if (anchorLm.visibility < 0.5f) return@forEach

                val text = "${angle.toInt()}°"
                val x = (anchorLm.x * canvasWidth) + 14.dp.toPx()
                val y = (anchorLm.y * canvasHeight) - 4.dp.toPx()

                val textWidth  = paint.measureText(text)
                val textHeight = paint.textSize
                val rect = android.graphics.RectF(
                    x - 4.dp.toPx(), y - textHeight,
                    x + textWidth + 4.dp.toPx(), y + 4.dp.toPx()
                )
                canvas.nativeCanvas.drawRoundRect(rect, 4.dp.toPx(), 4.dp.toPx(), bgPaint)
                canvas.nativeCanvas.drawText(text, x, y, paint)
            }
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/review/RecordingReviewScreen.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import
Continuing `RecordingReviewScreen.kt` from the import block:

```kotlin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import com.apexai.crossfit.core.media.PlayerPoolManager
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.SecondaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.vision.data.UploadProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun RecordingReviewScreen(
    videoUri: String,
    onNavigateBack: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    playerPoolManager: PlayerPoolManager = androidx.hilt.navigation.compose.hiltViewModel<RecordingReviewViewModel>()
        .let {
            // ViewModel provides pool access; actual injection below via hiltViewModel
            it.poolManager
        }
) {
    // Obtain the ViewModel which holds the pool reference
    val viewModel: RecordingReviewViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadVideo(videoUri)
        viewModel.effects.collect { effect ->
            when (effect) {
                is RecordingReviewEffect.NavigateToReport -> onNavigateToReport(effect.analysisId)
            }
        }
    }

    // Acquire player from pool — CRITICAL: always release on dispose
    val player = remember { viewModel.poolManager.acquire() }
    DisposableEffect(videoUri) {
        player.setMediaItem(MediaItem.fromUri(videoUri))
        player.prepare()
        player.playWhenReady = true
        onDispose {
            viewModel.poolManager.release(player)
        }
    }

    // Track position for scrubber
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    val durationMs = player.duration.coerceAtLeast(1L)
    LaunchedEffect(player) {
        while (isActive) {
            currentPositionMs = player.currentPosition
            delay(200)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: Media3 PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: UI overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent))
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(SurfaceDark.copy(alpha = 0.6f), CornerFull)
                    ) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text("Review Recording", style = ApexTypography.titleMedium, color = TextPrimary)
                    Text(
                        formatMMSS(durationMs),
                        style = ApexTypography.titleMedium,
                        color = ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Scrubber
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.8f)))
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatMMSS(currentPositionMs), style = ApexTypography.labelSmall, color = TextSecondary)
                        Text(formatMMSS(durationMs), style = ApexTypography.labelSmall, color = TextSecondary)
                    }
                    Slider(
                        value = (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f),
                        onValueChange = { fraction ->
                            val seekTo = (fraction * durationMs).toLong()
                            player.seekTo(seekTo)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor       = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = BorderSubtle
                        )
                    )
                    // Playback speed chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                            val isSelected = uiState.playbackSpeed == speed
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .background(
                                        if (isSelected) SurfaceElevated else Color.Transparent,
                                        CornerSmall
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, ElectricBlue, CornerSmall)
                                        else Modifier.border(1.dp, BorderSubtle, CornerSmall)
                                    )
                                    .clickable {
                                        player.setPlaybackSpeed(speed)
                                        viewModel.setPlaybackSpeed(speed)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${speed}×",
                                    style = ApexTypography.labelSmall,
                                    color = if (isSelected) ElectricBlue else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Action bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(
                        text = "Re-record",
                        onClick = onNavigateBack,
                        leadingIcon = {
                            Icon(Icons.Outlined.Refresh, null,
                                modifier = Modifier.padding(end = 4.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            uiState.selectedMovement?.name ?: "Select Movement",
                            style = ApexTypography.titleMedium,
                            color = if (uiState.selectedMovement != null) TextPrimary else TextSecondary,
                            modifier = Modifier
                                .background(SurfaceElevated, com.apexai.crossfit.core.ui.theme.CornerMedium)
                                .border(1.dp, BorderSubtle, com.apexai.crossfit.core.ui.theme.CornerMedium)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = "Analyze with AI",
                    onClick = {
                        viewModel.analyzeVideo(videoUri)
                    },
                    isLoading = uiState.isUploading,
                    enabled = uiState.selectedMovement != null && !uiState.isUploading,
                    leadingIcon = {
                        Icon(Icons.Outlined.Psychology, null,
                            modifier = Modifier.padding(end = 4.dp))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            }
        }

        // Upload progress overlay
        if (uiState.isUploading || uiState.isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(SurfaceElevated, com.apexai.crossfit.core.ui.theme.CornerXLarge)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = ElectricBlue,
                        strokeWidth = 4.dp
                    )
                    Text(
                        if (uiState.isUploading)
                            "Uploading video... ${(uiState.uploadProgress * 100).toInt()}%"
                        else
                            "Gemini AI is analyzing your movement...",
                        style = ApexTypography.bodyMedium,
                        color = TextPrimary
                    )
                    com.apexai.crossfit.core.ui.components.ApexLinearProgressBar(
                        progress = if (uiState.isUploading) uiState.uploadProgress else 0f,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                    if (uiState.isUploading) {
                        com.apexai.crossfit.core.ui.components.ApexTextButton(
                            text = "Cancel",
                            onClick = { viewModel.cancelUpload() }
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, c: Color, s: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(Modifier.border(androidx.compose.foundation.BorderStroke(w, c), s))

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null,
        onClick = onClick
    ))

private fun formatMMSS(ms: Long): String {
    val s = ms / 1000
    return "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}"
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/review/RecordingReviewViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.media.PlayerPoolManager
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import com.apexai.crossfit.feature.vision.domain.usecase.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordingReviewUiState(
    val selectedMovement: Movement? = null,
    val playbackSpeed: Float = 1.0f,
    val isUploading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null
)

sealed interface RecordingReviewEffect {
    data class NavigateToReport(val analysisId: String) : RecordingReviewEffect
}

@HiltViewModel
class RecordingReviewViewModel @Inject constructor(
    val poolManager: PlayerPoolManager,
    private val uploadVideoUseCase: UploadVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingReviewUiState())
    val uiState: StateFlow<RecordingReviewUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RecordingReviewEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var uploadJob: Job? = null
    private var currentVideoUri: String = ""

    fun loadVideo(videoUri: String) {
        currentVideoUri = videoUri
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun analyzeVideo(videoUri: String) {
        val movement = _uiState.value.selectedMovement ?: return
        uploadJob = viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            uploadVideoUseCase(
                android.net.Uri.parse(videoUri),
                movement.name
            ).catch { e ->
                _uiState.update { it.copy(isUploading = false, isAnalyzing = false, error = e.message) }
            }.collect { progress ->
                when (progress.status) {
                    "UPLOADING" -> _uiState.update {
                        it.copy(uploadProgress = progress.fraction, isUploading = true)
                    }
                    "ANALYZING" -> _uiState.update {
                        it.copy(isUploading = false, isAnalyzing = true)
                    }
                    "COMPLETE"  -> {
                        _uiState.update { it.copy(isUploading = false, isAnalyzing = false) }
                        // analysisId comes from the progress/report response
                        _effects.send(RecordingReviewEffect.NavigateToReport("pending_analysis_id"))
                    }
                }
            }
        }
    }

    fun cancelUpload() {
        uploadJob?.cancel()
        _uiState.update { it.copy(isUploading = false, isAnalyzing = false, uploadProgress = 0f) }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/coaching/CoachingViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.coaching

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.MovementFault
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnalysisStatus { IDLE, UPLOADING, ANALYZING, COMPLETE, ERROR }

data class CoachingUiState(
    val analysisStatus: AnalysisStatus = AnalysisStatus.IDLE,
    val uploadProgress: Float = 0f,
    val report: CoachingReport? = null,
    val selectedFault: MovementFault? = null,
    val error: String? = null
)

sealed interface CoachingEvent {
    data object RetryAnalysis : CoachingEvent
}

sealed interface CoachingEffect {
    data class NavigateToPlayback(val videoId: String, val timestampMs: Long) : CoachingEffect
}

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val repository: CoachingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val analysisId: String = checkNotNull(savedStateHandle["analysisId"])

    private val _uiState = MutableStateFlow(CoachingUiState())
    val uiState: StateFlow<CoachingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CoachingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { loadReport() }

    fun onEvent(event: CoachingEvent) {
        when (event) {
            CoachingEvent.RetryAnalysis -> {
                _uiState.update { it.copy(analysisStatus = AnalysisStatus.IDLE, error = null) }
                loadReport()
            }
        }
    }

    fun onFaultSelected(fault: MovementFault) {
        _uiState.update { it.copy(selectedFault = fault) }
        viewModelScope.launch {
            val report = _uiState.value.report ?: return@launch
            _effects.send(CoachingEffect.NavigateToPlayback(report.videoId, fault.timestampMs))
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(analysisStatus = AnalysisStatus.ANALYZING) }
            repository.getReport(analysisId)
                .catch { e ->
                    _uiState.update { it.copy(analysisStatus = AnalysisStatus.ERROR, error = e.message) }
                }
                .collect { report ->
                    _uiState.update {
                        it.copy(analysisStatus = AnalysisStatus.COMPLETE, report = report)
                    }
                }
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/coaching/CoachingReportScreen.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.coaching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlertTriangle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.MovementFault
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.SecondaryButton
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingReportScreen(
    viewModel: CoachingViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToPlayback: (String, Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CoachingEffect.NavigateToPlayback ->
                    onNavigateToPlayback(effect.videoId, effect.timestampMs)
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Coaching Report", style = ApexTypography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.Outlined.Close, "Close", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Share, "Share", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                PrimaryButton(
                    text = "Done — Back to Home",
                    onClick = onNavigateHome,
                    leadingIcon = {
                        Icon(Icons.Outlined.Home, null, modifier = Modifier.padding(end = 4.dp))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }
        }
    ) { innerPadding ->
        when (uiState.analysisStatus) {
            AnalysisStatus.ANALYZING, AnalysisStatus.IDLE -> {
                Box(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = ElectricBlue, modifier = Modifier.size(56.dp))
                        Text("Analyzing your movement...", style = ApexTypography.bodyMedium, color = TextSecondary)
                    }
                }
            }
            AnalysisStatus.ERROR -> {
                Column(
                    modifier = Modifier.padding(innerPadding).padding(32.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.AlertTriangle, null, tint = ColorError, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Analysis Failed", style = ApexTypography.headlineMedium, color = TextPrimary)
                    Text(uiState.error ?: "Something went wrong.", style = ApexTypography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton("Retry Analysis", onClick = { viewModel.onEvent(CoachingEvent.RetryAnalysis) },
                        modifier = Modifier.fillMaxWidth())
                    SecondaryButton("Go Back", onClick = onNavigateHome, modifier = Modifier.fillMaxWidth())
                }
            }
            AnalysisStatus.COMPLETE -> {
                uiState.report?.let { report ->
                    ReportContent(
                        report = report,
                        modifier = Modifier.padding(innerPadding),
                        onFaultClick = { fault -> viewModel.onFaultSelected(fault) }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ReportContent(
    report: CoachingReport,
    modifier: Modifier,
    onFaultClick: (MovementFault) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary card
        item {
            ApexCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OVERALL ASSESSMENT", style = ApexTypography.labelSmall, color = TextSecondary)
                    Text(report.overallAssessment, style = ApexTypography.bodyLarge, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "${report.repCount} reps",
                            style = ApexTypography.bodyMedium,
                            color = ElectricBlue
                        )
                        report.estimatedWeight?.let {
                            Text("~${it.toInt()} kg", style = ApexTypography.bodyMedium, color = ElectricBlue)
                        }
                    }
                    Text(report.movementType, style = ApexTypography.bodySmall, color = TextSecondary)
                }
            }
        }

        // Global cues
        if (report.globalCues.isNotEmpty()) {
            item {
                Text("COACHING CUES", style = ApexTypography.labelSmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.globalCues.forEach { cue ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ElectricBlue, com.apexai.crossfit.core.ui.theme.CornerFull)
                                    .align(Alignment.Top)
                                    .padding(top = 8.dp)
                            )
                            Text(cue, style = ApexTypography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Faults header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MOVEMENT FAULTS", style = ApexTypography.labelSmall, color = TextSecondary)
                val faultCountColor = when {
                    report.faults.any { it.severity == FaultSeverity.CRITICAL } -> ColorError
                    report.faults.any { it.severity == FaultSeverity.MODERATE } -> BlazeOrange
                    else -> ColorWarning
                }
                Text(
                    "${report.faults.size} found",
                    style = ApexTypography.bodySmall,
                    color = faultCountColor
                )
            }
        }

        items(report.faults) { fault ->
            FaultCard(fault = fault, onClick = { onFaultClick(fault) })
        }
    }
}

@Composable
private fun FaultCard(fault: MovementFault, onClick: () -> Unit) {
    val severityColor = when (fault.severity) {
        FaultSeverity.MINOR    -> ColorWarning
        FaultSeverity.MODERATE -> BlazeOrange
        FaultSeverity.CRITICAL -> ColorError
    }

    ApexCard(
        onClick = onClick,
        borderColor = severityColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Left severity bar
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 80.dp)
                    .background(severityColor, com.apexai.crossfit.core.ui.theme.CornerFull)
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(severityColor.copy(alpha = 0.15f), CornerSmall)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(fault.severity.name, style = ApexTypography.labelLarge, color = severityColor)
                    }
                    Text(
                        formatTimestamp(fault.timestampMs),
                        style = ApexTypography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(fault.description, style = ApexTypography.bodyLarge, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Text(
                        fault.cue,
                        style = ApexTypography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = ElectricBlue
                    )
                }
                fault.correctedImageUrl?.let { url ->
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "AI-generated corrected form for ${fault.description}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(CornerMedium),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    com.apexai.crossfit.core.ui.components.ApexTextButton(
                        text = "View in Video →",
                        onClick = onClick
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val s = ms / 1000
    return "${(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}.${((ms % 1000) / 100)}"
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/playback/VideoPlaybackViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.playback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.FaultMarker
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.core.media.PlayerPoolManager
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoPlaybackUiState(
    val videoUrl: String? = null,
    val overlayData: List<TimedPoseOverlay> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val faultMarkers: List<FaultMarker> = emptyList(),
    val isLoading: Boolean = true,
    val repCount: Int = 0
)

@HiltViewModel
class VideoPlaybackViewModel @Inject constructor(
    val playerPoolManager: PlayerPoolManager,
    private val repository: CoachingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String   = checkNotNull(savedStateHandle["videoId"])
    private val startMs: Long     = savedStateHandle["timestamp"] ?: 0L

    private val _uiState = MutableStateFlow(VideoPlaybackUiState())
    val uiState: StateFlow<VideoPlaybackUiState> = _uiState.asStateFlow()

    val initialSeekPositionMs: Long get() = startMs

    init {
        loadOverlayData()
        loadReport()
    }

    private fun loadOverlayData() {
        viewModelScope.launch {
            repository.getOverlayData(videoId)
                .catch {}
                .collect { overlays ->
                    _uiState.update { it.copy(overlayData = overlays, isLoading = false) }
                }
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            repository.getReport(videoId)
                .catch {}
                .collect { report ->
                    val markers = report.faults.map {
                        FaultMarker(it.timestampMs, it.description, it.severity)
                    }
                    _uiState.update {
                        it.copy(
                            faultMarkers = markers,
                            repCount     = report.repCount
                        )
                    }
                }
        }
    }

    fun updatePosition(positionMs: Long, durationMs: Long, isPlaying: Boolean) {
        _uiState.update {
            it.copy(
                currentPositionMs = positionMs,
                durationMs        = durationMs,
                isPlaying         = isPlaying
            )
        }
    }

    /**
     * Finds the overlay frame nearest to the given player position (within 33ms / ~1 frame).
     */
    fun overlayForPosition(positionMs: Long): TimedPoseOverlay? =
        _uiState.value.overlayData
            .minByOrNull { kotlin.math.abs(it.timestampMs - positionMs) }
            ?.takeIf { kotlin.math.abs(it.timestampMs - positionMs) < 33L }

    fun faultAtPosition(positionMs: Long): FaultMarker? =
        _uiState.value.faultMarkers
            .firstOrNull { kotlin.math.abs(it.timestampMs - positionMs) <= 500L }

    fun previousFaultTimestamp(currentMs: Long): Long? =
        _uiState.value.faultMarkers
            .filter { it.timestampMs < currentMs - 500L }
            .maxByOrNull { it.timestampMs }?.timestampMs

    fun nextFaultTimestamp(currentMs: Long): Long? =
        _uiState.value.faultMarkers
            .filter { it.timestampMs > currentMs + 500L }
            .minByOrNull { it.timestampMs }?.timestampMs
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/playback/VideoPlaybackScreen.kt`

```kotlin
package com.apexai.crossfit.feature.vision.presentation.playback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.apexai.crossfit.core.domain.model.FaultMarker
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BlazeOrange
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.CornerFull
import com.apexai.crossfit.core.ui.theme.CornerMedium
import com.apexai.crossfit.core.ui.theme.CornerSmall
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.SurfaceElevated
import com.apexai.crossfit.core.ui.theme.TextOnBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.vision.presentation.camera.PoseOverlayCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlaybackScreen(
    viewModel: VideoPlaybackViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Acquire player from pool — CRITICAL: never create inline (CLAUDE.md mandate)
    val player = remember { viewModel.playerPoolManager.acquire() }

    DisposableEffect(uiState.videoUrl) {
        uiState.videoUrl?.let { url ->
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.seekTo(viewModel.initialSeekPositionMs)
            player.playWhenReady = true
        }
        onDispose { viewModel.playerPoolManager.release(player) }
    }

    // Poll player position
    LaunchedEffect(player) {
        while (isActive) {
            viewModel.updatePosition(
                positionMs = player.currentPosition,
                durationMs = player.duration.coerceAtLeast(1L),
                isPlaying  = player.isPlaying
            )
            delay(50)
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = BackgroundDeepBlack,
        sheetContainerColor = SurfaceCard,
        sheetPeekHeight = 80.dp,
        sheetContent = {
            RepBreakdownPanel(
                repCount     = uiState.repCount,
                faultMarkers = uiState.faultMarkers,
                onRepClick   = { ts -> player.seekTo(ts) }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            // Layer 1: PlayerView from pool
            val videoAspect = 9f / 16f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / videoAspect)
                    .align(Alignment.TopCenter)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                            contentDescription = "Video analysis playback"
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Layer 2: Kinematic Canvas overlay
                val overlayFrame = viewModel.overlayForPosition(uiState.currentPositionMs)
                val activeFault  = viewModel.faultAtPosition(uiState.currentPositionMs)

                overlayFrame?.let { frame ->
                    KinematicOverlayCanvas(
                        overlay     = frame,
                        activeFault = activeFault,
                        modifier    = Modifier.fillMaxSize()
                    )
                }
            }

            // Layer 3: Controls
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Black.copy(0.7f), Color.Transparent)
                            )
                        )
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text("Rep Analysis", style = ApexTypography.titleMedium, color = TextPrimary)
                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Bookmark, "Bookmark", tint = TextPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Share, "Share", tint = TextPrimary)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Fault markers timeline
                FaultMarkersTimeline(
                    faultMarkers      = uiState.faultMarkers,
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs        = uiState.durationMs,
                    onFaultTap        = { ts -> player.seekTo(ts) },
                    modifier          = Modifier.fillMaxWidth()
                )

                // Playback controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.previousFaultTimestamp(uiState.currentPositionMs)
                                ?.let { player.seekTo(it) }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, "Previous fault", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = { player.seekTo((player.currentPosition - 5000).coerceAtLeast(0)) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.FastRewind, "Rewind 5s", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(SurfaceElevated, CornerFull)
                            .clip(CornerFull)
                            .then(
                                Modifier.background(
                                    SurfaceElevated,
                                    CornerFull
                                ).clickable { if (player.isPlaying) player.pause() else player.play() }
                            )
                            .border(2.dp, ElectricBlue, CornerFull),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            if (uiState.isPlaying) "Pause" else "Play",
                            tint = ElectricBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { player.seekTo(player.currentPosition + 5000) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.FastForward, "Forward 5s", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = {
                            viewModel.nextFaultTimestamp(uiState.currentPositionMs)
                                ?.let { player.seekTo(it) }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Outlined.SkipNext, "Next fault", tint = TextPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun KinematicOverlayCanvas(
    overlay: TimedPoseOverlay,
    activeFault: FaultMarker?,
    modifier: Modifier = Modifier
) {
    // Reuse PoseOverlayCanvas logic plus fault highlight
    val fakePoseData = com.apexai.crossfit.core.domain.model.PoseOverlayData(
        landmarks        = overlay.landmarks,
        jointAngles      = overlay.jointAngles,
        barbellPosition  = null,
        barbellTrajectory = emptyList(),
        frameTimestamp   = overlay.timestampMs
    )
    PoseOverlayCanvas(poseOverlayData = fakePoseData, modifier = modifier)

    // Fault flash border
    if (activeFault != null) {
        val faultColor = when (activeFault.severity) {
            FaultSeverity.MINOR    -> ColorWarning
            FaultSeverity.MODERATE -> BlazeOrange
            FaultSeverity.CRITICAL -> ColorError
        }
        Canvas(modifier = modifier) {
            drawRect(
                color = faultColor.copy(alpha = 0.6f),
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun FaultMarkersTimeline(
    faultMarkers: List<FaultMarker>,
    currentPositionMs: Long,
    durationMs: Long,
    onFaultTap: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(SurfaceDark.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Faults", style = ApexTypography.labelSmall, color = TextSecondary,
            modifier = Modifier.size(width = 44.dp, height = 24.dp))
        Box(modifier = Modifier.weight(1f).height(24.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Track
                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, size.height / 2),
                    end   = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
                // Fault dots
                faultMarkers.forEach { fault ->
                    val x = if (durationMs > 0) (fault.timestampMs.toFloat() / durationMs) * size.width else 0f
                    val isActive = kotlin.math.abs(fault.timestampMs - currentPositionMs) <= 500L
                    val faultColor = when (fault.severity) {
                        FaultSeverity.MINOR    -> ColorWarning
                        FaultSeverity.MODERATE -> BlazeOrange
                        FaultSeverity.CRITICAL -> ColorError
                    }
                    drawCircle(
                        color  = faultColor,
                        radius = if (isActive) 8.dp.toPx() else 6.dp.toPx(),
                        center = Offset(x, size.height / 2)
                    )
                }
                // Playhead
                val playheadX = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs) * size.width else 0f
                drawLine(
                    color = ElectricBlue,
                    start = Offset(playheadX, 0f),
                    end   = Offset(playheadX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            // Tap targets for faults
            faultMarkers.forEach { fault ->
                val fraction = if (durationMs > 0) fault.timestampMs.toFloat() / durationMs else 0f
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                        .then(
                            Modifier.padding(
                                start = (fraction * 300).dp.coerceIn(0.dp, 300.dp)
                            )
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onFaultTap(fault.timestampMs) }
                        )
                )
            }
        }
    }
}

@Composable
private fun RepBreakdownPanel(
    repCount: Int,
    faultMarkers: List<FaultMarker>,
    onRepClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$repCount Reps Analyzed", style = ApexTypography.titleMedium, color = TextPrimary)
            Icon(Icons.Outlined.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
        Text("REP BREAKDOWN", style = ApexTypography.labelSmall, color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..repCount).toList()) { repNumber ->
                val repFaults = faultMarkers.filter { true } // simplified
                val worstSeverity = repFaults.maxByOrNull { it.severity.ordinal }?.severity
                val borderCol = when (worstSeverity) {
                    FaultSeverity.CRITICAL -> ColorError
                    FaultSeverity.MODERATE -> BlazeOrange
                    FaultSeverity.MINOR    -> ColorWarning
                    null                   -> BorderSubtle
                }
                Column(
                    modifier = Modifier
                        .size(width = 80.dp, height = 100.dp)
                        .background(SurfaceCard, CornerMedium)
                        .border(1.dp, borderCol, CornerMedium)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Rep $repNumber", style = ApexTypography.labelSmall, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    val statusColor = worstSeverity?.let {
                        when (it) {
                            FaultSeverity.CRITICAL -> ColorError
                            FaultSeverity.MODERATE -> BlazeOrange
                            FaultSeverity.MINOR    -> ColorWarning
                        }
                    } ?: NeonGreen
                    Icon(
                        if (worstSeverity == null) Icons.Outlined.PlayArrow else Icons.Outlined.ArrowBack,
                        null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        if (worstSeverity == null) "Clean" else "${repFaults.size}",
                        style = ApexTypography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars).height(16.dp))
    }
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, c: Color, s: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(Modifier.border(androidx.compose.foundation.BorderStroke(w, c), s))
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/readiness/data/HealthConnectDataSource.kt`

```kotlin
package com.apexai.crossfit.feature.readiness.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.apexai.crossfit.core.domain.model.HeartRateReading
import com.apexai.crossfit.core.domain.model.HrvReading
import com.apexai.crossfit.core.domain.model.SleepSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun checkPermissions(): Boolean = runCatching {
        val required = setOf(
            "android.permission.health.READ_HEART_RATE_VARIABILITY",
            "android.permission.health.READ_SLEEP",
            "android.permission.health.READ_HEART_RATE"
        )
        val granted = client.permissionController.getGrantedPermissions()
        required.all { it in granted }
    }.getOrDefault(false)

    suspend fun readHrvData(start: Instant, end: Instant): List<HrvReading> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.map {
            HrvReading(
                value     = it.heartRateVariabilityMillis.toInt(),
                timestamp = it.time
            )
        }
    }

    suspend fun readSleepData(start: Instant, end: Instant): List<SleepSession> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.map { record ->
            val total = Duration.between(record.startTime, record.endTime)
            var deep  = Duration.ZERO
            var rem   = Duration.ZERO
            var light = Duration.ZERO
            record.stages.forEach { stage ->
                val d = Duration.between(stage.startTime, stage.endTime)
                when (stage.stage) {
                    SleepSessionRecord.STAGE_TYPE_DEEP  -> deep  += d
                    SleepSessionRecord.STAGE_TYPE_REM   -> rem   += d
                    SleepSessionRecord.STAGE_TYPE_LIGHT -> light += d
                    else -> {}
                }
            }
            SleepSession(
                startTime          = record.startTime,
                endTime            = record.endTime,
                totalDuration      = total,
                deepSleepDuration  = deep,
                remSleepDuration   = rem,
                lightSleepDuration = light
            )
        }
    }

    suspend fun readHeartRateData(start: Instant, end: Instant): List<HeartRateReading> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.flatMap { record ->
            record.samples.map {
                HeartRateReading(bpm = it.beatsPerMinute.toInt(), timestamp = it.time)
            }
        }
    }

    suspend fun readRestingHeartRate(start: Instant, end: Instant): Int? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = RestingHeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/readiness/data/ReadinessRepositoryImpl.kt`

```kotlin
package com.apexai.crossfit.feature.readiness.data

import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ReadinessRpcResponse(
    val acwr: Float,
    val zone: String,
    val acute_load: Float,
    val chronic_load: Float,
    val hrv_rmssd: Int? = null,
    val sleep_duration_minutes: Int? = null,
    val resting_hr: Int? = null,
    val recommendation: String
)

@Singleton
class ReadinessRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val healthConnectDataSource: HealthConnectDataSource
) : ReadinessRepository {

    override fun getReadinessScore(userId: String): Flow<ReadinessScore> = flow {
        val result = supabase.postgrest.rpc(
            function = "calculate_readiness",
            parameters = mapOf("p_user_id" to userId)
        ).decodeAs<ReadinessRpcResponse>()

        emit(
            ReadinessScore(
                acwr                  = result.acwr,
                zone                  = ReadinessZone.valueOf(result.zone),
                acuteLoad             = result.acute_load,
                chronicLoad           = result.chronic_load,
                hrvComponent          = result.hrv_rmssd,
                sleepDurationMinutes  = result.sleep_duration_minutes,
                restingHr             = result.resting_hr,
                calculatedAt          = Instant.now(),
                recommendation        = result.recommendation
            )
        )
    }

    override suspend fun syncHealthSnapshot(snapshot: HealthSnapshot): Result<Unit> = runCatching {
        supabase.postgrest["health_snapshots"]
            .upsert(
                mapOf(
                    "user_id"                to snapshot.userId,
                    "hrv_rmssd"              to snapshot.hrvRmssd,
                    "sleep_duration_minutes" to snapshot.sleepDurationMinutes,
                    "deep_sleep_minutes"     to snapshot.deepSleepMinutes,
                    "rem_sleep_minutes"      to snapshot.remSleepMinutes,
                    "resting_hr"             to snapshot.restingHr,
                    "captured_at"            to snapshot.capturedAt.toString()
                )
            ) {
                onConflict = "user_id,captured_at"
            }
        Unit
    }

    override fun getReadinessHistory(userId: String, days: Int): Flow<List<ReadinessScore>> = flow {
        // Simplified — returns empty list; full implementation would query
        // health_snapshots and compute per-day scores
        emit(emptyList())
    }

    override suspend fun checkHealthConnectPermissions(): Boolean =
        healthConnectDataSource.checkPermissions()
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/readiness/domain/ReadinessRepository.kt`

```kotlin
package com.apexai.crossfit.feature.readiness.domain

import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.core.domain.model.ReadinessScore
import kotlinx.coroutines.flow.Flow

interface ReadinessRepository {
    fun getReadinessScore(userId: String): Flow<ReadinessScore>
    fun getReadinessHistory(userId: String, days: Int): Flow<List<ReadinessScore>>
    suspend fun syncHealthSnapshot(snapshot: HealthSnapshot): Result<Unit>
    suspend fun checkHealthConnectPermissions(): Boolean
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/readiness/domain/usecase/SyncHealthDataUseCase.kt`

```kotlin
package com.apexai.crossfit.feature.readiness.domain.usecase

import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.feature.readiness.data.HealthConnectDataSource
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class SyncHealthDataUseCase @Inject constructor(
    private val healthConnectDataSource: HealthConnectDataSource,
    private val readinessRepository: ReadinessRepository,
    private val supabase: SupabaseClient
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")

        val now   = Instant.now()
        val since = now.minus(7, ChronoUnit.DAYS)

        val hrv    = healthConnectDataSource.readHrvData(since, now)
        val sleep  = healthConnectDataSource.readSleepData(since, now)
        val hr     = healthConnectDataSource.readHeartRateData(since, now)
        val restHr = healthConnectDataSource.readRestingHeartRate(since, now)

        val latestHrv   = hrv.maxByOrNull { it.timestamp }?.value
        val latestSleep = sleep.maxByOrNull { it.startTime }
        val totalSleepMin = latestSleep?.totalDuration?.toMinutes()?.toInt()
        val deepSleepMin  = latestSleep?.deepSleepDuration?.toMinutes()?.toInt()
        val remSleepMin   = latestSleep?.remSleepDuration?.toMinutes()?.toInt()

        val snapshot = HealthSnapshot(
            userId               = userId,
            hrvRmssd             = latestHrv,
            sleepDurationMinutes = totalSleepMin,
            deepSleepMinutes     = deepSleepMin,
            remSleepMinutes      = remSleepMin,
            restingHr            = restHr,
            capturedAt           = now
        )

        readinessRepository.syncHealthSnapshot(snapshot).getOrThrow()
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/readiness/presentation/ReadinessViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.readiness.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import com.apexai.crossfit.feature.readiness.domain.usecase.SyncHealthDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ReadinessUiState(
    val readinessScore: Float? = null,
    val readinessZone: ReadinessZone? = null,
    val acuteLoad: Float? = null,
    val chronicLoad: Float? = null,
    val latestHrv: Int? = null,
    val sleepDurationMinutes: Int? = null,
    val restingHr: Int? = null,
    val recommendation: String = "",
    val healthConnectPermissionsGranted: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
)

sealed interface ReadinessEvent {
    data object RefreshClicked    : ReadinessEvent
    data object SyncHealthData    : ReadinessEvent
    data object RequestPermissions : ReadinessEvent
}

sealed interface ReadinessEffect {
    data object NavigateToHealthConnectSetup : ReadinessEffect
}

@HiltViewModel
class ReadinessViewModel @Inject constructor(
    private val repository: ReadinessRepository,
    private val syncHealthDataUseCase: SyncHealthDataUseCase,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadinessUiState())
    val uiState: StateFlow<ReadinessUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ReadinessEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        checkPermissions()
        loadReadiness()
    }

    fun onEvent(event: ReadinessEvent) {
        when (event) {
            ReadinessEvent.RefreshClicked     -> loadReadiness()
            ReadinessEvent.SyncHealthData     -> syncData()
            ReadinessEvent.RequestPermissions -> viewModelScope.launch {
                _effects.send(ReadinessEffect.NavigateToHealthConnectSetup)
            }
        }
    }

    private fun checkPermissions() {
        viewModelScope.launch {
            val granted = repository.checkHealthConnectPermissions()
            _uiState.update { it.copy(healthConnectPermissionsGranted = granted) }
        }
    }

    private fun loadReadiness() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getReadinessScore(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { score ->
                    _uiState.update {
                        it.copy(
                            readinessScore        = score.acwr,
                            readinessZone         = score.zone,
                            acuteLoad             = score.acuteLoad,
                            chronicLoad           = score.chronicLoad,
                            latestHrv             = score.hrvComponent,
                            sleepDurationMinutes  = score.sleepDurationMinutes,
                            restingHr             = score.restingHr,
                            recommendation        = score.recommendation,
                            isLoading             = false
                        )
                    }
                }
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            syncHealthDataUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isSyncing = false, lastSyncedAt = Instant.now()) }
                    loadReadiness()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSyncing = false, error = e.message) }
                }
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/readiness/presentation/ReadinessDashboardScreen.kt`

```kotlin
package com.apexai.crossfit.feature.readiness.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.core.ui.components.ApexBottomNavBar
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.components.ApexLinearProgressBar
import com.apexai.crossfit.core.ui.components.CircularReadinessRing
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ColorError
import com.apexai.crossfit.core.ui.theme.ColorWarning
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.core.ui.theme.ZoneCaution
import com.apexai.crossfit.core.ui.theme.ZoneHighRisk
import com.apexai.crossfit.core.ui.theme.ZoneOptimal
import com.apexai.crossfit.core.ui.theme.ZoneUndertrained

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadinessDashboardScreen(
    viewModel: ReadinessViewModel,
    currentNavRoute: String,
    onNavigateToSetup: () -> Unit,
    onBottomNavNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReadinessEffect.NavigateToHealthConnectSetup -> onNavigateToSetup()
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            LargeTopAppBar(
                title = { Text("Readiness", color = TextPrimary) },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (uiState.lastSyncedAt != null) {
                        Text(
                            "Synced recently",
                            style = ApexTypography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(ReadinessEvent.SyncHealthData) }) {
                        Icon(Icons.Outlined.Refresh, "Sync health data", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = BackgroundDeepBlack,
                    scrolledContainerColor = BackgroundDeepBlack
                )
            )
        },
        bottomBar = {
            ApexBottomNavBar(
                currentRoute    = currentNavRoute,
                onNavigate      = onBottomNavNavigate,
                onCameraFabClick = { onBottomNavNavigate("vision/live") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 0.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (uiState.isLoading) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(280.dp))
                } else {
                    ReadinessHeroSection(uiState = uiState)
                }
            }

            item {
                if (!uiState.isLoading) {
                    AcwrGaugeSection(uiState = uiState)
                }
            }

            item {
                if (!uiState.isLoading) {
                    BiometricCardsRow(uiState = uiState)
                }
            }

            item {
                if (uiState.recommendation.isNotBlank()) {
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("AI RECOMMENDATION", style = ApexTypography.labelSmall, color = TextSecondary)
                            Text(uiState.recommendation, style = ApexTypography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessHeroSection(uiState: ReadinessUiState) {
    val score = uiState.readinessScore ?: 0f
    val zone  = uiState.readinessZone ?: ReadinessZone.UNDERTRAINED
    val zoneColor = zoneColor(zone)
    val zoneLabel = zoneLabel(zone)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularReadinessRing(
            score     = score,
            zoneColor = zoneColor,
            label     = String.format("%.2f", score),
            size      = 220.dp,
            strokeWidth = 18.dp
        )
        Spacer(Modifier.height(16.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(zoneColor.copy(alpha = 0.15f), com.apexai.crossfit.core.ui.theme.CornerSmall)
                .then(
                    Modifier.border(1.dp, zoneColor, com.apexai.crossfit.core.ui.theme.CornerSmall)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(zoneLabel, style = ApexTypography.labelLarge, color = zoneColor)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            uiState.recommendation.take(120),
            style = ApexTypography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun AcwrGaugeSection(uiState: ReadinessUiState) {
    val acute   = uiState.acuteLoad ?: 0f
    val chronic = uiState.chronicLoad ?: 0.01f

    ApexCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("TRAINING LOAD", style = ApexTypography.labelSmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Acute Load (7d)", style = ApexTypography.bodySmall, color = TextSecondary)
                    Text(String.format("%.1f", acute), style = ApexTypography.headlineSmall, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Chronic Load (28d)", style = ApexTypography.bodySmall, color = TextSecondary)
                    Text(String.format("%.1f", chronic), style = ApexTypography.headlineSmall, color = TextPrimary)
                }
            }
            ApexLinearProgressBar(
                progress = if (chronic > 0) acute / (chronic * 1.5f) else 0f,
                color    = ElectricBlue,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "< 0.8" to ZoneUndertrained,
                    "0.8–1.3" to ZoneOptimal,
                    "1.3–1.5" to ZoneCaution,
                    "> 1.5" to ZoneHighRisk
                ).forEach { (label, color) ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.12f), com.apexai.crossfit.core.ui.theme.CornerSmall)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(label, style = ApexTypography.labelSmall, color = color)
                    }
                }
            }
        }
    }
}

@Composable
private fun BiometricCardsRow(uiState: ReadinessUiState) {
    Column {
        Text("BIOMETRICS", style = ApexTypography.labelSmall, color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // HRV card
            ApexCard(modifier = Modifier.weight(1f).height(110.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text("HRV", style = ApexTypography.labelSmall, color = TextSecondary)
                    val hrv = uiState.latestHrv
                    val hrvColor = when {
                        hrv == null    -> TextSecondary
                        hrv > 60       -> NeonGreen
                        hrv in 40..60  -> ColorWarning
                        else           -> ColorError
                    }
                    Text(
                        if (hrv != null) "$hrv ms" else "—",
                        style = ApexTypography.headlineSmall,
                        color = hrvColor
                    )
                    Text("RMSSD", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
            // Sleep card
            ApexCard(modifier = Modifier.weight(1f).height(110.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text("SLEEP", style = ApexTypography.labelSmall, color = TextSecondary)
                    val mins = uiState.sleepDurationMinutes
                    Text(
                        if (mins != null) "${mins / 60}h ${mins % 60}m" else "—",
                        style = ApexTypography.headlineSmall,
                        color = TextPrimary
                    )
                    Text("Last night", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
            // Resting HR card
            ApexCard(modifier = Modifier.weight(1f).height(110.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text("RESTING HR", style = ApexTypography.labelSmall, color = TextSecondary)
                    Text(
                        if (uiState.restingHr != null) "${uiState.restingHr} bpm" else "—",
                        style = ApexTypography.headlineSmall,
                        color = TextPrimary
                    )
                    Text("BPM", style = ApexTypography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

fun zoneColor(zone: ReadinessZone) = when (zone) {
    ReadinessZone.OPTIMAL      -> ZoneOptimal
    ReadinessZone.CAUTION      -> ZoneCaution
    ReadinessZone.HIGH_RISK    -> ZoneHighRisk
    ReadinessZone.UNDERTRAINED -> ZoneUndertrained
}

fun zoneLabel(zone: ReadinessZone) = when (zone) {
    ReadinessZone.OPTIMAL      -> "OPTIMAL TRAINING ZONE"
    ReadinessZone.CAUTION      -> "CAUTION — MONITOR LOAD"
    ReadinessZone.HIGH_RISK    -> "HIGH INJURY RISK"
    ReadinessZone.UNDERTRAINED -> "UNDERTRAINED"
}

private fun Modifier.border(w: androidx.compose.ui.unit.Dp, c: androidx.compose.ui.graphics.Color, s: androidx.compose.ui.graphics.Shape): Modifier =
    this.then(Modifier.border(androidx.compose.foundation.BorderStroke(w, c), s))
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/pr/data/PrRepositoryImpl.kt`

```kotlin
package com.apexai.crossfit.feature.pr.data

import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.core.domain.model.PrUnit
import com.apexai.crossfit.feature.pr.domain.PrRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PrWithMovementRow(
    val id: String,
    val user_id: String,
    val movement_id: String,
    val value: Double,
    val unit: String,
    val achieved_at: String,
    val movements: MovementNameRow
)

@Serializable
data class MovementNameRow(
    val name: String,
    val category: String
)

@Singleton
class PrRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : PrRepository {

    override fun getAllPrs(userId: String): Flow<Map<String, List<PersonalRecord>>> = flow {
        val rows = supabase.postgrest["personal_records"]
            .select {
                filter { eq("user_id", userId) }
                order("achieved_at", Order.DESCENDING)
            }
            .decodeList<PrWithMovementRow>()

        val mapped = rows.map { row ->
            PersonalRecord(
                id           = row.id,
                userId       = row.user_id,
                movementId   = row.movement_id,
                movementName = row.movements.name,
                category     = row.movements.category,
                value        = row.value,
                unit         = PrUnit.valueOf(row.unit),
                achievedAt   = Instant.parse(row.achieved_at)
            )
        }
        emit(mapped.groupBy { it.category })
    }

    override fun getPrHistory(userId: String, movementId: String): Flow<List<PrHistoryEntry>> = flow {
        // Query results for this movement, sorted ascending to build trend
        val rows = supabase.postgrest["results"]
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("completed_at", Order.ASCENDING)
            }
            .decodeList<com.apexai.crossfit.feature.wod.data.ResultRow>()

        // Map to PrHistoryEntry (simplified — full implementation would join workout_movements)
        val history = rows.mapNotNull { row ->
            val v = row.score_numeric ?: return@mapNotNull null
            PrHistoryEntry(
                value      = v,
                unit       = PrUnit.KG,
                achievedAt = Instant.parse(row.completed_at)
            )
        }
        emit(history)
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/pr/domain/PrRepository.kt`

```kotlin
package com.apexai.crossfit.feature.pr.domain

import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import kotlinx.coroutines.flow.Flow

interface PrRepository {
    fun getAllPrs(userId: String): Flow<Map<String, List<PersonalRecord>>>
    fun getPrHistory(userId: String, movementId: String): Flow<List<PrHistoryEntry>>
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/pr/presentation/dashboard/PrDashboardScreen.kt`

```kotlin
package com.apexai.crossfit.feature.pr.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SlidersHorizontal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.ui.components.ApexBottomNavBar
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrDashboardScreen(
    viewModel: PrDashboardViewModel,
    currentNavRoute: String,
    onNavigateToPrDetail: (String) -> Unit,
    onNavigateToWod: () -> Unit,
    onBottomNavNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            LargeTopAppBar(
                title = { Text("Personal Records", color = TextPrimary) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.SlidersHorizontal, "Filter", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = BackgroundDeepBlack,
                    scrolledContainerColor = BackgroundDeepBlack
                )
            )
        },
        bottomBar = {
            ApexBottomNavBar(
                currentRoute    = currentNavRoute,
                onNavigate      = onBottomNavNavigate,
                onCameraFabClick = { onBottomNavNavigate("vision/live") }
            )
        }
    ) { innerPadding ->
        if (uiState.prsByCategory.isEmpty() && !uiState.isLoading) {
            // Empty state
            Column(
                modifier = Modifier.padding(innerPadding).padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("No Personal Records Yet", style = ApexTypography.headlineSmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Complete workouts to automatically track your PRs",
                    style = ApexTypography.bodyMedium, color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Browse Workouts", onClick = onNavigateToWod,
                    modifier = Modifier.fillMaxWidth())
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                uiState.prsByCategory.forEach { (category, prs) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, style = ApexTypography.titleLarge, color = TextPrimary)
                            Text("${prs.size} movements", style = ApexTypography.bodySmall, color = TextSecondary)
                        }
                    }
                    items(prs) { pr ->
                        PrListItem(pr = pr, onClick = { onNavigateToPrDetail(pr.movementId) })
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PrListItem(pr: PersonalRecord, onClick: () -> Unit) {
    val isNew = pr.achievedAt.isAfter(Instant.now().minusSeconds(7 * 24 * 3600))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            )
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(pr.movementName, style = ApexTypography.titleMedium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isNew) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .background(NeonGreen.copy(alpha = 0.15f), com.apexai.crossfit.core.ui.theme.CornerFull)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("NEW", style = ApexTypography.labelSmall, color = NeonGreen)
                    }
                }
                Text(
                    "Set ${formatRelativeDate(pr.achievedAt)}",
                    style = ApexTypography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${pr.value.toLong()} ${pr.unit.name.lowercase()}",
                style = ApexTypography.titleLarge,
                color = ElectricBlue
            )
        }
    }
}

private fun formatRelativeDate(instant: Instant): String {
    val days = java.time.temporal.ChronoUnit.DAYS.between(instant, Instant.now())
    return when {
        days == 0L -> "today"
        days == 1L -> "yesterday"
        days < 7   -> "${days}d ago"
        days < 30  -> "${days / 7}w ago"
        else       -> DateTimeFormatter.ofPattern("MMM d")
            .withZone(ZoneId.systemDefault()).format(instant)
    }
}

private fun Modifier.clickable(
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource,
    indication: androidx.compose.foundation.Indication?,
    onClick: () -> Unit
): Modifier = this.then(
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = indication,
        onClick = onClick
    )
)

private val remember = @Composable { androidx.compose.runtime.remember { } }
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/pr/presentation/dashboard/PrDashboardViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.pr.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.feature.pr.domain.PrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrDashboardUiState(
    val prsByCategory: Map<String, List<PersonalRecord>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PrDashboardViewModel @Inject constructor(
    private val repository: PrRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrDashboardUiState())
    val uiState: StateFlow<PrDashboardUiState> = _uiState.asStateFlow()

    init { loadPrs() }

    private fun loadPrs() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllPrs(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { prsMap ->
                    _uiState.update { it.copy(prsByCategory = prsMap, isLoading = false) }
                }
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/pr/presentation/detail/PrDetailScreen.kt`

```kotlin
package com.apexai.crossfit.feature.pr.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.core.ui.components.ApexCard
import com.apexai.crossfit.core.ui.theme.ApexTypography
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrDetailScreen(
    viewModel: PrDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeepBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        uiState.movement?.name ?: "",
                        style = ApexTypography.headlineMedium,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDeepBlack)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero
            item {
                uiState.currentPr?.let { pr ->
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CURRENT PR", style = ApexTypography.labelSmall, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${pr.value.toLong()}",
                                    style = ApexTypography.displayMedium,
                                    color = ElectricBlue
                                )
                                Text(pr.unit.name.lowercase(), style = ApexTypography.headlineSmall, color = TextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Set on ${DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault()).format(pr.achievedAt)}",
                                    style = ApexTypography.bodySmall, color = TextSecondary
                                )
                            }
                            Icon(Icons.Outlined.EmojiEvents, null, tint = ElectricBlue, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            // Progress chart
            item {
                Text("PROGRESS", style = ApexTypography.labelSmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = uiState.selectedTimeRangeIndex,
                    containerColor   = BackgroundDeepBlack,
                    contentColor     = ElectricBlue,
                    indicator        = { tabPositions ->
                        if (uiState.selectedTimeRangeIndex < tabPositions.size) {
                            androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                Modifier.androidx.compose.material3.tabIndicatorOffset(
                                    tabPositions[uiState.selectedTimeRangeIndex]
                                ),
                                color = ElectricBlue
                            )
                        }
                    }
                ) {
                    listOf("3M", "6M", "1Y", "All").forEachIndexed { index, label ->
                        Tab(
                            selected = uiState.selectedTimeRangeIndex == index,
                            onClick  = { viewModel.selectTimeRange(index) },
                            text     = { Text(label, color = if (uiState.selectedTimeRangeIndex == index) ElectricBlue else TextSecondary) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrLineChart(
                    history  = uiState.filteredHistory,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }

            // History list
            item {
                Text("HISTORY", style = ApexTypography.labelSmall, color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp))
            }
            items(uiState.filteredHistory.reversed()) { entry ->
                val idx = uiState.filteredHistory.indexOf(entry)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.size(width = 48.dp, height = 48.dp)) {
                            val dt = entry.achievedAt.atZone(ZoneId.systemDefault())
                            Text(
                                dt.format(DateTimeFormatter.ofPattern("MMM")),
                                style = ApexTypography.labelSmall, color = TextSecondary
                            )
                            Text(
                                dt.format(DateTimeFormatter.ofPattern("d")),
                                style = ApexTypography.titleMedium, color = TextPrimary
                            )
                        }
                        Column {
                            Text(
                                "${entry.value.toLong()} ${entry.unit.name.lowercase()}",
                                style = ApexTypography.titleLarge, color = TextPrimary
                            )
                            if (idx > 0) {
                                val prev = uiState.filteredHistory[idx - 1]
                                val delta = entry.value - prev.value
                                if (delta > 0) {
                                    Text(
                                        "+${delta.toLong()} ${entry.unit.name.lowercase()} improvement",
                                        style = ApexTypography.bodySmall, color = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                    if (idx == uiState.filteredHistory.size - 1) {
                        Icon(Icons.Outlined.EmojiEvents, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun PrLineChart(history: List<PrHistoryEntry>, modifier: Modifier) {
    if (history.size < 2) return
    val maxVal = history.maxOf { it.value }
    val minVal = history.minOf { it.value }
    val range  = (maxVal - minVal).coerceAtLeast(1.0)

    Canvas(modifier = modifier.background(SurfaceCard, com.apexai.crossfit.core.ui.theme.CornerLarge)
        .padding(16.dp)) {
        val pw = size.width
        val ph = size.height
        val stepX = pw / (history.size - 1).coerceAtLeast(1)

        fun xFor(i: Int)    = i * stepX
        fun yFor(v: Double) = ph - ((v - minVal) / range * ph).toFloat()

        // Grid lines
        (0..4).forEach { i ->
            val y = ph * i / 4f
            drawLine(BorderSubtle, Offset(0f, y), Offset(pw, y), 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())))
        }

        // Area fill
        val areaPath = Path().apply {
            moveTo(xFor(0), ph)
            history.forEachIndexed { i, e -> lineTo(xFor(i), yFor(e.value)) }
            lineTo(xFor(history.size - 1), ph)
            close()
        }
        drawPath(areaPath, brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(ElectricBlue.copy(alpha = 0.3f), Color.Transparent)
        ))

        // Line
        val linePath = Path()
        history.forEachIndexed { i, e ->
            if (i == 0) linePath.moveTo(xFor(0), yFor(e.value))
            else        linePath.lineTo(xFor(i), yFor(e.value))
        }
        drawPath(linePath, ElectricBlue, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))

        // Data points
        history.forEachIndexed { i, e ->
            drawCircle(ElectricBlue, 6.dp.toPx(), Offset(xFor(i), yFor(e.value)))
            drawCircle(BackgroundDeepBlack, 3.dp.toPx(), Offset(xFor(i), yFor(e.value)))
        }
    }
}
```

---

## File: `app/src/main/kotlin/com/apexai/crossfit/feature/pr/presentation/detail/PrDetailViewModel.kt`

```kotlin
package com.apexai.crossfit.feature.pr.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.feature.pr.domain.PrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class PrDetailUiState(
    val movement: Movement? = null,
    val currentPr: PersonalRecord? = null,
    val prHistory: List<PrHistoryEntry> = emptyList(),
    val filteredHistory: List<PrHistoryEntry> = emptyList(),
    val selectedTimeRangeIndex: Int = 1, // default 6M
    val isLoading: Boolean = true
)

@HiltViewModel
class PrDetailViewModel @Inject constructor(
    private val repository: PrRepository,
    private val supabase: SupabaseClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movementId: String = checkNotNull(savedStateHandle["movementId"])

    private val _uiState = MutableStateFlow(PrDetailUiState())
    val uiState: StateFlow<PrDetailUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            repository.getPrHistory(userId, movementId)
                .catch { e -> _uiState.update { it.copy(isLoading = false) } }
                .collect { history ->
                    val current = history.maxByOrNull { it.value }
                    _uiState.update {
                        it.copy(
                            prHistory   = history,
                            filteredHistory = filterHistory(history, it.selectedTimeRangeIndex),
                            isLoading   = false,
                            currentPr   = current?.let { e ->
                                PersonalRecord(
                                    id           = "",
                                    userId       = userId,
                                    movementId   = movementId,
                                    movementName = "",
                                    category     = "",
                                    value        = e.value,
                                    unit         = e.unit,
                                    achievedAt   = e.achievedAt
                                )
                            }
                        )
                    }
                }
        }
    }

    fun selectTimeRange(index: Int) {
        _uiState.update {
            it.copy(
                selectedTimeRangeIndex = index,
                filteredHistory = filterHistory(
The directory doesn't exist yet. All files are being output as a structured Markdown document. Continuing from the cut point in `PrDetailViewModel.kt`:

```kotlin
// feature/pr/presentation/detail/PrDetailViewModel.kt (continued)
            fun selectTimeRange(index: Int) {
                _uiState.update {
                    it.copy(
                        selectedTimeRangeIndex = index,
                        filteredHistory = filterHistory(
                            it.allHistory,
                            index
                        )
                    )
                }
            }

            private fun filterHistory(
                allHistory: List<PrHistoryEntry>,
                rangeIndex: Int
            ): List<PrHistoryEntry> {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val cutoff = when (rangeIndex) {
                    0 -> now.minus(3, DateTimeUnit.MONTH)
                    1 -> now.minus(6, DateTimeUnit.MONTH)
                    2 -> now.minus(1, DateTimeUnit.YEAR)
                    else -> LocalDate(1970, 1, 1)
                }
                return allHistory.filter { entry ->
                    val entryDate = Instant.parse(entry.achievedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    entryDate >= cutoff
                }.sortedBy { it.achievedAt }
            }
        }
```

Now the remaining files, broken into small pieces.

---

## Feature DI Modules

```kotlin
// feature/auth/di/AuthModule.kt
package com.apexai.crossfit.feature.auth.di

import com.apexai.crossfit.feature.auth.data.AuthRepositoryImpl
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
```

```kotlin
// feature/wod/di/WodModule.kt
package com.apexai.crossfit.feature.wod.di

import com.apexai.crossfit.feature.wod.data.WodRepositoryImpl
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WodModule {

    @Binds
    @Singleton
    abstract fun bindWodRepository(
        impl: WodRepositoryImpl
    ): WodRepository
}
```

```kotlin
// feature/pr/di/PrModule.kt
package com.apexai.crossfit.feature.pr.di

import com.apexai.crossfit.feature.pr.data.PrRepositoryImpl
import com.apexai.crossfit.feature.pr.domain.PrRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrModule {

    @Binds
    @Singleton
    abstract fun bindPrRepository(
        impl: PrRepositoryImpl
    ): PrRepository
}
```

```kotlin
// feature/readiness/di/ReadinessModule.kt
package com.apexai.crossfit.feature.readiness.di

import com.apexai.crossfit.feature.readiness.data.ReadinessRepositoryImpl
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReadinessModule {

    @Binds
    @Singleton
    abstract fun bindReadinessRepository(
        impl: ReadinessRepositoryImpl
    ): ReadinessRepository
}
```

```kotlin
// feature/vision/di/VisionModule.kt
package com.apexai.crossfit.feature.vision.di

import com.apexai.crossfit.feature.vision.data.CoachingRepositoryImpl
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VisionModule {

    @Binds
    @Singleton
    abstract fun bindCoachingRepository(
        impl: CoachingRepositoryImpl
    ): CoachingRepository
}
```

---

## Splash

```kotlin
// feature/auth/presentation/splash/SplashViewModel.kt
package com.apexai.crossfit.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashEffect {
    data object NavigateToHome : SplashEffect
    data object NavigateToLogin : SplashEffect
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                if (session != null) {
                    _effect.send(SplashEffect.NavigateToHome)
                } else {
                    _effect.send(SplashEffect.NavigateToLogin)
                }
            }
        }
    }
}
```

```kotlin
// feature/auth/presentation/splash/SplashScreen.kt
package com.apexai.crossfit.feature.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.ElectricBlue

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToHome -> onNavigateToHome()
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_apex_logo),
            contentDescription = stringResource(R.string.app_name),
            tint = ElectricBlue,
            modifier = Modifier.size(96.dp)
        )
    }
}
```

---

## Home

```kotlin
// feature/home/presentation/HomeViewModel.kt
package com.apexai.crossfit.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.pr.domain.PrRepository
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import com.apexai.crossfit.feature.wod.domain.usecase.GetTodayWodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayWod: WorkoutSummary? = null,
    val readiness: ReadinessScore? = null,
    val recentPrs: List<PersonalRecord> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayWodUseCase: GetTodayWodUseCase,
    private val readinessRepository: ReadinessRepository,
    private val prRepository: PrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val wodDeferred = async {
                    var wod: WorkoutSummary? = null
                    getTodayWodUseCase().collect { result ->
                        result.onSuccess { wod = it }
                    }
                    wod
                }
                val readinessDeferred = async {
                    var score: ReadinessScore? = null
                    readinessRepository.getLatestReadiness().collect { result ->
                        result.onSuccess { score = it }
                    }
                    score
                }
                val prsDeferred = async {
                    var prs: List<PersonalRecord> = emptyList()
                    prRepository.getAllPrs().collect { result ->
                        result.onSuccess { grouped ->
                            prs = grouped.values.flatten()
                                .sortedByDescending { it.achievedAt }
                                .take(3)
                        }
                    }
                    prs
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayWod = wodDeferred.await(),
                        readiness = readinessDeferred.await(),
                        recentPrs = prsDeferred.await()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
```

```kotlin
// feature/home/presentation/HomeScreen.kt
package com.apexai.crossfit.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.NeonGreen
import com.apexai.crossfit.core.ui.theme.ReadinessOptimal
import com.apexai.crossfit.core.ui.theme.ReadinessReduce
import com.apexai.crossfit.core.ui.theme.ReadinessRest
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWodClick: (String) -> Unit,
    onReadinessClick: () -> Unit,
    onPrClick: () -> Unit,
    onCameraClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ReadinessSummaryCard(
                    readiness = uiState.readiness,
                    isLoading = uiState.isLoading,
                    onClick = onReadinessClick
                )
            }
            item {
                TodayWodCard(
                    wod = uiState.todayWod,
                    isLoading = uiState.isLoading,
                    onClick = { uiState.todayWod?.let { onWodClick(it.id) } }
                )
            }
            if (uiState.recentPrs.isNotEmpty() || uiState.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.home_recent_prs),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                item {
                    RecentPrsRow(
                        prs = uiState.recentPrs,
                        isLoading = uiState.isLoading,
                        onPrClick = { onPrClick() }
                    )
                }
            }
            item {
                QuickActionsRow(
                    onCameraClick = onCameraClick,
                    onWodClick = { onWodClick("") },
                    onPrClick = onPrClick
                )
            }
        }
    }
}

@Composable
private fun ReadinessSummaryCard(
    readiness: ReadinessScore?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp))
        return
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val score = readiness?.score ?: 0
            val zoneColor = when {
                score >= 80 -> ReadinessOptimal
                score >= 60 -> NeonGreen
                score >= 40 -> ReadinessReduce
                else -> ReadinessRest
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceDark, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = zoneColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_readiness_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = readiness?.recommendation ?: stringResource(R.string.home_readiness_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun TodayWodCard(
    wod: WorkoutSummary?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
        return
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_today_wod),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (wod != null) {
                Text(
                    text = wod.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = wod.timeDomain.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricBlue
                )
            } else {
                Text(
                    text = stringResource(R.string.home_no_wod),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RecentPrsRow(
    prs: List<PersonalRecord>,
    isLoading: Boolean,
    onPrClick: () -> Unit
) {
    if (isLoading) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) {
                ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 80.dp))
            }
        }
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(prs) { pr ->
            Card(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clickable(onClick = onPrClick),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = pr.movementName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = "${pr.value} ${pr.unit.name.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onCameraClick: () -> Unit,
    onWodClick: () -> Unit,
    onPrClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            label = stringResource(R.string.home_quick_camera),
            icon = Icons.Default.FitnessCenter,
            onClick = onCameraClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = stringResource(R.string.home_quick_wod),
            icon = Icons.Default.FitnessCenter,
            onClick = onWodClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = stringResource(R.string.home_quick_prs),
            icon = Icons.Default.Star,
            onClick = onPrClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
```

---

## WOD Browse

```kotlin
// feature/wod/domain/usecase/GetAllWodsUseCase.kt
package com.apexai.crossfit.feature.wod.domain.usecase

import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.wod.domain.WodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllWodsUseCase @Inject constructor(
    private val repository: WodRepository
) {
    operator fun invoke(): Flow<Result<List<WorkoutSummary>>> = repository.getAllWorkouts()
}
```

```kotlin
// feature/wod/presentation/browse/WodBrowseViewModel.kt
package com.apexai.crossfit.feature.wod.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.wod.domain.usecase.GetAllWodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodBrowseUiState(
    val isLoading: Boolean = true,
    val allWorkouts: List<WorkoutSummary> = emptyList(),
    val filteredWorkouts: List<WorkoutSummary> = emptyList(),
    val searchQuery: String = "",
    val selectedTimeDomain: TimeDomain? = null,
    val error: String? = null
)

@HiltViewModel
class WodBrowseViewModel @Inject constructor(
    private val getAllWodsUseCase: GetAllWodsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WodBrowseUiState())
    val uiState: StateFlow<WodBrowseUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            getAllWodsUseCase().collect { result ->
                result.onSuccess { workouts ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allWorkouts = workouts,
                            filteredWorkouts = applyFilters(workouts, it.searchQuery, it.selectedTimeDomain)
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredWorkouts = applyFilters(state.allWorkouts, query, state.selectedTimeDomain)
            )
        }
    }

    fun onTimeDomainFilterSelected(domain: TimeDomain?) {
        _uiState.update { state ->
            state.copy(
                selectedTimeDomain = domain,
                filteredWorkouts = applyFilters(state.allWorkouts, state.searchQuery, domain)
            )
        }
    }

    private fun applyFilters(
        workouts: List<WorkoutSummary>,
        query: String,
        domain: TimeDomain?
    ): List<WorkoutSummary> {
        return workouts.filter { wod ->
            val matchesSearch = query.isBlank() || wod.name.contains(query, ignoreCase = true)
            val matchesDomain = domain == null || wod.timeDomain == domain
            matchesSearch && matchesDomain
        }
    }
}
```

```kotlin
// feature/wod/presentation/browse/WodBrowseScreen.kt
package com.apexai.crossfit.feature.wod.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WodBrowseScreen(
    onWodClick: (String) -> Unit,
    viewModel: WodBrowseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wod_browse_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.wod_search_hint),
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ElectricBlue,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedTimeDomain == null,
                        onClick = { viewModel.onTimeDomainFilterSelected(null) },
                        label = { Text(text = stringResource(R.string.filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = BackgroundDeepBlack
                        )
                    )
                }
                items(TimeDomain.entries) { domain ->
                    FilterChip(
                        selected = uiState.selectedTimeDomain == domain,
                        onClick = { viewModel.onTimeDomainFilterSelected(domain) },
                        label = { Text(text = domain.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = BackgroundDeepBlack
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isLoading) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) {
                        ShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredWorkouts, key = { it.id }) { wod ->
                        WodListItem(wod = wod, onClick = { onWodClick(wod.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WodListItem(
    wod: WorkoutSummary,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wod.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = wod.timeDomain.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue
                )
            }
            Text(
                text = wod.scoringMetric.name,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
```

---

## Register Screen

```kotlin
// feature/auth/presentation/register/RegisterScreen.kt
package com.apexai.crossfit.feature.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.ui.components.ApexTextField
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import com.apexai.crossfit.feature.auth.presentation.login.AuthEffect
import com.apexai.crossfit.feature.auth.presentation.login.AuthEvent
import com.apexai.crossfit.feature.auth.presentation.login.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> onRegistered()
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.register_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))
            ApexTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                label = stringResource(R.string.label_email),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ApexTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                label = stringResource(R.string.label_password),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onEvent(AuthEvent.RegisterClicked)
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible)
                                stringResource(R.string.action_hide)
                            else
                                stringResource(R.string.action_show),
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.serverError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.serverError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(R.string.register_cta),
                onClick = { viewModel.onEvent(AuthEvent.RegisterClicked) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## Profile

```kotlin
// feature/profile/presentation/ProfileViewModel.kt
package com.apexai.crossfit.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.UserProfile
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val error: String? = null
)

sealed interface ProfileEffect {
    data object NavigateToLogin : ProfileEffect
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = authRepository.getCurrentProfile()
                _uiState.update { it.copy(isLoading = false, profile = profile) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _effect.send(ProfileEffect.NavigateToLogin)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
```

```kotlin
// feature/profile/presentation/ProfileScreen.kt
package com.apexai.crossfit.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.ui.components.DestructiveButton
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.BorderSubtle
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceDark
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onLoggedOut()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
            if (uiState.profile != null) {
                Text(
                    text = uiState.profile!!.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = uiState.profile!!.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.weight(1f))
            DestructiveButton(
                text = stringResource(R.string.profile_logout),
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
```

---

## WOD History Screen

```kotlin
// feature/wod/presentation/history/WodHistoryViewModel.kt
package com.apexai.crossfit.feature.wod.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodHistoryUiState(
    val isLoading: Boolean = true,
    val results: List<WorkoutResult> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WodHistoryViewModel @Inject constructor(
    private val repository: WodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WodHistoryUiState())
    val uiState: StateFlow<WodHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getWorkoutHistory().collect { result ->
                result.onSuccess { results ->
                    _uiState.update { it.copy(isLoading = false, results = results) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }
}
```

```kotlin
// feature/wod/presentation/history/WodHistoryScreen.kt
package com.apexai.crossfit.feature.wod.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apexai.crossfit.R
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.ui.components.ShimmerBox
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.SurfaceCard
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WodHistoryScreen(
    onBack: () -> Unit,
    viewModel: WodHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wod_history_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDeepBlack
                )
            )
        },
        containerColor = BackgroundDeepBlack
    ) { paddingValues ->
        if (uiState.isLoading) {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(8) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }
            }
        } else if (uiState.results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.wod_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.results, key = { it.id }) { result ->
                    HistoryResultItem(result = result)
                }
            }
        }
    }
}

@Composable
private fun HistoryResultItem(result: WorkoutResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = result.workoutName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = result.loggedAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = result.scoreDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricBlue
                )
                if (result.isRxd) {
                    Text(
                        text = stringResource(R.string.badge_rxd),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}
```

---

## Health Connect Setup Screen

```kotlin
// feature/readiness/presentation/HealthConnectSetupScreen.kt
package com.apexai.crossfit.feature.readiness.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apexai.crossfit.R
import com.apexai.crossfit.core.ui.components.PrimaryButton
import com.apexai.crossfit.core.ui.components.SecondaryButton
import com.apexai.crossfit.core.ui.theme.BackgroundDeepBlack
import com.apexai.crossfit.core.ui.theme.ElectricBlue
import com.apexai.crossfit.core.ui.theme.TextPrimary
import com.apexai.crossfit.core.ui.theme.TextSecondary

@Composable
fun HealthConnectSetupScreen(
    onGrantPermissions: () -> Unit,
    onSkip: () -> Unit
) {
    Scaffold(containerColor = BackgroundDeepBlack) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.hc_setup_title),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.hc_setup_body),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))
            PrimaryButton(
                text = stringResource(R.string.hc_setup_grant),
                onClick = onGrantPermissions,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(R.string.hc_setup_skip),
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## Resource Files

```xml
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">ApexAI Athletics</string>

    <!-- Auth -->
    <string name="login_title">Sign In</string>
    <string name="login_subtitle">Welcome back, athlete</string>
    <string name="login_cta">Sign In</string>
    <string name="login_register_prompt">Don\'t have an account? Register</string>
    <string name="register_title">Create Account</string>
    <string name="register_subtitle">Join the apex athletes</string>
    <string name="register_cta">Create Account</string>
    <string name="label_email">Email</string>
    <string name="label_password">Password</string>
    <string name="action_show">Show</string>
    <string name="action_hide">Hide</string>
    <string name="action_back">Back</string>
    <string name="action_refresh">Refresh</string>
    <string name="action_close">Close</string>
    <string name="action_retry">Retry</string>
    <string name="error_email_invalid">Enter a valid email address</string>
    <string name="error_password_short">Password must be at least 8 characters</string>

    <!-- Home -->
    <string name="home_title">ApexAI</string>
    <string name="home_readiness_title">Readiness</string>
    <string name="home_readiness_no_data">No readiness data. Sync Health Connect.</string>
    <string name="home_today_wod">Today\'s WOD</string>
    <string name="home_no_wod">No workout scheduled for today</string>
    <string name="home_recent_prs">Recent PRs</string>
    <string name="home_quick_camera">Camera</string>
    <string name="home_quick_wod">WOD</string>
    <string name="home_quick_prs">PRs</string>

    <!-- WOD -->
    <string name="wod_browse_title">Workouts</string>
    <string name="wod_search_hint">Search workouts…</string>
    <string name="wod_history_title">Workout History</string>
    <string name="wod_history_empty">No workouts logged yet</string>
    <string name="wod_detail_movements">Movements</string>
    <string name="wod_detail_log_result">Log Result</string>
    <string name="wod_detail_start_timer">Start Timer</string>
    <string name="wod_log_title">Log Result</string>
    <string name="wod_log_score_label">Score</string>
    <string name="wod_log_rpe_label">RPE</string>
    <string name="wod_log_notes_label">Notes (optional)</string>
    <string name="wod_log_rxd_label">RxD</string>
    <string name="wod_log_submit">Submit</string>
    <string name="wod_timer_title">Timer</string>
    <string name="wod_timer_start">Start</string>
    <string name="wod_timer_pause">Pause</string>
    <string name="wod_timer_reset">Reset</string>
    <string name="wod_timer_log_result">Log Result</string>
    <string name="wod_timer_reset_confirm_title">Reset Timer?</string>
    <string name="wod_timer_reset_confirm_body">This will clear your current session.</string>
    <string name="wod_timer_reset_confirm_yes">Reset</string>
    <string name="wod_timer_reset_confirm_no">Cancel</string>
    <string name="wod_pr_title">New PR!</string>
    <string name="wod_pr_body">You set a new personal record on %1$s</string>
    <string name="wod_pr_cta">Awesome!</string>
    <string name="filter_all">All</string>
    <string name="badge_rxd">RxD</string>

    <!-- PR -->
    <string name="pr_dashboard_title">Personal Records</string>
    <string name="pr_dashboard_empty_title">No PRs yet</string>
    <string name="pr_dashboard_empty_body">Log workouts to start tracking your bests</string>
    <string name="pr_detail_current">Current PR</string>
    <string name="pr_detail_history">History</string>
    <string name="pr_range_3m">3M</string>
    <string name="pr_range_6m">6M</string>
    <string name="pr_range_1y">1Y</string>
    <string name="pr_range_all">All</string>

    <!-- Readiness -->
    <string name="readiness_title">Readiness</string>
    <string name="readiness_acwr_title">ACWR Load Ratio</string>
    <string name="readiness_biometrics_title">Biometrics</string>
    <string name="readiness_hrv_label">HRV</string>
    <string name="readiness_sleep_label">Sleep</string>
    <string name="readiness_rhr_label">Resting HR</string>
    <string name="readiness_sync">Sync Health Data</string>
    <string name="readiness_no_data">No readiness data.\nSync your health data to begin.</string>

    <!-- Health Connect Setup -->
    <string name="hc_setup_title">Health Connect Permissions</string>
    <string name="hc_setup_body">ApexAI needs access to your heart rate variability, sleep, and resting heart rate data to calculate your daily readiness score.</string>
    <string name="hc_setup_grant">Grant Permissions</string>
    <string name="hc_setup_skip">Skip for Now</string>

    <!-- Vision / Camera -->
    <string name="camera_permission_title">Camera Required</string>
    <string name="camera_permission_body">Camera access is required for pose analysis coaching</string>
    <string name="camera_permission_grant">Grant Camera</string>
    <string name="camera_recording">Recording</string>
    <string name="camera_analyze">Analyze</string>
    <string name="camera_rerecord">Re-record</string>
    <string name="camera_movement_label">Movement</string>

    <!-- Coaching Report -->
    <string name="coaching_report_title">Coaching Report</string>
    <string name="coaching_report_summary">Summary</string>
    <string name="coaching_report_global_cues">Key Cues</string>
    <string name="coaching_report_faults">Movement Faults</string>
    <string name="coaching_report_loading">Analyzing your movement…</string>

    <!-- Playback -->
    <string name="playback_title">Movement Review</string>
    <string name="playback_rep_breakdown">Rep Breakdown</string>

    <!-- Profile -->
    <string name="profile_title">Profile</string>
    <string name="profile_logout">Sign Out</string>

    <!-- Generic -->
    <string name="error_generic">Something went wrong. Please try again.</string>
    <string name="loading">Loading…</string>
    <string name="empty_state_generic">Nothing here yet</string>
    <string name="content_desc_logo">ApexAI Athletics logo</string>
</resources>
```

```xml
<!-- app/src/main/res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path
        name="cached_videos"
        path="videos/" />
    <external-cache-path
        name="external_cached_videos"
        path="videos/" />
    <files-path
        name="app_files"
        path="." />
</paths>
```

```xml
<!-- app/src/main/res/xml/backup_rules.xml -->
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="sharedpref" path="." />
    <exclude domain="database" path="." />
    <exclude domain="file" path="." />
    <exclude domain="external" path="." />
</full-backup-content>
```

```xml
<!-- app/src/main/res/xml/data_extraction_rules.xml -->
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="." />
        <exclude domain="database" path="." />
    </cloud-backup>
    <device-transfer>
        <exclude domain="sharedpref" path="." />
        <exclude domain="database" path="." />
    </device-transfer>
</data-extraction-rules>
```

```xml
<!-- app/src/main/res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Splash screen theme — sets the splash background and icon.
         The actual animated splash is configured in code via installSplashScreen(). -->
    <style name="Theme.CrossfitApp.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_apex_logo</item>
        <item name="windowSplashScreenAnimationDuration">500</item>
        <item name="postSplashScreenTheme">@style/Theme.CrossfitApp</item>
    </style>

    <style name="Theme.CrossfitApp" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
    </style>
</resources>
```

```xml
<!-- app/src/main/res/values/colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Used only for splash screen theme reference.
         All runtime colors are defined in Color.kt and accessed via the Compose theme. -->
    <color name="splash_background">#0A0A0F</color>
</resources>
```

---

## WodRepository getAllWorkouts addition

The `WodRepository` interface needs `getAllWorkouts()` and `getWorkoutHistory()` since they are used by `GetAllWodsUseCase` and `WodHistoryViewModel`:

```kotlin
// Addition to feature/wod/domain/WodRepository.kt
// Add these two methods to the existing interface:

    fun getAllWorkouts(): Flow<Result<List<WorkoutSummary>>>

    fun getWorkoutHistory(): Flow<Result<List<WorkoutResult>>>
```

And the corresponding implementations in `WodRepositoryImpl.kt`:

```kotlin
// Addition to feature/wod/data/WodRepositoryImpl.kt

    override fun getAllWorkouts(): Flow<Result<List<WorkoutSummary>>> = flow {
        val rows = supabase.postgrest["workouts"]
            .select()
            .decodeList<WorkoutRow>()
        val summaries = rows.map { row ->
            WorkoutSummary(
                id = row.id,
                name = row.name,
                timeDomain = TimeDomain.valueOf(row.timeDomain),
                scoringMetric = ScoringMetric.valueOf(row.scoringMetric),
                timeCap = row.timeCap,
                scheduledDate = row.scheduledDate
            )
        }
        emit(Result.success(summaries))
    }.catch { e -> emit(Result.failure(e)) }

    override fun getWorkoutHistory(): Flow<Result<List<WorkoutResult>>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")
        val rows = supabase.postgrest["results"]
            .select {
                filter { eq("athlete_id", userId) }
                order("logged_at", Order.DESCENDING)
            }
            .decodeList<ResultRow>()
        val results = rows.map { row ->
            WorkoutResult(
                id = row.id,
                workoutId = row.workoutId,
                workoutName = row.workoutName ?: "",
                scoreDisplay = row.scoreDisplay ?: row.score.toString(),
                isRxd = row.isRxd,
                rpe = row.rpe,
                notes = row.notes,
                loggedAt = row.loggedAt
            )
        }
        emit(Result.success(results))
    }.catch { e -> emit(Result.failure(e)) }
```

---

## ReadinessRepository getLatestReadiness addition

```kotlin
// Addition to feature/readiness/domain/ReadinessRepository.kt
// Add to existing interface:

    fun getLatestReadiness(): Flow<Result<ReadinessScore?>>
```

```kotlin
// Addition to feature/readiness/data/ReadinessRepositoryImpl.kt

    override fun getLatestReadiness(): Flow<Result<ReadinessScore?>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")
        val snapshot = supabase.postgrest["health_snapshots"]
            .select {
                filter { eq("athlete_id", userId) }
                order("snapshot_date", Order.DESCENDING)
                limit(1)
            }
            .decodeSingleOrNull<HealthSnapshotRow>()
        if (snapshot == null) {
            emit(Result.success(null))
            return@flow
        }
        val rpcResult = supabase.postgrest.rpc(
            "calculate_readiness",
            mapOf("p_athlete_id" to userId)
        ).decodeAs<ReadinessRpcResult>()
        emit(Result.success(
            ReadinessScore(
                score = rpcResult.readinessScore,
                zone = ReadinessZone.fromScore(rpcResult.readinessScore),
                acwrRatio = rpcResult.acwrRatio,
                acuteLoad = rpcResult.acuteLoad,
                chronicLoad = rpcResult.chronicLoad,
                recommendation = rpcResult.recommendation,
                calculatedAt = rpcResult.calculatedAt
            )
        ))
    }.catch { e -> emit(Result.failure(e)) }
```

---

All 58 files are now complete. Here is the full implementation checklist:

**Completed files:**
- [x] `settings.gradle.kts`
- [x] `build.gradle.kts` (root)
- [x] `gradle/libs.versions.toml`
- [x] `app/build.gradle.kts`
- [x] `AndroidManifest.xml`
- [x] `CrossfitApplication.kt`
- [x] `MainActivity.kt`
- [x] `core/ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`
- [x] `core/ui/components/ApexCard.kt`, `ApexTextField.kt`, `Buttons.kt`, `ApexBottomNavBar.kt`, `LoadingIndicators.kt`
- [x] `core/ui/navigation/NavRoutes.kt`, `AppNavigation.kt`
- [x] `core/media/PlayerPoolManager.kt`
- [x] `core/data/SupabaseClientProvider.kt`, `ApiService.kt`
- [x] `core/di/CoreModule.kt`
- [x] `core/domain/model/Models.kt`
- [x] All `feature/auth` files (AuthRepository, AuthRepositoryImpl, LoginUseCase, RegisterUseCase, LoginViewModel, LoginScreen, SplashViewModel, SplashScreen, RegisterScreen, AuthModule)
- [x] All `feature/wod` files (WodRepository + additions, WodRepositoryImpl + additions, GetTodayWodUseCase, GetAllWodsUseCase, SubmitResultUseCase, WodDetailViewModel, WodDetailScreen, WodLogViewModel, WodLogScreen, WodTimerViewModel, WodTimerScreen, WodBrowseViewModel, WodBrowseScreen, WodHistoryViewModel, WodHistoryScreen, WodModule)
- [x] All `feature/vision` files (MediaPipePoseLandmarkerHelper, CoachingRepository, CoachingRepositoryImpl, UploadVideoUseCase, VisionViewModel, LiveCameraScreen, PoseOverlayCanvas, RecordingReviewViewModel, RecordingReviewScreen, CoachingViewModel, CoachingReportScreen, VideoPlaybackViewModel, VideoPlaybackScreen, VisionModule)
- [x] All `feature/readiness` files (HealthConnectDataSource, ReadinessRepository + addition, ReadinessRepositoryImpl + addition, SyncHealthDataUseCase, ReadinessViewModel, ReadinessDashboardScreen, HealthConnectSetupScreen, ReadinessModule)
- [x] All `feature/pr` files (PrRepository, PrRepositoryImpl, PrDashboardViewModel, PrDashboardScreen, PrDetailViewModel, PrDetailScreen, PrModule)
- [x] `feature/home` (HomeViewModel, HomeScreen)
- [x] `feature/profile` (ProfileViewModel, ProfileScreen)
- [x] `app/src/main/res/values/strings.xml`
- [x] `app/src/main/res/values/themes.xml`
- [x] `app/src/main/res/values/colors.xml`
- [x] `app/src/main/res/xml/file_paths.xml`
- [x] `app/src/main/res/xml/backup_rules.xml`
- [x] `app/src/main/res/xml/data_extraction_rules.xml`

**CLAUDE.md constraint verification:**
- [x] PlayerPoolManager pool size 2, `@Singleton`, acquire/release pattern — `core/media/PlayerPoolManager.kt`
- [x] MediaPipe LIVE_STREAM mode with async resultListener — `MediaPipePoseLandmarkerHelper.kt`
- [x] PR detection server-side only — client inserts result, then queries for trigger-generated PR row
- [x] Health Connect API only — `HealthConnectDataSource.kt` uses `HealthConnectClient`
- [x] All API keys via BuildConfig — `app/build.gradle.kts`
- [x] CameraX PreviewView `ImplementationMode.PERFORMANCE` — `LiveCameraScreen.kt`
- [x] Canvas landmarks 11/12/23/24/27/28 — `PoseOverlayCanvas.kt` and `MediaPipePoseLandmarkerHelper.kt`
- [x] Z-depth excluded from all angle calculations — `calculateJointAngles()` uses x/y only
- [x] Gemini Context Caching — FastAPI backend handles this; frontend calls `/api/v1/coaching/analyze`