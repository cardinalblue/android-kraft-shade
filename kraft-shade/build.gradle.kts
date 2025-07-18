
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.vanniktech.maven.publish") version "0.34.0"
}


android {
    namespace = "com.cardinalblue.kraftshade"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags.clear()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    val publishVersion = project.properties["publish_version"]?.toString() ?: run {
        throw IllegalArgumentException("publish_version property is required. Use -Ppublish_version=X.Y.Z")
    }
    coordinates("com.cardinalblue", "kraftshade", publishVersion)

    pom {
        name.set("KraftShade")
        description.set("KraftShade is a modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0. Built with coroutines support and a focus on developer experience, KraftShade makes complex graphics operations simple while maintaining flexibility and performance.")
        inceptionYear.set("2025")
        url.set("https://github.com/cardinalblue/android-kraft-shade")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("landicefu")
                name.set("Landice Fu")
                url.set("https://github.com/landicefu")
            }

            developer {
                id.set("YujiWongTW")
                name.set("Yuji Wong")
                url.set("https://github.com/YujiWongTW")
            }

            developer {
                id.set("hungyanbin")
                name.set("Yanbin Hung")
                url.set("https://github.com/hungyanbin")
            }
        }

        scm {
            url.set("https://github.com/cardinalblue/android-kraft-shade")
            connection.set("scm:git:git://github.com/cardinalblue/android-kraft-shade.git")
            developerConnection.set("scm:git:ssh://git@github.com/cardinalblue/android-kraft-shade.git")
        }
    }
}
