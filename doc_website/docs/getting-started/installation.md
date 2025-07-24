---
sidebar_position: 1
---

# Installation

KraftShade is available on Maven Central and can be easily integrated into your Android project using Gradle.

## Requirements

- Android API level 21 (Android 5.0 Lollipop) or higher
- Kotlin 1.5.0 or higher
- OpenGL ES 2.0 support on the target device

## Adding Dependencies

You can add KraftShade to your project using one of the following methods:

### Gradle (build.gradle.kts)

```kotlin
dependencies {
    // Core library
    implementation("com.cardinalblue:kraftshade:latest_version")
    
    // Optional: Jetpack Compose integration
    implementation("com.cardinalblue:kraftshade-compose:latest_version")
}
```

Replace `latest_version` with the current version of KraftShade. You can find the latest version by checking the badges at the top of the [GitHub repository](https://github.com/cardinalblue/android-kraft-shade) or by visiting the [Maven Central repository](https://central.sonatype.com/artifact/com.cardinalblue/kraftshade).

### Version Catalog (libs.versions.toml)

If you're using Gradle's version catalog feature, add the following to your `libs.versions.toml` file:

```toml
[versions]
kraftshade = "latest_version"

[libraries]
kraftshade-core = { group = "com.cardinalblue", name = "kraftshade", version.ref = "kraftshade" }
kraftshade-compose = { group = "com.cardinalblue", name = "kraftshade-compose", version.ref = "kraftshade" }
```

Then in your module's build.gradle.kts file:

```kotlin
dependencies {
    implementation(libs.kraftshade.core)
    // Optional: Jetpack Compose integration
    implementation(libs.kraftshade.compose)
}
```

## Setting Up Logging

It's recommended to initialize KraftShade's logging system in your Application class:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Set the log level (DEBUG, INFO, WARNING, ERROR, or NONE)
        KraftLogger.logLevel = KraftLogger.Level.DEBUG
        
        // Optionally throw exceptions on errors for easier debugging during development
        KraftLogger.throwOnError = true
    }
}
```

Don't forget to register your Application class in your AndroidManifest.xml:

```xml
<application
    android:name=".App"
    ...>
    <!-- Your application components -->
</application>
```

## Next Steps

Now that you have KraftShade installed, you can:

- Follow the [Quick Start Guide](./quick-start-guide) to create your first KraftShade application
- Learn about the [Basic Concepts](./basic-concepts) of KraftShade
- Create your [First Effect](./first-effect) with KraftShade
