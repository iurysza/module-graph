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

        return if (rootModuleInclusionPattern == null) {
            parseAllProjects(
                allProjects = allProjects,
                customModuleTypes = customModuleTypes,
                configExclusionPattern = configExclusionPattern,
                moduleExclusionPattern = moduleExclusionPattern,
            )
        } else {
            val rootModules =
                allProjects.filter { rootModuleInclusionPattern.matches(it.path) }
            require(rootModules.isNotEmpty()) {
                "The graph cannot be generated as no modules match the rootModules pattern: $rootModulesRegex"
            }
            parseFromRoots(
                rootModules = rootModules,
                customModuleTypes = customModuleTypes,
                configExclusionPattern = configExclusionPattern,
                moduleExclusionPattern = moduleExclusionPattern,
            )
        }
    }

    /** @return the modules which are direct dependencies in [config] */
    private fun getDirectDependencies(
        config: GradleProjectConfiguration,
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
    ) = config.projects
        .filterNot { configExclusionPattern.matches(config.name) }
        .filterNot { moduleExclusionPattern.matches(it.path) }

    /** The simple parse - just iterate over all modules */
    private fun parseAllProjects(
        allProjects: List<GradleProject>,
        customModuleTypes: List<ModuleType>,
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
    ): ProjectGraph {
        val projectGraph = hashMapOf<Module, List<Module>>()
        allProjects
            .asSequence()
            .filterNot { moduleExclusionPattern.matches(it.path) }
            .forEach { sourceProject ->
                sourceProject.configurations.forEach { config ->
                    getDirectDependencies(
                        config = config,
                        configExclusionPattern = configExclusionPattern,
                        moduleExclusionPattern = moduleExclusionPattern,
                    ).forEach { targetProject ->
                        registerDependency(
                            sourceProject = sourceProject,
                            targetProject = targetProject,
                            projectGraph = projectGraph,
                            config = config,
                            customModuleTypes = customModuleTypes,
                        )
                    }
                }
            }
        return projectGraph
    }

    /**
     * To handle root modules we need to parse the dependency tree recursively,
     * starting at the root module nodes.
     * This ensures we only include module nodes that are reachable from the root.
     */
    private fun parseFromRoots(
        rootModules: List<GradleProject>,
        customModuleTypes: List<ModuleType>,
        configExclusionPattern: RegexMatcher?,
        moduleExclusionPattern: RegexMatcher?,
    ): ProjectGraph {
        val projectGraph = hashMapOf<Module, List<Module>>()

        fun parseModuleDeps(sourceProject: GradleProject) {
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
