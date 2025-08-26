import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

android {
    namespace = "com.cardinalblue.kraftshade.media3"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    if (gradleLocalProperties(rootDir, providers).getProperty("library.build_with_submodule") == "true") {
        api(project(":kraft-shade"))
    } else {
        api(libs.kraftshade)
    }
    implementation(libs.androidx.media3.effect)
    implementation(libs.jetBrains.coroutinesAndroid)
}


mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    val publishVersion = project.properties["publish_version"]?.toString()
    coordinates("com.cardinalblue", "kraftshade-media3", publishVersion)

    pom {
        name.set("KraftShade Media3")
        description.set("A library that enables the Media3 Player API and Transformer API to use KraftShade for processing video frames")
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
