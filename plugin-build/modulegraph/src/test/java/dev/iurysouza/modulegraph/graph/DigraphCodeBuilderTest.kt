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
        |  alpha --> gama
        |  gama --> zeta
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
        """.trimMargin()
        assertEquals(expectedMermaidCode, mermaidCode.value)
    }
}
