package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.CreateModuleGraphTask
import dev.iurysouza.modulegraph.gradle.ModuleGraphExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ModuleGraphPluginTest {

    private val pluginId = "dev.iurysouza.modulegraph"
    private val pluginExtension = "moduleGraphConfig"

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        assert(project.tasks.getByName("createModuleGraph") is CreateModuleGraphTask)
    }

    @Test
    fun `extension moduleGraphConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        assertNotNull(project.extensions.getByName("moduleGraphConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(pluginId)
        val aFilePath = "${project.projectDir}/README.md"
        (project.extensions.getByName(pluginExtension) as ModuleGraphExtension).apply {
            heading.set("### Dependency Diagram")
            theme.set(Theme.NEUTRAL)
            showFullPath.set(true)
            linkText.set(LinkText.CONFIGURATION)
            orientation.set(Orientation.TOP_TO_BOTTOM)
            readmePath.set(aFilePath)
        }

        val task = project.tasks.getByName("createModuleGraph") as CreateModuleGraphTask

        assertEquals("### Dependency Diagram", task.heading.get())
        assertEquals(aFilePath, task.readmePath.get())
        assertEquals(LinkText.CONFIGURATION, task.linkText.get())
        assertEquals(true, task.showFullPath.get())
        assertEquals(Theme.NEUTRAL, task.theme.get())
        assertEquals(Orientation.TOP_TO_BOTTOM, task.orientation.get())
    }
}
