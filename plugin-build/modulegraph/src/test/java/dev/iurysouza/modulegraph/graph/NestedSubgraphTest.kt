package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Mermaid
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.model.GraphParseResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NestedSubgraphTest {

    @Test
    fun `Build digraph with nested subgraphs`() {
        val graphModel = nestedModuleGraph()
        val config = getConfig()
        val result = Mermaid.generateGraph(GraphParseResult(graphModel, config))

        val nestedGraph = """
        ```mermaid
            %%{
              init: {
                'theme': 'neutral'
              }
            }%%

            graph LR
              subgraph :libs
                :libs:app-common["app-common"]
                subgraph :crash-reporting
                  :libs:crash-reporting:api["api"]
                  :libs:crash-reporting:firebase["firebase"]
                end
              end
              :App --> :libs:app-common
              :App --> :libs:crash-reporting:api
              :App --> :libs:crash-reporting:firebase
            classDef focus fill:#769566,stroke:#fff,stroke-width:2px,color:#fff;
            class :App focus
        ```
        """.trimIndent()
        val expectedMermaidCode = """
        ```mermaid
        %%{
          init: {
            'theme': 'neutral'
          }
        }%%

        graph LR
          subgraph :libs
            :libs:app-common["app-common"]
          end
          subgraph :libs:crash-reporting
            :libs:crash-reporting:api["api"]
            :libs:crash-reporting:firebase["firebase"]
          end
          :App --> :libs:app-common
          :App --> :libs:crash-reporting:api
          :App --> :libs:crash-reporting:firebase
        ```
        """.trimIndent()
        assertEquals(nestedGraph, result)
    }

    private fun nestedModuleGraph() = mapOf(
        Module(
            path = ":App",
        ) to listOf(
            Module(
                path = ":libs:app-common",
                configName = "implementation",
            ),
            Module(
                path = ":libs:crash-reporting:api",
                configName = "implementation",
            ),
            Module(
                path = ":libs:crash-reporting:firebase",
                configName = "implementation",
            ),
        ),
        Module(
            path = ":libs:crash-reporting:api",
        ) to emptyList(),
        Module(
            path = ":libs:crash-reporting:firebase",
        ) to emptyList(),
        Module(
            path = ":libs:app-common",
        ) to emptyList(),
    )
}
