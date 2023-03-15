package com.iurysouza.kotlin.gradle.template.plugin

import dev.iurysouza.modulegraph.plugin.CreateModuleGraphTask
import dev.iurysouza.modulegraph.plugin.ModuleGraphExtension
import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ModuleGraphPluginTest {

    private val pluginId = "dev.iurysouza.modulegraph.plugin"
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
        val aFile = File(project.projectDir, "README.md")
        (project.extensions.getByName(pluginExtension) as ModuleGraphExtension).apply {
            heading.set("### Dependency Diagram")
            readmeFile.set(aFile)
        }

        val task = project.tasks.getByName("createModuleGraph") as CreateModuleGraphTask

        assertEquals("### Dependency Diagram", task.heading.get())
        assertEquals(aFile, task.readmeFile.get())
    }
}
