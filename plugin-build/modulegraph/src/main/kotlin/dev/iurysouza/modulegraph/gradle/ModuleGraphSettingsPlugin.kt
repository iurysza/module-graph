package dev.iurysouza.modulegraph.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings

private const val CONTRIBUTE_TASK = "moduleGraphContribute"

/**
 * Settings plugin that wires the module graph for the whole build.
 *
 * Apply it in `settings.gradle(.kts)`:
 * ```
 * plugins { id("dev.iurysouza.modulegraph.settings") }
 * ```
 *
 * It applies [ModuleGraphPlugin] to the root project and registers a [ModuleGraphContributeTask] on
 * every other project, which the root `createModuleGraph` task depends on.
 */
open class ModuleGraphSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.gradle.sharedServices.registerIfAbsent(
            ModuleGraphRegistry.NAME,
            ModuleGraphRegistry::class.java,
        ) {}

        val contributingPaths = mutableListOf<String>()

        settings.gradle.settingsEvaluated { evaluated ->
            contributingPaths += evaluated.rootProject.children.flatMap { it.allPaths() }
        }

        @Suppress("UnstableApiUsage")
        settings.gradle.lifecycle.beforeProject { project ->
            if (project == project.rootProject) {
                project.pluginManager.apply(ModuleGraphPlugin::class.java)
                project.tasks.named("createModuleGraph").configure { task ->
                    task.dependsOn(contributingPaths.map { "$it:$CONTRIBUTE_TASK" })
                }
            } else {
                project.tasks.register(
                    ModuleGraphContributeTask.NAME,
                    ModuleGraphContributeTask::class.java,
                ) { task ->
                    task.projectInfo.set(project.provider { project.collectProjectInfo() })
                }
            }
        }
    }

    private fun ProjectDescriptor.allPaths(): List<String> =
        listOf(path) + children.flatMap { it.allPaths() }
}
