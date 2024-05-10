package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.model.GraphConfig

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
    ) to
        listOf(
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
        |  subgraph :sample
        |    :sample:alpha["alpha"]
        |    :sample:zeta["zeta"]
        |  end
        |  subgraph :sample:container
        |    :sample:container:gama["gama"]
        |  end
        |  :sample:alpha --> :sample:container:gama
        |  :sample:container:gama --> :sample:zeta
        |
        |classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
        |class :sample:container:gama focus
        |```
""".trimMargin()

internal fun getConfig(
    readmePath: String = "",
    heading: String = "",
    theme: Theme? = null,
    rootModulesRegex: String? = null,
    orientation: Orientation? = null,
    focusedModulesRegex: String? = null,
    excludedConfigurationsRegex: String? = null,
    excludedModulesRegex: String? = null,
    linkText: LinkText? = null,
    setStyleByModuleType: Boolean? = null,
    showFullPath: Boolean? = null,
) =
    GraphConfig.Builder(
        readmePath = readmePath,
        heading = heading,
    ).apply {
        this.theme = theme
        this.rootModulesRegex = rootModulesRegex
        this.orientation = orientation
        this.focusedModulesRegex = focusedModulesRegex
        this.excludedConfigurationsRegex = excludedConfigurationsRegex
        this.excludedModulesRegex = excludedModulesRegex
        this.linkText = linkText
        this.setStyleByModuleType = setStyleByModuleType
        this.showFullPath = showFullPath
    }.build()

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
    |  subgraph :app
    |    :app:playground["playground"]
    |    :app:main["main"]
    |  end
    |  subgraph :core
    |    :core:footballinfo["footballinfo"]
    |    :core:common["common"]
    |    :core:design-system["design-system"]
    |    :core:reddit["reddit"]
    |    :core:webview-to-native-player["webview-to-native-player"]
    |  end
    |  subgraph :features
    |    :features:match-day["match-day"]
    |    :features:match-thread["match-thread"]
    |  end
    |  :core:footballinfo --> :core:common
    |  :features:match-day --> :core:common
    |  :features:match-day --> :core:footballinfo
    |  :features:match-day --> :core:design-system
    |  :features:match-day --> :core:reddit
    |  :features:match-thread --> :core:webview-to-native-player
    |  :features:match-thread --> :core:common
    |  :features:match-thread --> :core:footballinfo
    |  :features:match-thread --> :core:design-system
    |  :features:match-thread --> :core:reddit
    |  :app:playground --> :core:webview-to-native-player
    |  :app:playground --> :features:match-thread
    |  :app:playground --> :core:design-system
    |  :app:playground --> :features:match-day
    |  :core:reddit --> :core:common
    |  :core:webview-to-native-player --> :core:common
    |  :app:main --> :features:match-thread
    |  :app:main --> :features:match-day
    |  :app:main --> :core:design-system
    |  :app:main --> :core:common
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
|  :core:footballinfo -- debugAndroidTestCompileClasspath --> :core:common
|  :core:footballinfo -- debugUnitTestCompileClasspath --> :core:common
|  :core:footballinfo -- implementation --> :core:common
|  :core:footballinfo -- releaseUnitTestCompileClasspath --> :core:common
|  :features:match-day -- debugAndroidTestCompileClasspath --> :core:common
|  :features:match-day -- debugAndroidTestCompileClasspath --> :core:footballinfo
|  :features:match-day -- debugAndroidTestCompileClasspath --> :core:design-system
|  :features:match-day -- debugAndroidTestCompileClasspath --> :core:reddit
|  :features:match-day -- debugUnitTestCompileClasspath --> :core:common
|  :features:match-day -- debugUnitTestCompileClasspath --> :core:footballinfo
|  :features:match-day -- debugUnitTestCompileClasspath --> :core:design-system
|  :features:match-day -- debugUnitTestCompileClasspath --> :core:reddit
|  :features:match-day -- implementation --> :core:common
|  :features:match-day -- implementation --> :core:footballinfo
|  :features:match-day -- implementation --> :core:design-system
|  :features:match-day -- implementation --> :core:reddit
|  :features:match-day -- releaseUnitTestCompileClasspath --> :core:common
|  :features:match-day -- releaseUnitTestCompileClasspath --> :core:footballinfo
|  :features:match-day -- releaseUnitTestCompileClasspath --> :core:design-system
|  :features:match-day -- releaseUnitTestCompileClasspath --> :core:reddit
|  :features:match-thread -- debugAndroidTestCompileClasspath --> :core:webview-to-native-player
|  :features:match-thread -- debugAndroidTestCompileClasspath --> :core:common
|  :features:match-thread -- debugAndroidTestCompileClasspath --> :core:footballinfo
|  :features:match-thread -- debugAndroidTestCompileClasspath --> :core:design-system
|  :features:match-thread -- debugAndroidTestCompileClasspath --> :core:reddit
|  :features:match-thread -- debugUnitTestCompileClasspath --> :core:webview-to-native-player
|  :features:match-thread -- debugUnitTestCompileClasspath --> :core:common
|  :features:match-thread -- debugUnitTestCompileClasspath --> :core:footballinfo
|  :features:match-thread -- debugUnitTestCompileClasspath --> :core:design-system
|  :features:match-thread -- debugUnitTestCompileClasspath --> :core:reddit
|  :features:match-thread -- implementation --> :core:webview-to-native-player
|  :features:match-thread -- implementation --> :core:common
|  :features:match-thread -- implementation --> :core:footballinfo
|  :features:match-thread -- implementation --> :core:design-system
|  :features:match-thread -- implementation --> :core:reddit
|  :features:match-thread -- releaseUnitTestCompileClasspath --> :core:webview-to-native-player
|  :features:match-thread -- releaseUnitTestCompileClasspath --> :core:common
|  :features:match-thread -- releaseUnitTestCompileClasspath --> :core:footballinfo
|  :features:match-thread -- releaseUnitTestCompileClasspath --> :core:design-system
|  :features:match-thread -- releaseUnitTestCompileClasspath --> :core:reddit
|  :app:playground -- debugAndroidTestCompileClasspath --> :core:webview-to-native-player
|  :app:playground -- debugAndroidTestCompileClasspath --> :features:match-thread
|  :app:playground -- debugAndroidTestCompileClasspath --> :core:design-system
|  :app:playground -- debugUnitTestCompileClasspath --> :core:webview-to-native-player
|  :app:playground -- debugUnitTestCompileClasspath --> :features:match-thread
|  :app:playground -- debugUnitTestCompileClasspath --> :core:design-system
|  :app:playground -- implementation --> :core:webview-to-native-player
|  :app:playground -- implementation --> :features:match-thread
|  :app:playground -- implementation --> :core:design-system
|  :app:playground -- releaseUnitTestCompileClasspath --> :core:webview-to-native-player
|  :app:playground -- releaseUnitTestCompileClasspath --> :features:match-thread
|  :app:playground -- releaseUnitTestCompileClasspath --> :core:design-system
|  :app:playground -- testImplementation --> :features:match-day
|  :core:reddit -- debugAndroidTestCompileClasspath --> :core:common
|  :core:reddit -- debugUnitTestCompileClasspath --> :core:common
|  :core:reddit -- implementation --> :core:common
|  :core:reddit -- releaseUnitTestCompileClasspath --> :core:common
|  :core:webview-to-native-player -- implementation --> :core:common
|  :app:main -- debugAndroidTestCompileClasspath --> :features:match-thread
|  :app:main -- debugAndroidTestCompileClasspath --> :features:match-day
|  :app:main -- debugAndroidTestCompileClasspath --> :core:design-system
|  :app:main -- debugAndroidTestCompileClasspath --> :core:common
|  :app:main -- debugUnitTestCompileClasspath --> :features:match-thread
|  :app:main -- debugUnitTestCompileClasspath --> :features:match-day
|  :app:main -- debugUnitTestCompileClasspath --> :core:design-system
|  :app:main -- debugUnitTestCompileClasspath --> :core:common
|  :app:main -- implementation --> :features:match-thread
|  :app:main -- implementation --> :features:match-day
|  :app:main -- implementation --> :core:design-system
|  :app:main -- implementation --> :core:common
|  :app:main -- releaseUnitTestCompileClasspath --> :features:match-thread
|  :app:main -- releaseUnitTestCompileClasspath --> :features:match-day
|  :app:main -- releaseUnitTestCompileClasspath --> :core:design-system
|  :app:main -- releaseUnitTestCompileClasspath --> :core:common
""".trimMargin()
