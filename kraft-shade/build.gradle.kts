import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/cardinalblue/android-public-maven-packages")

                credentials {
                    username = ""
                    password = gradleLocalProperties(rootDir, providers).getProperty("github.personalAccessToken")
                }
            }
        }

        publications {
            val packageGroupId = "com.cardinalblue"
            val packageArtifactId = "kraftshade"

            val envKey = "MAVEN_PACKAGE_VERSION"
            val envVersion = System.getenv(envKey)
            val packageVersion = if (envVersion != null && envVersion.isNotBlank()) {
                envVersion
            } else {
                println("Environment variable $envKey is not set. Use default version 0.1.0 for publication.")
                "0.1.0"
            }

            create<MavenPublication>("release") {
                groupId = packageGroupId
                artifactId = packageArtifactId
                version = packageVersion

                from(components["release"])
            }
        }
    }
}