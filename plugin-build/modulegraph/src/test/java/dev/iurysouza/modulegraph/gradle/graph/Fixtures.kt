package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.Digraph
import dev.iurysouza.modulegraph.LinkText

internal fun aDigraphInput(
    regex: String = ".*",
    showFullPath: Boolean = false,
) = DigraphInput(
    pattern = regex.toRegex(),
    dependencies = mapOf(
        ":sample:zeta" to listOf(
            Dependency(
                targetProjectPath = ":sample:beta",
                configName = "implementation"
            )
        ),
        ":sample:alpha" to listOf(
            Dependency(
                targetProjectPath = ":sample:zeta",
                configName = "implementation"
            ),
            Dependency(
                targetProjectPath = ":sample:beta",
                configName = "implementation"
            ),
            Dependency(
                targetProjectPath = ":sample:container:gama",
                configName = "implementation"
            ),
            Dependency(
                targetProjectPath = ":sample:container:delta",
                configName = "implementation"
            )
        ),
        ":sample:container:gama" to listOf(
            Dependency(
                targetProjectPath = ":sample:zeta",
                configName = "implementation"
            )
        )
    ),
    showFullPath = showFullPath,
)

internal val expectedDigraph = Digraph(
    focusedProjects = setOf(
        "zeta",
        "beta",
        "alpha",
        "gama",
        "delta"
    ),
    digraph = mapOf(
        "zeta" to setOf(
            "beta"
        ),
        "alpha" to setOf(
            "zeta",
            "beta",
            "gama",
            "delta"
        ),
        "gama" to setOf(
            "zeta"
        )
    ),
    mermaidStringSyntax = """
        |alpha --> gama
        |gama --> zeta""".trimMargin()
)
