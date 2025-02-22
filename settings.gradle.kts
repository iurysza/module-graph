pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
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
includeBuild("plugin-build")
