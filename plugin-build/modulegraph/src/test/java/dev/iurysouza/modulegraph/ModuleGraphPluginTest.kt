package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.CreateModuleGraphTask
import dev.iurysouza.modulegraph.gradle.ModuleGraphExtension
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ModuleGraphPluginTest {

    private val pluginId = "dev.iurysouza.modulegraph"
    private val pluginExtension = "moduleGraphConfig"

    @Test
    fun `error when plugin is applied to the project without valid graph config`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        assertThrows<GradleException> {
            project.tasks.getByName("createModuleGraph")
        }
    }

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
            readmePath.set("README.md")
            heading.set("# Heading")
        }
        assert(project.tasks.getByName("createModuleGraph") is CreateModuleGraphTask)
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
    }
}
