package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.Dependency

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
        |%%{
        |  init: {
        |    'theme': 'base',
        |    'themeVariables': {"primaryTextColor":"#F6F8FAff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","tertiaryColor":"#40375c","lineColor":"#f5a623","fontSize":"12px"}
        |  }
        |}%%
        |
        |graph TB
        |  subgraph container
        |    gama
        |  end
        |  subgraph sample
        |    alpha
        |    zeta
        |  end
        |  alpha --> gama
        |  gama --> zeta
        |
        |classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
        |class gama focus""".trimMargin()

fun someGraphOptions(
    orientation: Orientation = Orientation.LEFT_TO_RIGHT,
    linkText: LinkText = LinkText.NONE,
    regex: Regex = ".*".toRegex(),
    showFullPath: Boolean = false,
    theme: Theme = Theme.NEUTRAL,
) = GraphOptions(
    linkText = linkText,
    orientation = orientation,
    pattern = regex,
    showFullPath = showFullPath,
    theme = theme,
)
