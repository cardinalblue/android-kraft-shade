pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/cardinalblue/android-public-maven-packages")
            credentials {
                username = ""
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "KraftShade"
include(":demo")
include(":kraft-shade")
include(":kraft-shade-compose")
include(":kraft-shade-media3")