@file:Suppress("UnstableApiUsage")

rootProject.name = "game-server"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    pluginManagement.plugins.apply {
        kotlin("jvm").version("1.8.0")
    }
}
