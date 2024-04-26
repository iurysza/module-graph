package dev.iurysouza.modulegraph

import java.io.Serializable
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

@Suppress("UnusedPrivateMember")
sealed class ModuleType(
    open val id: String,
    open val color: String,
) : Serializable {
    data class Unknown(override val color: String = "#676767") : ModuleType("unknown", color)

    data class Custom(
        override val id: String,
        override val color: String,
    ) : ModuleType(id, color)

    data class KotlinMultiplatform(override val color: String = "#C792EA") : ModuleType(
        id = "org.jetbrains.kotlin.multiplatform",
        color = color,
    )

    data class ReactNativeLibrary(override val color: String = "#5DD3F3") : ModuleType(
        id = "com.facebook.react:react-.*",
        color = color,
    )

    data class AndroidApp(override val color: String = "#2C4162") : ModuleType(
        id = "com.android.application",
        color = color,
    )

    data class AndroidLibrary(override val color: String = "#3BD482") : ModuleType(
        id = "com.android.library",
        color = color,
    )

    data class Kotlin(override val color: String = "#8150FF") : ModuleType(
        id = "org.jetbrains.kotlin.jvm",
        color = color,
    )

    data class JavaLibrary(override val color: String = "#EC8324") : ModuleType(
        id = "java-library",
        color = color,
    )

    data class Java(override val color: String = "#B5661C") : ModuleType(
        id = "java",
        color = color,
    )

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

private val defaultPlugins = listOf(
    ModuleType.ReactNativeLibrary(),
    ModuleType.KotlinMultiplatform(),
    ModuleType.AndroidApp(),
    ModuleType.AndroidLibrary(),
    ModuleType.Kotlin(),
    ModuleType.JavaLibrary(),
    ModuleType.Java(),
)

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
internal fun Project.getModuleType(
    customPlugins: List<ModuleType>,
): ModuleType = (customPlugins + defaultPlugins)
    .distinctBy { it.id }
    .sortedWith(pluginTypeComparator)
    .firstOrNull {
        plugins.hasPlugin(it.id) || hasLibraryDependency(it.id)
    } ?: ModuleType.Unknown()

internal fun Project.hasLibraryDependency(dependencyGroupAndName: String): Boolean = runCatching {
    configurations.flatMap { configuration ->
        configuration.dependencies.filterIsInstance<ExternalModuleDependency>()
    }.any { dependency ->
        dependencyGroupAndName.toRegex().matches("${dependency.group}:${dependency.name}")
    }
}.getOrElse { e ->
    println("Error resolving dependencies: ${e.message}")
    false
}

internal val pluginPrecedenceOrder = listOf(
    ModuleType.Custom::class,
    ModuleType.AndroidApp::class,
    ModuleType.ReactNativeLibrary::class,
    ModuleType.KotlinMultiplatform::class,
    ModuleType.AndroidLibrary::class,
    ModuleType.Kotlin::class,
    ModuleType.JavaLibrary::class,
    ModuleType.Java::class,
    ModuleType.Unknown::class,
)

internal val pluginTypeComparator = Comparator { a: ModuleType, b: ModuleType ->
    val indexA = pluginPrecedenceOrder.indexOfFirst { it.isInstance(a) }
    val indexB = pluginPrecedenceOrder.indexOfFirst { it.isInstance(b) }
    indexA.compareTo(indexB)
}
