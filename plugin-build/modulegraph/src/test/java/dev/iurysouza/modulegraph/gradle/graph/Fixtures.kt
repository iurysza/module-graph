package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.graph.DigraphInput

internal fun aDigraphInput(
    regex: String = ".*",
    showFullPath: Boolean = false,
) = DigraphInput(
    pattern = regex.toRegex(),
    dependencies = aModuleGraph(),
    showFullPath = showFullPath,
)

internal fun aModuleGraph() = mapOf(
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
)

internal val expectedMermaidGraphCode = """
        |```mermaid
        |%%{
        |  init: {
        |    'theme': 'base',
        |	'themeVariables': {"primaryTextColor":"#F6F8FAff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","tertiaryColor":"#40375c","lineColor":"#f5a623","fontSize":"12px"}
        |  }
        |}%%
        |
        |graph TB
        |  subgraph sample
        |    alpha
        |    zeta
        |  end
        |  subgraph container
        |    gama
        |  end
        |  alpha --> gama
        |  gama --> zeta
        |
        |classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
        |class gama focus
        |```""".trimMargin()
