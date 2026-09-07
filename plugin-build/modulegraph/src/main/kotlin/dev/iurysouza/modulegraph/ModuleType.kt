package dev.iurysouza.modulegraph

import java.io.Serializable as JavaSerializable
import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

@Suppress("UnusedPrivateMember")
@Serializable
sealed class ModuleType(
    open val id: String,
    open val color: String,
) : JavaSerializable {
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
 * Determines the primary [ModuleType] from a precedence-ordered list of candidates.
 *
 * The first candidate whose id matches an applied plugin id or an external dependency coordinate is
 * returned, or [ModuleType.Unknown] if none match.
 *
 * @param pluginIds the ids of plugins applied to the project.
 * @param externalDependencies the "group:name" coordinates of the project's external dependencies.
 * @param customPlugins additional [ModuleType] candidates considered alongside the defaults.
 */
internal fun resolveModuleType(
    pluginIds: List<String>,
    externalDependencies: List<String>,
    customPlugins: List<ModuleType>,
): ModuleType = (customPlugins + defaultPlugins)
    .distinctBy { it.id }
    .sortedWith(pluginTypeComparator)
    .firstOrNull { type ->
        pluginIds.contains(type.id) || externalDependencies.any {
            type.id.toRegex().matches(it)
        }
    } ?: ModuleType.Unknown()

/** @return the ids of all known module-type plugins applied to this project. */
internal fun Project.appliedModuleTypePluginIds(
    customPlugins: List<ModuleType>,
): List<String> = (customPlugins + defaultPlugins)
    .map { it.id }
    .distinct()
    .filter { plugins.hasPlugin(it) }

/**
 * Snapshot of applied plugin ids for [ProjectInfo].
 *
 * Prefers listing every applied plugin id via Gradle's plugin manager so custom
 * [ModuleType] plugin ids survive Isolated Projects snapshots. Falls back to probing
 * [defaultPlugins] plus [customPlugins] when the richer listing is unavailable.
 */
internal fun Project.snapshotAppliedPluginIds(
    customPlugins: List<ModuleType> = emptyList(),
): List<String> {
    val fromManager = allAppliedPluginIdsOrNull().orEmpty()
    val fromProbe = appliedModuleTypePluginIds(customPlugins)
    return (fromManager + fromProbe).distinct()
}

/**
 * Lists applied plugin ids using [org.gradle.api.plugins.PluginManager] internals when
 * present (`getPluginContainer` + `findPluginIdForClass`). Returns null if unavailable.
 */
internal fun Project.allAppliedPluginIdsOrNull(): List<String>? = try {
    val manager = pluginManager
    val getContainer = manager.javaClass.methods.firstOrNull {
        it.name == "getPluginContainer" && it.parameterCount == 0
    } ?: return null
    val findId = manager.javaClass.methods.firstOrNull {
        it.name == "findPluginIdForClass" && it.parameterCount == 1
    } ?: return null
    val container = getContainer.invoke(manager) as? Iterable<*> ?: return null
    container.mapNotNull { plugin ->
        if (plugin == null) return@mapNotNull null
        val optional = findId.invoke(manager, plugin.javaClass) ?: return@mapNotNull null
        val isPresent = optional.javaClass.getMethod("isPresent").invoke(optional) as Boolean
        if (!isPresent) return@mapNotNull null
        optional.javaClass.getMethod("get").invoke(optional)?.toString()
    }.distinct()
} catch (_: ReflectiveOperationException) {
    null
} catch (_: ClassCastException) {
    null
}

/** @return the "group:name" coordinates of every external dependency declared in this project. */
internal fun Project.externalDependencyCoordinates(): List<String> = runCatching {
    configurations.flatMap { configuration ->
        configuration.dependencies.filterIsInstance<ExternalModuleDependency>()
    }.map { dependency -> "${dependency.group}:${dependency.name}" }
        .distinct()
}.getOrElse { e ->
    println("Error resolving dependencies: ${e.message}")
    emptyList()
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
