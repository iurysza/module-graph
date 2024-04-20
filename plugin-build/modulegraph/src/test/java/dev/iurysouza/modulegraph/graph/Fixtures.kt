package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.gradle.Module

internal fun aModuleGraph() = mapOf(
    Module(
        path = ":sample:zeta",
    ) to listOf(
        Module(
            path = ":sample:beta",
            configName = "implementation",
        ),
    ),
    Module(
        path = ":sample:alpha",
    ) to listOf(
        Module(
            path = ":sample:zeta",
            configName = "implementation",
        ),
        Module(
            path = ":sample:beta",
            configName = "implementation",
        ),
        Module(
            path = ":sample:container:gama",
            configName = "implementation",
        ),
        Module(
            path = ":sample:container:delta",
            configName = "implementation",
        ),
    ),
    Module(
        path = ":sample:container:gama",
    )
        to listOf(
            Module(
                path = ":sample:zeta",
                configName = "implementation",
            ),
        ),
)

internal val expectedMermaidGraphCode = """
        |```mermaid
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
        |class gama focus
        |```
""".trimMargin()

fun withGraphOptions(
    orientation: Orientation = Orientation.LEFT_TO_RIGHT,
    linkText: LinkText = LinkText.NONE,
    focusedModulesRegex: String? = null,
    showFullPath: Boolean = false,
    theme: Theme = Theme.NEUTRAL,
    setStyleByModuleType: Boolean = false,
) = GraphOptions(
    linkText = linkText,
    theme = theme,
    orientation = orientation,
    pattern = focusedModulesRegex?.toRegex(),
    showFullPath = showFullPath,
    setStyleByModuleType = setStyleByModuleType,
)

internal val liveMatchReconstructedModel = mapOf(
    Module(":core:footballinfo") to listOf(
        Module(
            path = ":core:common",
            configName = "implementation",
        ),
    ),
    Module(":features:match-day") to listOf(
        Module(
            path = ":core:common",
            configName = "implementation",
        ),
        Module(
            path = ":core:footballinfo",
            configName = "implementation",
        ),
        Module(
            path = ":core:design-system",
            configName = "implementation",
        ),
        Module(
            path = ":core:reddit",
            configName = "implementation",
        ),
    ),
    Module(":features:match-thread") to listOf(
        Module(
            path = ":core:webview-to-native-player",
            configName = "implementation",
        ),
        Module(
            path = ":core:common",
            configName = "implementation",
        ),
        Module(
            path = ":core:footballinfo",
            configName = "implementation",
        ),
        Module(
            path = ":core:design-system",
            configName = "implementation",
        ),
        Module(
            path = ":core:reddit",
            configName = "implementation",
        ),
    ),
    Module(":app:playground") to listOf(
        Module(
            path = ":core:webview-to-native-player",
            configName = "implementation",
        ),
        Module(
            path = ":features:match-thread",
            configName = "implementation",
        ),
        Module(
            path = ":core:design-system",
            configName = "implementation",
        ),
        Module(
            path = ":features:match-day",
            configName = "testImplementation",
        ),
    ),
    Module(":core:reddit") to listOf(
        Module(
            path = ":core:common",
            configName = "implementation",
        ),
    ),
    Module(":core:webview-to-native-player") to listOf(
        Module(
            path = ":core:common",
            configName = "implementation",
        ),
    ),
    Module(":app:main") to listOf(
        Module(
            path = ":features:match-thread",
            configName = "implementation",
        ),
        Module(
            path = ":features:match-day",
            configName = "implementation",
        ),
        Module(
            path = ":core:design-system",
            configName = "implementation",
        ),
        Module(
            path = ":core:common",
            configName = "implementation",
        ),
    ),
)

internal val expectedLiveMatchGraph = """
    |```mermaid
    |%%{
    |  init: {
    |    'theme': 'base',
    |    'themeVariables': {"primaryTextColor":"#fff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","lineColor":"#f5a623","tertiaryColor":"#40375c","fontSize":"12px"}
    |  }
    |}%%

    |graph LR
    |  subgraph app
    |    playground
    |    main
    |  end
    |  subgraph core
    |    footballinfo
    |    common
    |    design-system
    |    reddit
    |    webview-to-native-player
    |  end
    |  subgraph features
    |    match-day
    |    match-thread
    |  end
    |  footballinfo --> common
    |  match-day --> common
    |  match-day --> footballinfo
    |  match-day --> design-system
    |  match-day --> reddit
    |  match-thread --> webview-to-native-player
    |  match-thread --> common
    |  match-thread --> footballinfo
    |  match-thread --> design-system
    |  match-thread --> reddit
    |  playground --> webview-to-native-player
    |  playground --> match-thread
    |  playground --> design-system
    |  playground --> match-day
    |  reddit --> common
    |  webview-to-native-player --> common
    |  main --> match-thread
    |  main --> match-day
    |  main --> design-system
    |  main --> common
    |```
""".trimMargin()

internal val fullLiveMatchGraph = mapOf(
    Module(":core:footballinfo") to listOf(
        Module(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":core:footballinfo", "debugAndroidTestRuntimeClasspath"),
        Module(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":core:footballinfo", "debugUnitTestRuntimeClasspath"),
        Module(":core:common", "implementation"),
        Module(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":core:footballinfo", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":features:match-day") to listOf(
        Module(":features:match-day", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Module(":core:design-system", "debugAndroidTestCompileClasspath"),
        Module(":core:reddit", "debugAndroidTestCompileClasspath"),
        Module(":features:match-day", "debugAndroidTestRuntimeClasspath"),
        Module(":features:match-day", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Module(":core:design-system", "debugUnitTestCompileClasspath"),
        Module(":core:reddit", "debugUnitTestCompileClasspath"),
        Module(":features:match-day", "debugUnitTestRuntimeClasspath"),
        Module(":core:common", "implementation"),
        Module(":core:footballinfo", "implementation"),
        Module(":core:design-system", "implementation"),
        Module(":core:reddit", "implementation"),
        Module(":features:match-day", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Module(":core:design-system", "releaseUnitTestCompileClasspath"),
        Module(":core:reddit", "releaseUnitTestCompileClasspath"),
        Module(":features:match-day", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":features:match-day") to listOf(
        Module(":features:match-day", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Module(":core:design-system", "debugAndroidTestCompileClasspath"),
        Module(":core:reddit", "debugAndroidTestCompileClasspath"),
        Module(":features:match-day", "debugAndroidTestRuntimeClasspath"),
        Module(":features:match-day", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Module(":core:design-system", "debugUnitTestCompileClasspath"),
        Module(":core:reddit", "debugUnitTestCompileClasspath"),
        Module(":features:match-day", "debugUnitTestRuntimeClasspath"),
        Module(":core:common", "implementation"),
        Module(":core:footballinfo", "implementation"),
        Module(":core:design-system", "implementation"),
        Module(":core:reddit", "implementation"),
        Module(":features:match-day", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Module(":core:design-system", "releaseUnitTestCompileClasspath"),
        Module(":core:reddit", "releaseUnitTestCompileClasspath"),
        Module(":features:match-day", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":features:match-thread") to listOf(
        Module(":features:match-thread", "debugAndroidTestCompileClasspath"),
        Module(":core:webview-to-native-player", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Module(":core:design-system", "debugAndroidTestCompileClasspath"),
        Module(":core:reddit", "debugAndroidTestCompileClasspath"),
        Module(":features:match-thread", "debugAndroidTestRuntimeClasspath"),
        Module(":features:match-thread", "debugUnitTestCompileClasspath"),
        Module(":core:webview-to-native-player", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Module(":core:design-system", "debugUnitTestCompileClasspath"),
        Module(":core:reddit", "debugUnitTestCompileClasspath"),
        Module(":features:match-thread", "debugUnitTestRuntimeClasspath"),
        Module(":core:webview-to-native-player", "implementation"),
        Module(":core:common", "implementation"),
        Module(":core:footballinfo", "implementation"),
        Module(":core:design-system", "implementation"),
        Module(":core:reddit", "implementation"),
        Module(":features:match-thread", "releaseUnitTestCompileClasspath"),
        Module(":core:webview-to-native-player", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Module(":core:design-system", "releaseUnitTestCompileClasspath"),
        Module(":core:reddit", "releaseUnitTestCompileClasspath"),
        Module(":features:match-thread", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":app:playground") to listOf(
        Module(":app:playground", "debugAndroidTestCompileClasspath"),
        Module(":core:webview-to-native-player", "debugAndroidTestCompileClasspath"),
        Module(":features:match-thread", "debugAndroidTestCompileClasspath"),
        Module(":core:design-system", "debugAndroidTestCompileClasspath"),
        Module(":app:playground", "debugUnitTestCompileClasspath"),
        Module(":core:webview-to-native-player", "debugUnitTestCompileClasspath"),
        Module(":features:match-thread", "debugUnitTestCompileClasspath"),
        Module(":core:design-system", "debugUnitTestCompileClasspath"),
        Module(":app:playground", "debugUnitTestRuntimeClasspath"),
        Module(":core:webview-to-native-player", "implementation"),
        Module(":features:match-thread", "implementation"),
        Module(":core:design-system", "implementation"),
        Module(":app:playground", "releaseUnitTestCompileClasspath"),
        Module(":core:webview-to-native-player", "releaseUnitTestCompileClasspath"),
        Module(":features:match-thread", "releaseUnitTestCompileClasspath"),
        Module(":core:design-system", "releaseUnitTestCompileClasspath"),
        Module(":app:playground", "releaseUnitTestRuntimeClasspath"),
        Module(":features:match-day", "testImplementation"),
    ),
    Module(":core:reddit") to listOf(
        Module(":core:reddit", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":core:reddit", "debugAndroidTestRuntimeClasspath"),
        Module(":core:reddit", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":core:reddit", "debugUnitTestRuntimeClasspath"),
        Module(":core:common", "implementation"),
        Module(":core:reddit", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":core:reddit", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":core:webview-to-native-player") to listOf(
        Module(":core:webview-to-native-player", "debugAndroidTestCompileClasspath"),
        Module(":core:webview-to-native-player", "debugAndroidTestRuntimeClasspath"),
        Module(":core:webview-to-native-player", "debugUnitTestCompileClasspath"),
        Module(":core:webview-to-native-player", "debugUnitTestRuntimeClasspath"),
        Module(":core:common", "implementation"),
        Module(":core:webview-to-native-player", "releaseUnitTestCompileClasspath"),
        Module(":core:webview-to-native-player", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":app:main") to listOf(
        Module(":app:main", "debugAndroidTestCompileClasspath"),
        Module(":features:match-thread", "debugAndroidTestCompileClasspath"),
        Module(":features:match-day", "debugAndroidTestCompileClasspath"),
        Module(":core:design-system", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":app:main", "debugUnitTestCompileClasspath"),
        Module(":features:match-thread", "debugUnitTestCompileClasspath"),
        Module(":features:match-day", "debugUnitTestCompileClasspath"),
        Module(":core:design-system", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":app:main", "debugUnitTestRuntimeClasspath"),
        Module(":features:match-thread", "implementation"),
        Module(":features:match-day", "implementation"),
        Module(":core:design-system", "implementation"),
        Module(":core:common", "implementation"),
        Module(":app:main", "releaseUnitTestCompileClasspath"),
        Module(":features:match-thread", "releaseUnitTestCompileClasspath"),
        Module(":features:match-day", "releaseUnitTestCompileClasspath"),
        Module(":core:design-system", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":app:main", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":core:common") to listOf(
        Module(":core:common", "debugAndroidTestCompileClasspath"),
        Module(":core:common", "debugAndroidTestRuntimeClasspath"),
        Module(":core:common", "debugUnitTestCompileClasspath"),
        Module(":core:common", "debugUnitTestRuntimeClasspath"),
        Module(":core:common", "releaseUnitTestCompileClasspath"),
        Module(":core:common", "releaseUnitTestRuntimeClasspath"),
    ),
    Module(":core:design-system") to listOf(
        Module(":core:design-system", "debugAndroidTestCompileClasspath"),
        Module(":core:design-system", "debugAndroidTestRuntimeClasspath"),
        Module(":core:design-system", "debugUnitTestCompileClasspath"),
        Module(":core:design-system", "debugUnitTestRuntimeClasspath"),
        Module(":core:design-system", "releaseUnitTestCompileClasspath"),
        Module(":core:design-system", "releaseUnitTestRuntimeClasspath"),
    ),
)
internal val liveMatchMermaidGraphWithConfigurations = """
            |  footballinfo -- debugAndroidTestCompileClasspath --> common
            |  footballinfo -- debugUnitTestCompileClasspath --> common
            |  footballinfo -- implementation --> common
            |  footballinfo -- releaseUnitTestCompileClasspath --> common
            |  match-day -- debugAndroidTestCompileClasspath --> common
            |  match-day -- debugAndroidTestCompileClasspath --> footballinfo
            |  match-day -- debugAndroidTestCompileClasspath --> design-system
            |  match-day -- debugAndroidTestCompileClasspath --> reddit
            |  match-day -- debugUnitTestCompileClasspath --> common
            |  match-day -- debugUnitTestCompileClasspath --> footballinfo
            |  match-day -- debugUnitTestCompileClasspath --> design-system
            |  match-day -- debugUnitTestCompileClasspath --> reddit
            |  match-day -- implementation --> common
            |  match-day -- implementation --> footballinfo
            |  match-day -- implementation --> design-system
            |  match-day -- implementation --> reddit
            |  match-day -- releaseUnitTestCompileClasspath --> common
            |  match-day -- releaseUnitTestCompileClasspath --> footballinfo
            |  match-day -- releaseUnitTestCompileClasspath --> design-system
            |  match-day -- releaseUnitTestCompileClasspath --> reddit
            |  match-thread -- debugAndroidTestCompileClasspath --> webview-to-native-player
            |  match-thread -- debugAndroidTestCompileClasspath --> common
            |  match-thread -- debugAndroidTestCompileClasspath --> footballinfo
            |  match-thread -- debugAndroidTestCompileClasspath --> design-system
            |  match-thread -- debugAndroidTestCompileClasspath --> reddit
            |  match-thread -- debugUnitTestCompileClasspath --> webview-to-native-player
            |  match-thread -- debugUnitTestCompileClasspath --> common
            |  match-thread -- debugUnitTestCompileClasspath --> footballinfo
            |  match-thread -- debugUnitTestCompileClasspath --> design-system
            |  match-thread -- debugUnitTestCompileClasspath --> reddit
            |  match-thread -- implementation --> webview-to-native-player
            |  match-thread -- implementation --> common
            |  match-thread -- implementation --> footballinfo
            |  match-thread -- implementation --> design-system
            |  match-thread -- implementation --> reddit
            |  match-thread -- releaseUnitTestCompileClasspath --> webview-to-native-player
            |  match-thread -- releaseUnitTestCompileClasspath --> common
            |  match-thread -- releaseUnitTestCompileClasspath --> footballinfo
            |  match-thread -- releaseUnitTestCompileClasspath --> design-system
            |  match-thread -- releaseUnitTestCompileClasspath --> reddit
            |  playground -- debugAndroidTestCompileClasspath --> webview-to-native-player
            |  playground -- debugAndroidTestCompileClasspath --> match-thread
            |  playground -- debugAndroidTestCompileClasspath --> design-system
            |  playground -- debugUnitTestCompileClasspath --> webview-to-native-player
            |  playground -- debugUnitTestCompileClasspath --> match-thread
            |  playground -- debugUnitTestCompileClasspath --> design-system
            |  playground -- implementation --> webview-to-native-player
            |  playground -- implementation --> match-thread
            |  playground -- implementation --> design-system
            |  playground -- releaseUnitTestCompileClasspath --> webview-to-native-player
            |  playground -- releaseUnitTestCompileClasspath --> match-thread
            |  playground -- releaseUnitTestCompileClasspath --> design-system
            |  playground -- testImplementation --> match-day
            |  reddit -- debugAndroidTestCompileClasspath --> common
            |  reddit -- debugUnitTestCompileClasspath --> common
            |  reddit -- implementation --> common
            |  reddit -- releaseUnitTestCompileClasspath --> common
            |  webview-to-native-player -- implementation --> common
            |  main -- debugAndroidTestCompileClasspath --> match-thread
            |  main -- debugAndroidTestCompileClasspath --> match-day
            |  main -- debugAndroidTestCompileClasspath --> design-system
            |  main -- debugAndroidTestCompileClasspath --> common
            |  main -- debugUnitTestCompileClasspath --> match-thread
            |  main -- debugUnitTestCompileClasspath --> match-day
            |  main -- debugUnitTestCompileClasspath --> design-system
            |  main -- debugUnitTestCompileClasspath --> common
            |  main -- implementation --> match-thread
            |  main -- implementation --> match-day
            |  main -- implementation --> design-system
            |  main -- implementation --> common
            |  main -- releaseUnitTestCompileClasspath --> match-thread
            |  main -- releaseUnitTestCompileClasspath --> match-day
            |  main -- releaseUnitTestCompileClasspath --> design-system
            |  main -- releaseUnitTestCompileClasspath --> common
""".trimMargin()
