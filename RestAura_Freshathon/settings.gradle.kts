// settings.gradle.kts

pluginManagement {
    repositories {
        // Google's repo has Android Gradle Plugin, Compose Compiler, etc.
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("org\\.jetbrains\\.kotlin.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // JitPack for GitHub-hosted artifacts
        maven("https://jitpack.io")
    }

    plugins {
        // Kotlin + Compose plugin declaration
        id("org.jetbrains.kotlin.android") version "2.0.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPack for runtime dependencies
        maven("https://jitpack.io")
    }
}

rootProject.name = "My Application"
include(":app")
