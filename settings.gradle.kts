pluginManagement {
    includeBuild("plugin-build")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("dev.iurysouza.modulegraph.settings")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "module-graph-plugin-project"

include(":sample:alpha")
include(":sample:beta")
include(":sample:zeta")
include(":sample:test")
include(":sample:container:gama")
include(":sample:container:delta")
