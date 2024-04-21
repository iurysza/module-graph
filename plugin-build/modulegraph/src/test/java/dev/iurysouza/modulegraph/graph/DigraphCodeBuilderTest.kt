package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.LinkText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DigraphCodeBuilderTest {

    @Test
    fun `Build digraph with default options returns correct mermaid syntax`() {
        val graphModel = aModuleGraph()
        val graphOptions = withGraphOptions(
            focusedModulesRegex = ".*gama.*",
        )

        val mermaidCode = DigraphCodeBuilder.build(
            digraphModel = DigraphBuilder.build(graphModel, graphOptions),
            linkText = graphOptions.linkText,
        )

        val expectedMermaidCode = """
        |  :sample:alpha --> :sample:container:gama
        |  :sample:container:gama --> :sample:zeta
        """.trimMargin()
        assertEquals(expectedMermaidCode, mermaidCode.value)
    }

    @Test
    fun `Build digraph with configuration link text includes configuration details`() {
        val graphModel = fullLiveMatchGraph
        val graphOptions = withGraphOptions(
            linkText = LinkText.CONFIGURATION,
        )

        val mermaidCode = DigraphCodeBuilder.build(
            digraphModel = DigraphBuilder.build(graphModel, graphOptions),
            linkText = graphOptions.linkText,
        )

        assertEquals(liveMatchMermaidGraphWithConfigurations, mermaidCode.value)
    }

    @Test
    fun `Build digraph ignoring configurations when link text is none`() {
        val graphModel = fullLiveMatchGraph
        val graphOptions = withGraphOptions()

        val mermaidCode = DigraphCodeBuilder.build(
            digraphModel = DigraphBuilder.build(graphModel, graphOptions),
            linkText = graphOptions.linkText,
        )

        val expectedMermaidCode = """
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
        """.trimMargin()
        assertEquals(expectedMermaidCode, mermaidCode.value)
    }
}
