package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.CreateModuleGraphTask
import dev.iurysouza.modulegraph.gradle.ModuleGraphExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModuleGraphPluginTest {

    private val pluginId = "dev.iurysouza.modulegraph"
    private val pluginExtension = "moduleGraphConfig"

    @Test
    fun `extension moduleGraphConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        assertNotNull(project.extensions.getByName(pluginExtension))
    }

    @Test
    fun `plugin is correctly applied to the project with minimal valid graph config`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        (project.extensions.getByName(pluginExtension) as ModuleGraphExtension).apply {
            readmePath.set("${project.projectDir}/README.md")
            heading.set("# Module Graph")
        }
        assert(project.tasks.getByName("createModuleGraph") is CreateModuleGraphTask)
    }

    @Test
    fun `graph-only config does not resolve a primary graph`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        (project.extensions.getByName(pluginExtension) as ModuleGraphExtension).apply {
            graph(
                readmePath = "${project.projectDir}/docs/GRAPH.md",
                heading = "## Custom Graph",
            )
        }
        val task = project.tasks.getByName("createModuleGraph") as CreateModuleGraphTask
        val configs = task.graphConfigsResolved.get()
        assertEquals(1, configs.size)
        assertEquals("## Custom Graph", configs.single().heading)
        assertTrue(configs.single().readmePath.endsWith("docs/GRAPH.md"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        val aFilePath = "${project.projectDir}/README.md"
        (project.extensions.getByName(pluginExtension) as ModuleGraphExtension).apply {
            readmePath.set(aFilePath)
            heading.set("### Dependency Diagram")

            theme.set(Theme.NEUTRAL)
            showFullPath.set(true)
            linkText.set(LinkText.CONFIGURATION)
            orientation.set(Orientation.TOP_TO_BOTTOM)
            excludedConfigurationsRegex.set("implementation")
            excludedModulesRegex.set("project")
            focusedModulesRegex.set(".*test.*")
            rootModulesRegex.set(".*")
            includeIsolatedModules.set(true)
        }

        val task = project.tasks.getByName("createModuleGraph") as CreateModuleGraphTask

        assertEquals("### Dependency Diagram", task.heading.get())
        assertEquals(aFilePath, task.readmePath.get())
        assertEquals(LinkText.CONFIGURATION, task.linkText.get())
        assertEquals(true, task.showFullPath.get())
        assertEquals(Theme.NEUTRAL, task.theme.get())
        assertEquals(Orientation.TOP_TO_BOTTOM, task.orientation.get())
        assertEquals(".*test.*", task.focusedModulesRegex.get())
        assertEquals("implementation", task.excludedConfigurationsRegex.get())
        assertEquals("project", task.excludedModulesRegex.get())
        assertEquals(".*", task.rootModulesRegex.get())
        assertEquals(true, task.includeIsolatedModules.get())
    }

    @Test
    fun `createModuleGraph declares readme files as outputs not the project directory`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        val readmeRelPath = "docs/MODULE_GRAPH.md"
        (project.extensions.getByName(pluginExtension) as ModuleGraphExtension).apply {
            readmePath.set(readmeRelPath)
            heading.set("# Module Graph")
        }

        val task = project.tasks.getByName("createModuleGraph") as CreateModuleGraphTask
        val declaredOutputs = task.outputs.files.files

        assertTrue(
            declaredOutputs.any { it == project.projectDir.resolve(readmeRelPath) },
            "Expected readme path among outputs, got: $declaredOutputs",
        )
        assertFalse(
            declaredOutputs.contains(project.projectDir),
            "projectDirectory must not be declared as an output",
        )
    }
}
