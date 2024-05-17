@file:Suppress("ktlint:standard:property-naming")

package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.gradle.graphparser.ProjectParser
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectPath
import dev.iurysouza.modulegraph.gradle.graphparser.projectquerier.ProjectQuerier
import dev.iurysouza.modulegraph.graph.getConfig
import dev.iurysouza.modulegraph.model.alias.ProjectGraph
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/** Tests the 'rootModulesRegex' configuration option */
internal class ProjectParserRootModulesTest {
    private val theme = Theme.NEUTRAL

    private val entireGraph: ProjectGraph = mapOf(
        ModuleToDeps.app,
        ModuleToDeps.featAUi,
        ModuleToDeps.commonComponent,
        ModuleToDeps.coreUi,
        ModuleToDeps.featAData,
        ModuleToDeps.commonData,
        ModuleToDeps.coreNetworking,
        ModuleToDeps.coreUtil,
    )

    private val projectQuerier = object : ProjectQuerier {
        override fun getProjectType(
            projectPath: ProjectPath,
            customModuleTypes: List<ModuleType>,
        ): ModuleType = Default.moduleType

        override fun getConfigurations(projectPath: ProjectPath): List<GradleProjectConfiguration> {
            val projectAndDeps = Project.all.first { it.path == projectPath }
            val config = GradleProjectConfiguration(
                name = Default.configName,
                projectPaths = projectAndDeps.deps,
            )
            return listOf(config)
        }
    }

    @Test
    fun `correct graph when root module is app`() {
        val expectedGraph = entireGraph
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjectPaths = Project.allPaths,
            config = getConfig(rootModulesRegex = MockProjectPath.app, theme = theme),
            projectQuerier = projectQuerier,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root module is both featA UI and data separately`() {
        val expectedGraph = mapOf(
            ModuleToDeps.featAUi,
            ModuleToDeps.commonComponent,
            ModuleToDeps.coreUi,
            ModuleToDeps.featAData,
            ModuleToDeps.commonData,
            ModuleToDeps.coreNetworking,
            ModuleToDeps.coreUtil,
        )
        val rootModules = "(${MockProjectPath.featAUi})|(${MockProjectPath.featAData})"
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjectPaths = Project.allPaths,
            config = getConfig(rootModulesRegex = rootModules, theme = theme),
            projectQuerier = projectQuerier,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root module is only featA UI`() {
        val expectedGraph = mapOf(
            ModuleToDeps.featAUi,
            ModuleToDeps.commonComponent,
            ModuleToDeps.coreUi,
            ModuleToDeps.coreUtil,
            ModuleToDeps.coreNetworking,
        )
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjectPaths = Project.allPaths,
            config = getConfig(rootModulesRegex = MockProjectPath.featAUi, theme = theme),
            projectQuerier = projectQuerier,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root module is only featA data`() {
        val expectedGraph = mapOf(
            ModuleToDeps.featAData,
            ModuleToDeps.commonData,
            ModuleToDeps.coreNetworking,
            ModuleToDeps.coreUtil,
        )
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjectPaths = Project.allPaths,
            config = getConfig(rootModulesRegex = MockProjectPath.featAData, theme = theme),
            projectQuerier = projectQuerier,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root modules not set`() {
        val expectedGraph = entireGraph
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjectPaths = Project.allPaths,
            config = getConfig(rootModulesRegex = null, theme = theme),
            projectQuerier = projectQuerier,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }
}

private object Default {
    val moduleType = ModuleType.Java()
    const val configName = "prod"
}

private object MockProjectPath {
    const val app = ":app"
    const val featAUi = ":feat1:ui"
    const val featAData = ":feat1:data"
    const val commonComponent = ":common:component"
    const val coreUi = ":core:ui"
    const val coreUtil = ":core:util"
    const val commonData = ":common:data"
    const val coreNetworking = ":core:networking"
}

private data class ProjectAndDeps(val path: ProjectPath, val deps: List<ProjectPath>)

private object Project {
    val coreUtil = ProjectAndDeps(
        MockProjectPath.coreUtil,
        // We create a circular dependency between util and networking
        // In a real Gradle project this is illegal, but we should still be able to render it.
        listOf(MockProjectPath.coreNetworking),
    )
    val coreNetworking = ProjectAndDeps(
        MockProjectPath.coreNetworking,
        listOf(MockProjectPath.coreUtil),
    )
    val commonData = ProjectAndDeps(
        MockProjectPath.commonData,
        listOf(MockProjectPath.coreNetworking, MockProjectPath.coreUtil),
    )
    val coreUi = ProjectAndDeps(
        MockProjectPath.coreUi,
        listOf(MockProjectPath.coreUtil),
    )
    val commonComponent = ProjectAndDeps(
        MockProjectPath.commonComponent,
        listOf(MockProjectPath.coreUi),
    )
    val featAUi = ProjectAndDeps(
        MockProjectPath.featAUi,
        listOf(MockProjectPath.commonComponent),
    )
    val featAData = ProjectAndDeps(
        MockProjectPath.featAData,
        listOf(MockProjectPath.commonData),
    )
    val app = ProjectAndDeps(
        MockProjectPath.app,
        listOf(
            MockProjectPath.featAUi,
            MockProjectPath.featAData,
        ),
    )

    val all = listOf(
        coreUtil,
        coreNetworking,
        commonData,
        coreUi,
        commonComponent,
        featAUi,
        featAData,
        app,
    )

    val allPaths = all.map { it.path }
}

private object ModuleToDeps {
    val app = createDefaultModuleSource(MockProjectPath.app) to listOf(
        createDefaultModuleTarget(MockProjectPath.featAUi),
        createDefaultModuleTarget(MockProjectPath.featAData),
    )
    val featAUi = createDefaultModuleSource(MockProjectPath.featAUi) to listOf(
        createDefaultModuleTarget(MockProjectPath.commonComponent),
    )
    val commonComponent = createDefaultModuleSource(MockProjectPath.commonComponent) to listOf(
        createDefaultModuleTarget(MockProjectPath.coreUi),
    )
    val coreUi = createDefaultModuleSource(MockProjectPath.coreUi) to listOf(
        createDefaultModuleTarget(MockProjectPath.coreUtil),
    )
    val featAData = createDefaultModuleSource(MockProjectPath.featAData) to listOf(
        createDefaultModuleTarget(MockProjectPath.commonData),
    )
    val commonData = createDefaultModuleSource(MockProjectPath.commonData) to listOf(
        createDefaultModuleTarget(MockProjectPath.coreNetworking),
        createDefaultModuleTarget(MockProjectPath.coreUtil),
    )
    val coreNetworking = createDefaultModuleSource(MockProjectPath.coreNetworking) to listOf(
        createDefaultModuleTarget(MockProjectPath.coreUtil),
    )
    val coreUtil = createDefaultModuleSource(MockProjectPath.coreUtil) to listOf(
        createDefaultModuleTarget(MockProjectPath.coreNetworking),
    )

    private fun createDefaultModuleSource(path: String) = Module(
        path = path,
        type = Default.moduleType,
        configName = null,
    )

    private fun createDefaultModuleTarget(path: String) = Module(
        path = path,
        type = Default.moduleType,
        configName = Default.configName,
    )
}
