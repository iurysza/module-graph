package dev.iurysouza.modulegraph.gradle

import java.io.Serializable

/**
 * Represents a dependency on a project.
 * Contains the name of the configuration to which the dependency belongs.
 */
internal data class Dependency(
    val path: String,
    val configName: String? = null,
    val pluginType: PluginType? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 46465844
    }
}

@Suppress("UnusedPrivateMember")
internal sealed class PluginType(open val id: String) : Serializable {
    data class Custom(override val id: String) : PluginType(id)
    object AndroidApp : PluginType("com.android.application") {
        private fun readResolve(): Any = AndroidApp
    }

    object AndroidLibrary : PluginType("com.android.library") {
        private fun readResolve(): Any = AndroidLibrary
    }

    object KotlinMultiplatform : PluginType("org.jetbrains.kotlin.multiplatform") {
        private fun readResolve(): Any = KotlinMultiplatform
    }

    object ReactNativeLibrary : PluginType("react-native") {
        private fun readResolve(): Any = ReactNativeLibrary
    }

    object Kotlin : PluginType("org.jetbrains.kotlin.jvm") {
        private fun readResolve(): Any = Kotlin
    }

    object JavaLibrary : PluginType("java-library") {
        private fun readResolve(): Any = JavaLibrary
    }

    object Java : PluginType("java") {
        private fun readResolve(): Any = Java
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
