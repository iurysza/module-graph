package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.Dependency

internal fun aModuleGraph() = mapOf(
    ":sample:zeta" to listOf(
        Dependency(
            targetProjectPath = ":sample:beta",
            configName = "implementation",
        ),
    ),
    ":sample:alpha" to listOf(
        Dependency(
            targetProjectPath = ":sample:zeta",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":sample:beta",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":sample:container:gama",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":sample:container:delta",
            configName = "implementation",
        ),
    ),
    ":sample:container:gama" to listOf(
        Dependency(
            targetProjectPath = ":sample:zeta",
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
            configName = "implementation",
        ),
    ),
    ":features:match-day" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:footballinfo",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:reddit",
            configName = "implementation",
        ),
    ),
    ":features:match-thread" to listOf(
        Dependency(
            targetProjectPath = ":core:webview-to-native-player",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:footballinfo",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:reddit",
            configName = "implementation",
        ),
    ),
    ":app:playground" to listOf(
        Dependency(
            targetProjectPath = ":core:webview-to-native-player",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":features:match-thread",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":features:match-day",
            configName = "testImplementation",
        ),
    ),
    ":core:reddit" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation",
        ),
    ),
    ":core:webview-to-native-player" to listOf(
        Dependency(
            targetProjectPath = ":core:common",
            configName = "implementation",
        ),
    ),
    ":app:main" to listOf(
        Dependency(
            targetProjectPath = ":features:match-thread",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":features:match-day",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:design-system",
            configName = "implementation",
        ),
        Dependency(
            targetProjectPath = ":core:common",
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

internal val fullLiveMatchGraph: Map<String, List<Dependency>> = mapOf(
    ":core:footballinfo" to listOf(
        Dependency(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Dependency(":core:common", "debugAndroidTestCompileClasspath"),
        Dependency(":core:footballinfo", "debugAndroidTestRuntimeClasspath"),
        Dependency(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Dependency(":core:common", "debugUnitTestCompileClasspath"),
        Dependency(":core:footballinfo", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:common", "implementation"),
        Dependency(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Dependency(":core:common", "releaseUnitTestCompileClasspath"),
        Dependency(":core:footballinfo", "releaseUnitTestRuntimeClasspath"),
    ),
    ":features:match-day" to listOf(
        Dependency(":features:match-day", "debugAndroidTestCompileClasspath"),
        Dependency(":core:common", "debugAndroidTestCompileClasspath"),
        Dependency(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Dependency(":core:design-system", "debugAndroidTestCompileClasspath"),
        Dependency(":core:reddit", "debugAndroidTestCompileClasspath"),
        Dependency(":features:match-day", "debugAndroidTestRuntimeClasspath"),
        Dependency(":features:match-day", "debugUnitTestCompileClasspath"),
        Dependency(":core:common", "debugUnitTestCompileClasspath"),
        Dependency(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Dependency(":core:design-system", "debugUnitTestCompileClasspath"),
        Dependency(":core:reddit", "debugUnitTestCompileClasspath"),
        Dependency(":features:match-day", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:common", "implementation"),
        Dependency(":core:footballinfo", "implementation"),
        Dependency(":core:design-system", "implementation"),
        Dependency(":core:reddit", "implementation"),
        Dependency(":features:match-day", "releaseUnitTestCompileClasspath"),
        Dependency(":core:common", "releaseUnitTestCompileClasspath"),
        Dependency(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Dependency(":core:design-system", "releaseUnitTestCompileClasspath"),
        Dependency(":core:reddit", "releaseUnitTestCompileClasspath"),
        Dependency(":features:match-day", "releaseUnitTestRuntimeClasspath"),
    ),
    ":features:match-thread" to listOf(
        Dependency(":features:match-thread", "debugAndroidTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "debugAndroidTestCompileClasspath"),
        Dependency(":core:common", "debugAndroidTestCompileClasspath"),
        Dependency(":core:footballinfo", "debugAndroidTestCompileClasspath"),
        Dependency(":core:design-system", "debugAndroidTestCompileClasspath"),
        Dependency(":core:reddit", "debugAndroidTestCompileClasspath"),
        Dependency(":features:match-thread", "debugAndroidTestRuntimeClasspath"),
        Dependency(":features:match-thread", "debugUnitTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "debugUnitTestCompileClasspath"),
        Dependency(":core:common", "debugUnitTestCompileClasspath"),
        Dependency(":core:footballinfo", "debugUnitTestCompileClasspath"),
        Dependency(":core:design-system", "debugUnitTestCompileClasspath"),
        Dependency(":core:reddit", "debugUnitTestCompileClasspath"),
        Dependency(":features:match-thread", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:webview-to-native-player", "implementation"),
        Dependency(":core:common", "implementation"),
        Dependency(":core:footballinfo", "implementation"),
        Dependency(":core:design-system", "implementation"),
        Dependency(":core:reddit", "implementation"),
        Dependency(":features:match-thread", "releaseUnitTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "releaseUnitTestCompileClasspath"),
        Dependency(":core:common", "releaseUnitTestCompileClasspath"),
        Dependency(":core:footballinfo", "releaseUnitTestCompileClasspath"),
        Dependency(":core:design-system", "releaseUnitTestCompileClasspath"),
        Dependency(":core:reddit", "releaseUnitTestCompileClasspath"),
        Dependency(":features:match-thread", "releaseUnitTestRuntimeClasspath"),
    ),
    ":app:playground" to listOf(
        Dependency(":app:playground", "debugAndroidTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "debugAndroidTestCompileClasspath"),
        Dependency(":features:match-thread", "debugAndroidTestCompileClasspath"),
        Dependency(":core:design-system", "debugAndroidTestCompileClasspath"),
        Dependency(":app:playground", "debugUnitTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "debugUnitTestCompileClasspath"),
        Dependency(":features:match-thread", "debugUnitTestCompileClasspath"),
        Dependency(":core:design-system", "debugUnitTestCompileClasspath"),
        Dependency(":app:playground", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:webview-to-native-player", "implementation"),
        Dependency(":features:match-thread", "implementation"),
        Dependency(":core:design-system", "implementation"),
        Dependency(":app:playground", "releaseUnitTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "releaseUnitTestCompileClasspath"),
        Dependency(":features:match-thread", "releaseUnitTestCompileClasspath"),
        Dependency(":core:design-system", "releaseUnitTestCompileClasspath"),
        Dependency(":app:playground", "releaseUnitTestRuntimeClasspath"),
        Dependency(":features:match-day", "testImplementation"),
    ),
    ":core:reddit" to listOf(
        Dependency(":core:reddit", "debugAndroidTestCompileClasspath"),
        Dependency(":core:common", "debugAndroidTestCompileClasspath"),
        Dependency(":core:reddit", "debugAndroidTestRuntimeClasspath"),
        Dependency(":core:reddit", "debugUnitTestCompileClasspath"),
        Dependency(":core:common", "debugUnitTestCompileClasspath"),
        Dependency(":core:reddit", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:common", "implementation"),
        Dependency(":core:reddit", "releaseUnitTestCompileClasspath"),
        Dependency(":core:common", "releaseUnitTestCompileClasspath"),
        Dependency(":core:reddit", "releaseUnitTestRuntimeClasspath"),
    ),
    ":core:webview-to-native-player" to listOf(
        Dependency(":core:webview-to-native-player", "debugAndroidTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "debugAndroidTestRuntimeClasspath"),
        Dependency(":core:webview-to-native-player", "debugUnitTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:common", "implementation"),
        Dependency(":core:webview-to-native-player", "releaseUnitTestCompileClasspath"),
        Dependency(":core:webview-to-native-player", "releaseUnitTestRuntimeClasspath"),
    ),
    ":app:main" to listOf(
        Dependency(":app:main", "debugAndroidTestCompileClasspath"),
        Dependency(":features:match-thread", "debugAndroidTestCompileClasspath"),
        Dependency(":features:match-day", "debugAndroidTestCompileClasspath"),
        Dependency(":core:design-system", "debugAndroidTestCompileClasspath"),
        Dependency(":core:common", "debugAndroidTestCompileClasspath"),
        Dependency(":app:main", "debugUnitTestCompileClasspath"),
        Dependency(":features:match-thread", "debugUnitTestCompileClasspath"),
        Dependency(":features:match-day", "debugUnitTestCompileClasspath"),
        Dependency(":core:design-system", "debugUnitTestCompileClasspath"),
        Dependency(":core:common", "debugUnitTestCompileClasspath"),
        Dependency(":app:main", "debugUnitTestRuntimeClasspath"),
        Dependency(":features:match-thread", "implementation"),
        Dependency(":features:match-day", "implementation"),
        Dependency(":core:design-system", "implementation"),
        Dependency(":core:common", "implementation"),
        Dependency(":app:main", "releaseUnitTestCompileClasspath"),
        Dependency(":features:match-thread", "releaseUnitTestCompileClasspath"),
        Dependency(":features:match-day", "releaseUnitTestCompileClasspath"),
        Dependency(":core:design-system", "releaseUnitTestCompileClasspath"),
        Dependency(":core:common", "releaseUnitTestCompileClasspath"),
        Dependency(":app:main", "releaseUnitTestRuntimeClasspath"),
    ),
    ":core:common" to listOf(
        Dependency(":core:common", "debugAndroidTestCompileClasspath"),
        Dependency(":core:common", "debugAndroidTestRuntimeClasspath"),
        Dependency(":core:common", "debugUnitTestCompileClasspath"),
        Dependency(":core:common", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:common", "releaseUnitTestCompileClasspath"),
        Dependency(":core:common", "releaseUnitTestRuntimeClasspath"),
    ),
    ":core:design-system" to listOf(
        Dependency(":core:design-system", "debugAndroidTestCompileClasspath"),
        Dependency(":core:design-system", "debugAndroidTestRuntimeClasspath"),
        Dependency(":core:design-system", "debugUnitTestCompileClasspath"),
        Dependency(":core:design-system", "debugUnitTestRuntimeClasspath"),
        Dependency(":core:design-system", "releaseUnitTestCompileClasspath"),
        Dependency(":core:design-system", "releaseUnitTestRuntimeClasspath"),
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
