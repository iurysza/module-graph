package com.iurysouza.kotlin.gradle.template.plugin

import dev.iurysouza.modulegraph.CreateModuleGraphTask
import dev.iurysouza.modulegraph.ModuleGraphExtension
import dev.iurysouza.modulegraph.Theme
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

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
    fun `extension templateExampleConfig is created correctly`() {
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
            readmePath.set(aFilePath)
        }

        val task = project.tasks.getByName("createModuleGraph") as CreateModuleGraphTask

        assertEquals("### Dependency Diagram", task.heading.get())
        assertEquals(aFilePath, task.readmePath.get())
        assertEquals(Theme.NEUTRAL, task.theme.get())
    }
}
