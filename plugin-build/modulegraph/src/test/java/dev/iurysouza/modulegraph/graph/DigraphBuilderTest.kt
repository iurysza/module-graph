package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DigraphBuilderTest {

    @Test
    fun `digraph builder works as expected`() {
        val graphModel = aModuleGraph()
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
            theme = Theme.NEUTRAL,
            showFullPath = false,
            pattern = ".*gama.*".toRegex(),
            orientation = Orientation.TOP_TO_BOTTOM,
        )

        val digraphModelList = DigraphBuilder.build(graphModel, graphOptions)
        val digraphSyntax = DigraphCodeBuilder.build(digraphModelList, graphOptions.linkText)

        val mermaidStringSyntax = """
        |  alpha --> gama
        |  gama --> zeta
        """.trimMargin()
        assertEquals(mermaidStringSyntax, digraphSyntax.value)
    }

//    @Test
//    fun `when linkText is Config, digraphbuilder considers all different configurations`() {
//        val graphModel = fullLiveMatchGraph
//        val graphOptions = GraphOptions(
//            linkText = LinkText.CONFIGURATION,
//            theme = Theme.NEUTRAL,
//            showFullPath = false,
//            orientation = Orientation.LEFT_TO_RIGHT,
//        )
//
//        val digraphModelList = DigraphBuilder.build(graphModel, graphOptions)
//        val digraphSyntax = DigraphCodeBuilder.build(digraphModelList, graphOptions.linkText)
//
//        assertEquals(liveMatchMermaidGraphWithConfigurations, digraphSyntax.value)
//    }

//    @Test
//    fun `when linkText is none, digraphbuilder ignores different configurations`() {
//        val graphModel = fullLiveMatchGraph
//        val graphOptions = GraphOptions(
//            linkText = LinkText.NONE,
//            theme = Theme.NEUTRAL,
//            showFullPath = false,
//            orientation = Orientation.LEFT_TO_RIGHT,
//        )
//
//        val digraphModelList = DigraphBuilder.build(graphModel, graphOptions)
//        val digraphSyntax = DigraphCodeBuilder.build(digraphModelList, graphOptions.linkText)
//
//        val mermaidStringSyntax = """
//                |  footballinfo --> common
//                |  match-day --> common
//                |  match-day --> footballinfo
//                |  match-day --> design-system
//                |  match-day --> reddit
//                |  match-thread --> webview-to-native-player
//                |  match-thread --> common
//                |  match-thread --> footballinfo
//                |  match-thread --> design-system
//                |  match-thread --> reddit
//                |  playground --> webview-to-native-player
//                |  playground --> match-thread
//                |  playground --> design-system
//                |  playground --> match-day
//                |  reddit --> common
//                |  webview-to-native-player --> common
//                |  main --> match-thread
//                |  main --> match-day
//                |  main --> design-system
//                |  main --> common
//        """.trimMargin()
//        assertEquals(mermaidStringSyntax, digraphSyntax.value)
//    }
}
