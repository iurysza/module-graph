package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.gradle.graphparser.ProjectParser
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProject
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/** Tests the 'rootModules' configuration option */
internal class ProjectParserRootModulesTest {
    private val theme = Theme.NEUTRAL

    private val entireGraph: Map<Module, List<Module>> = mapOf(
        ModuleToDeps.app,
        ModuleToDeps.featAUi,
        ModuleToDeps.commonComponent,
        ModuleToDeps.coreUi,
        ModuleToDeps.featAData,
        ModuleToDeps.commonData,
        ModuleToDeps.coreNetworking,
    )

    @Test
    fun `correct graph when root module is app`() {
        val expectedGraph = entireGraph
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjects = Project.all,
            rootModulesRegex = ProjectName.app,
            excludedConfigurations = null,
            excludedModules = null,
            theme = theme,
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
        )
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjects = Project.all,
            rootModulesRegex = "(${ProjectName.featAUi})|(${ProjectName.featAData})",
            excludedConfigurations = null,
            excludedModules = null,
            theme = theme,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root module is only featA UI`() {
        val expectedGraph = mapOf(
            ModuleToDeps.featAUi,
            ModuleToDeps.commonComponent,
            ModuleToDeps.coreUi,
        )
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjects = Project.all,
            rootModulesRegex = ProjectName.featAUi,
            excludedConfigurations = null,
            excludedModules = null,
            theme = theme,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root module is only featA data`() {
        val expectedGraph = mapOf(
            ModuleToDeps.featAData,
            ModuleToDeps.commonData,
            ModuleToDeps.coreNetworking,
        )
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjects = Project.all,
            rootModulesRegex = ProjectName.featAData,
            excludedConfigurations = null,
            excludedModules = null,
            theme = theme,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }

    @Test
    fun `correct graph when root modules not set`() {
        val expectedGraph = entireGraph
        val actualGraph = ProjectParser.parseProjectGraph(
            allProjects = Project.all,
            rootModulesRegex = null,
            excludedConfigurations = null,
            excludedModules = null,
            theme = theme,
        )

        Assertions.assertEquals(expectedGraph, actualGraph)
    }
}

private object Default {
    val moduleType = ModuleType.Java()
    val configName = "prod"
}

private object ProjectName {
    const val app = ":app"
    const val featAUi = ":feat1:ui"
    const val featAData = ":feat1:data"
    const val commonComponent = ":common:component"
    const val coreUi = ":core:ui"
    const val coreUtil = ":core:util"
    const val commonData = ":common:data"
    const val coreNetworking = ":core:networking"
}

private object Project {
    val coreUtil = createMockProject(
        ProjectName.coreUtil,
        emptyList(),
    )
    val coreNetworking = createMockProject(
        ProjectName.coreNetworking,
        listOf(coreUtil),
    )
    val commonData = createMockProject(
        ProjectName.commonData,
        listOf(coreNetworking, coreUtil),
    )
    val coreUi = createMockProject(
        ProjectName.coreUi,
        listOf(coreUtil),
    )
    val commonComponent = createMockProject(
        ProjectName.commonComponent,
        listOf(coreUi),
    )
    val featAUi = createMockProject(
        ProjectName.featAUi,
        listOf(commonComponent),
    )
    val featAData = createMockProject(
        ProjectName.featAData,
        listOf(commonData),
    )
    val app = createMockProject(
        ProjectName.app,
        listOf(
            featAUi,
            featAData,
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

    private fun createMockProject(name: String, dependencies: List<GradleProject>) =
        object : GradleProject {
            override val path = name

            override val configurations: List<GradleProjectConfiguration> = listOf(
                GradleProjectConfiguration(
                    name = Default.configName,
                    projects = dependencies,
                ),
            )

            override fun getModuleType(customModuleTypes: List<ModuleType>) = Default.moduleType
        }
}

private object ModuleToDeps {
    val app = createDefaultModuleSource(ProjectName.app) to listOf(
        createDefaultModuleTarget(ProjectName.featAUi),
        createDefaultModuleTarget(ProjectName.featAData),
    )
    val featAUi = createDefaultModuleSource(ProjectName.featAUi) to listOf(
        createDefaultModuleTarget(ProjectName.commonComponent),
    )
    val commonComponent = createDefaultModuleSource(ProjectName.commonComponent) to listOf(
        createDefaultModuleTarget(ProjectName.coreUi),
    )
    val coreUi = createDefaultModuleSource(ProjectName.coreUi) to listOf(
        createDefaultModuleTarget(ProjectName.coreUtil),
    )
    val featAData = createDefaultModuleSource(ProjectName.featAData) to listOf(
        createDefaultModuleTarget(ProjectName.commonData),
    )
    val commonData = createDefaultModuleSource(ProjectName.commonData) to listOf(
        createDefaultModuleTarget(ProjectName.coreNetworking),
        createDefaultModuleTarget(ProjectName.coreUtil),
    )
    val coreNetworking = createDefaultModuleSource(ProjectName.coreNetworking) to listOf(
        createDefaultModuleTarget(ProjectName.coreUtil),
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
