package dev.iurysouza.modulegraph.gradle.graphparser

import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.gradle.RegexMatcher
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProject
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.matches

internal object ProjectParser {
    internal fun parseProjectGraph(
        allProjects: List<GradleProject>,
        rootModulesRegex: String?,
        excludedConfigurations: String?,
        excludedModules: String?,
        theme: Theme,
    ): ProjectGraph {
        val configExclusionPattern = excludedConfigurations?.let { RegexMatcher(it) }
        val moduleExclusionPattern = excludedModules?.let { RegexMatcher(it) }
        val rootModuleInclusionPattern = rootModulesRegex?.let { RegexMatcher(it) }

        val customModuleTypes = when (theme) {
            is Theme.BASE -> theme.moduleTypes
            else -> emptyList()
        }

        val rootModules = if (rootModuleInclusionPattern == null) allProjects else {
            allProjects.filter { rootModuleInclusionPattern.matches(it.path) }
        }
        require(rootModules.isNotEmpty()) {
            "The graph cannot be generated as no rootModules were found"
        }
        return parseFromRoots(
            rootModules = rootModules,
            customModuleTypes = customModuleTypes,
            configExclusionPattern = configExclusionPattern,
            moduleExclusionPattern = moduleExclusionPattern,
        )
    }

    /** @return the modules which are direct dependencies in [config] */
    private fun getDirectDependencies(
        config: GradleProjectConfiguration,
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
    ) = config.projects
        .filterNot { configExclusionPattern.matches(config.name) }
        .filterNot { moduleExclusionPattern.matches(it.path) }

    /**
     * To handle root modules we need to parse the dependency tree recursively,
     * starting at the root module nodes.
     * This ensures we only include module nodes that are reachable from the root.
     *
     * It can happen that projects have circular dependencies.
     * Gradle will refuse to build such a project, but we can still render it in a graph -
     * in fact, such a graph can be useful to catch circular dependencies.
     * We need to handle these cases to avoid infinite recursion.
     */
    private fun parseFromRoots(
        rootModules: List<GradleProject>,
        customModuleTypes: List<ModuleType>,
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
    ): ProjectGraph {
        val projectGraph = hashMapOf<Module, List<Module>>()
        val projectsParsed = mutableListOf<GradleProject>()

        fun parseModuleDeps(sourceProject: GradleProject) {
            // Don't parse projects more than once -
            // it's a waste of time, and it might lead to infinite recursion
            if (sourceProject in projectsParsed) return
            projectsParsed.add(sourceProject)

            sourceProject.configurations.forEach { config ->
                val deps = getDirectDependencies(
                    config = config,
                    configExclusionPattern = configExclusionPattern,
                    moduleExclusionPattern = moduleExclusionPattern,
                )
                deps.forEach { targetProject ->
                    registerDependency(
                        sourceProject = sourceProject,
                        targetProject = targetProject,
                        projectGraph = projectGraph,
                        config = config,
                        customModuleTypes = customModuleTypes,
                    )
                    parseModuleDeps(targetProject)
                }
            }
        }

        rootModules.forEach { rootModule ->
            parseModuleDeps(rootModule)
        }
        return projectGraph
    }

    /** Registers the dependency from [sourceProject] to [targetProject] in [projectGraph] */
    private fun registerDependency(
        sourceProject: GradleProject,
        targetProject: GradleProject,
        projectGraph: MutableMap<Module, List<Module>>,
        config: GradleProjectConfiguration,
        customModuleTypes: List<ModuleType>,
    ) {
        val sourceModule = Module(
            path = sourceProject.path,
            type = sourceProject.getModuleType(customModuleTypes),
        )
        projectGraph[sourceModule] = projectGraph.getOrDefault(sourceModule, emptyList())
            .plus(
                Module(
                    path = targetProject.path,
                    configName = config.name,
                    type = targetProject.getModuleType(customModuleTypes),
                ),
            )
    }
}

private typealias ProjectGraph = Map<Module, List<Module>>
