import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

android {
    namespace = "com.cardinalblue.kraftshade.compose"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
}


mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.cardinalblue", "kraftshade-compose", "1.0.32")

    pom {
        name.set("KraftShade Compose")
        description.set("A Compose library for KraftShade, a Kotlin-based image processing library")
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
