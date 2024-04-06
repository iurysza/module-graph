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

internal val liveMatchReconstructedModel = mapOf(
    ":core:footballinfo" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation"
        )
    ),
    ":features:match-day" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:footballinfo",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:reddit",
            configName = "implementation"
        )
    ),
    ":features:match-thread" to listOf(
        Dependency(
            targetProjectPath = ":core:webview-to-native-player",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:footballinfo",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:reddit",
            configName = "implementation"
        )
    ),
    ":app:playground" to listOf(
        Dependency(
            targetProjectPath = ":core:webview-to-native-player",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":features:match-thread",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":features:match-day",
            configName = "testImplementation"
        )
    ),
    ":core:reddit" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation"
        )
    ),
    ":core:webview-to-native-player" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation"
        )
    ),
    ":app:main" to listOf(
        Dependency(
            targetProjectPath = ":features:match-thread",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":features:match-day",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation"
        ),
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation"
        )
    )
)

internal val expectedLiveMatchGraph = """
    %%{
      init: {
        'theme': 'base',
        'themeVariables': {"primaryTextColor":"#fff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","lineColor":"#f5a623","tertiaryColor":"#40375c","fontSize":"12px"}
      }
    }%%

    graph LR
      subgraph app
        playground
        main
      end
      subgraph core
        footballinfo
        common
        design-system
        reddit
        webview-to-native-player
      end
      subgraph features
        match-day
        match-thread
      end
      footballinfo --> common
      match-day --> common
      match-day --> footballinfo
      match-day --> design-system
      match-day --> reddit
      match-thread --> webview-to-native-player
      match-thread --> common
      match-thread --> footballinfo
      match-thread --> design-system
      match-thread --> reddit
      playground --> webview-to-native-player
      playground --> match-thread
      playground --> design-system
      playground --> match-day
      reddit --> common
      webview-to-native-player --> common
      main --> match-thread
      main --> match-day
      main --> design-system
      main --> common
""".trimIndent()
