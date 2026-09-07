package dev.iurysouza.modulegraph

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SnapshotAppliedPluginIdsTest {

    @Test
    fun `snapshot includes applied plugin ids so custom ModuleTypes can resolve`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")

        val custom = ModuleType.Custom("java", "#FF00FF")
        val pluginIds = project.snapshotAppliedPluginIds(customPlugins = listOf(custom))

        assertTrue(
            pluginIds.any { it == "java" || it == "org.gradle.java" },
            "expected java plugin id in $pluginIds",
        )

        val resolved = resolveModuleType(
            pluginIds = pluginIds,
            externalDependencies = emptyList(),
            customPlugins = listOf(custom),
        )
        assertEquals(custom.id, resolved.id)
        assertEquals(custom.color, resolved.color)
    }

    @Test
    fun `resolveModuleType matches custom plugin id from snapshot list`() {
        val custom = ModuleType.Custom("com.example.feature", "#ABCDEF")
        val resolved = resolveModuleType(
            pluginIds = listOf("com.example.feature", "java"),
            externalDependencies = emptyList(),
            customPlugins = listOf(custom),
        )
        assertEquals("com.example.feature", resolved.id)
    }
}
