package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.LinkText
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class DigraphBuilderTest {

    @Test
    fun `digraph builder works as expected`() {
        val linkText = LinkText.NONE
        val anInput = aDigraphInput(regex = ".*gama.*".toRegex())
        val digraphModelList = DigraphBuilder.build(anInput)
        val digraphSyntax = DigraphCodeGenerator.mermaid(digraphModelList, linkText)

        assertEquals(expectedDigraph.mermaidStringSyntax, digraphSyntax.value)
    }
}
