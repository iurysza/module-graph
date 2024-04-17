package dev.iurysouza.modulegraph.gradle

import java.io.Serializable
import org.gradle.api.Project

@Suppress("UnusedPrivateMember")
sealed class PluginType(
    open val id: String,
    open val color: String,
) : Serializable {
    data class Unknown(override val color: String = "#FFFFFF") : PluginType("unknown", color)

    data class Custom(
        override val id: String,
        override val color: String = "#B2CCD6",
    ) : PluginType(id, color)

    data class KotlinMultiplatform(override val color: String = "#C792EA") : PluginType(
        id = "org.jetbrains.kotlin.multiplatform",
        color = color,
    )

    data class ReactNativeLibrary(override val color: String = "#FFCB6B") : PluginType(
        id = "react-native",
        color = color,
    )

    data class Flutter(override val color: String = "#02569B") : PluginType(
        id = "dev.flutter.flutter-gradle-plugin",
        color = color,
    )

    data class AndroidApp(override val color: String = "#468079") : PluginType(
        id = "com.android.application",
        color = color,
    )

    data class AndroidLibrary(override val color: String = "#80CBC4") : PluginType(
        id = "com.android.library",
        color = color,
    )

    data class Kotlin(override val color: String = "#F07178") : PluginType(
        id = "org.jetbrains.kotlin.jvm",
        color = color,
    )

    data class JavaLibrary(override val color: String = "#82AAFF") : PluginType(
        id = "java-library",
        color = color,
    )

    data class Java(override val color: String = "#C3E88D") : PluginType(
        id = "java",
        color = color,
    )

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

/**
 * Determines the primary plugin type applied to the project based on a precedence order.
 *
 * The first plugin from this ordered list that matches a plugin applied to the project
 * is returned as the primary plugin type. If no such plugin is found, `PluginType.Unknown()`
 * is returned.
 *
 * @param customPlugins A list of additional `PluginType` instances that should be considered
 *                      alongside the predefined list of plugins.
 * @return The primary `PluginType` applied to the project or `PluginType.Unknown()` if
 *         no match is found.
 */
internal fun Project.identifyPlugin(
    customPlugins: List<PluginType>,
): PluginType {
    val defaultPlugins = listOf(
        PluginType.Flutter(),
        PluginType.ReactNativeLibrary(),
        PluginType.KotlinMultiplatform(),
        PluginType.AndroidApp(),
        PluginType.AndroidLibrary(),
        PluginType.Kotlin(),
        PluginType.JavaLibrary(),
        PluginType.Java(),
    )

    return (customPlugins + defaultPlugins)
        .distinctBy { it::class }
        .sortedWith(pluginTypeComparator)
        .firstOrNull { project.plugins.hasPlugin(it.id) } ?: PluginType.Unknown()
}

internal val pluginPrecedenceOrder = listOf(
    PluginType.Custom::class,
    PluginType.Flutter::class,
    PluginType.ReactNativeLibrary::class,
    PluginType.KotlinMultiplatform::class,
    PluginType.AndroidApp::class,
    PluginType.AndroidLibrary::class,
    PluginType.Kotlin::class,
    PluginType.JavaLibrary::class,
    PluginType.Java::class,
    PluginType.Unknown::class,
)

internal val pluginTypeComparator = Comparator { a: PluginType, b: PluginType ->
    val indexA = pluginPrecedenceOrder.indexOfFirst { it.isInstance(a) }
    val indexB = pluginPrecedenceOrder.indexOfFirst { it.isInstance(b) }
    indexA.compareTo(indexB)
}
