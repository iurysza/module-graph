package dev.iurysouza.modulegraph.gradle.graphparser

import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.gradle.RegexMatcher
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectPath
import dev.iurysouza.modulegraph.gradle.graphparser.projectquerier.ProjectQuerier
import dev.iurysouza.modulegraph.gradle.matches
import dev.iurysouza.modulegraph.model.GraphConfig
import dev.iurysouza.modulegraph.model.alias.ProjectGraph

internal object ProjectParser {
    internal fun parseProjectGraph(
        allProjectPaths: List<ProjectPath>,
        config: GraphConfig,
        projectQuerier: ProjectQuerier,
    ): ProjectGraph {
        val configExclusionPattern = config.excludedConfigurationsRegex?.let { RegexMatcher(it) }
        val moduleExclusionPattern = config.excludedModulesRegex?.let { RegexMatcher(it) }
        val rootModuleInclusionPattern = config.rootModulesRegex?.let { RegexMatcher(it) }

        val theme = config.theme
        val customModuleTypes = when (theme) {
            is Theme.BASE -> theme.moduleTypes
            else -> emptyList()
        }

        val rootModules = if (rootModuleInclusionPattern == null) {
            allProjectPaths
        } else {
            allProjectPaths.filter { rootModuleInclusionPattern.matches(it) }
        }
        require(rootModules.isNotEmpty()) {
            "The graph cannot be generated as no rootModules were found"
        }
        return parseFromRoots(
            rootModulePaths = rootModules,
            customModuleTypes = customModuleTypes,
            configExclusionPattern = configExclusionPattern,
            moduleExclusionPattern = moduleExclusionPattern,
            projectQuerier = projectQuerier,
        )
    }

    /** @return the modules which are direct dependencies */
    private fun GradleProjectConfiguration.getDirectDependencies(
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
    ): List<String> {
        return if (configExclusionPattern.matches(name)) {
            emptyList()
        } else {
            projectPaths.filterNot { moduleExclusionPattern.matches(it) }
        }
    }

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
        rootModulePaths: List<ProjectPath>,
        customModuleTypes: List<ModuleType>,
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
        projectQuerier: ProjectQuerier,
    ): ProjectGraph {
        val projectGraph = hashMapOf<Module, List<Module>>()
        val projectPathsParsed = hashSetOf<ProjectPath>()

        fun parseModuleDeps(sourceProjectPath: ProjectPath) {
            // Don't parse projects more than once -
            // it's a waste of time, and it might lead to infinite recursion
            if (sourceProjectPath in projectPathsParsed) return
            projectPathsParsed.add(sourceProjectPath)

            projectQuerier.getConfigurations(sourceProjectPath).forEach { config ->
                val deps = config.getDirectDependencies(
                    configExclusionPattern = configExclusionPattern,
                    moduleExclusionPattern = moduleExclusionPattern,
                )
                deps.forEach { targetProject ->
                    registerDependency(
                        sourceProjectPath = sourceProjectPath,
                        targetProjectPath = targetProject,
                        projectGraph = projectGraph,
                        configName = config.name,
                        customModuleTypes = customModuleTypes,
                        projectQuerier = projectQuerier,
                    )
                    parseModuleDeps(targetProject)
                }
            }
        }

        rootModulePaths.forEach { rootModule ->
            parseModuleDeps(rootModule)
        }
        return projectGraph
    }

    /** Registers the dependency from [sourceProjectPath] to [targetProjectPath] in [projectGraph] */
    private fun registerDependency(
        sourceProjectPath: ProjectPath,
        targetProjectPath: ProjectPath,
        projectGraph: MutableMap<Module, List<Module>>,
        configName: String,
        customModuleTypes: List<ModuleType>,
        projectQuerier: ProjectQuerier,
    ) {
        val sourceModule = Module(
            path = sourceProjectPath,
            type = projectQuerier.getProjectType(sourceProjectPath, customModuleTypes),
        )
        projectGraph[sourceModule] = projectGraph.getOrDefault(sourceModule, emptyList())
            .plus(
                Module(
                    path = targetProjectPath,
                    configName = configName,
                    type = projectQuerier.getProjectType(targetProjectPath, customModuleTypes),
                ),
            )
    }
}
