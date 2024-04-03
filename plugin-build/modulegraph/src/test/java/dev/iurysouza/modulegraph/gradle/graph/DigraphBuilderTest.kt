package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.gradle.graph.DigraphBuilder
import dev.iurysouza.modulegraph.gradle.graph.DigraphCodeGenerator
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class DigraphBuilderTest {

    @Test
    fun `digraph builder works as expected`() {
        val linkText = LinkText.NONE
        val anInput = aDigraphInput(regex = ".*gama.*")
        val digraphModelList = DigraphBuilder.build(anInput)
        val digraphSyntax = DigraphCodeGenerator.mermaid(digraphModelList, linkText)

        val mermaidStringSyntax = """
        |alpha --> gama
        |gama --> zeta""".trimMargin()
        assertEquals(mermaidStringSyntax, digraphSyntax.value)
    }
}
