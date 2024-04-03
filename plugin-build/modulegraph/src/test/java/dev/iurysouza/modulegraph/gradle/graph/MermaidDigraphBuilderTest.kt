package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.LinkText
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class MermaidDigraphBuilderTest {

    @Test
    fun `digraph builder works as expected`() {
        val digraph = DigraphBuilder.build(mermaidInput)
        val mermaidSyntax = DigraphCodeGenerator.mermaid(digraph, LinkText.NONE)

        assertEquals(expectedDigraph.mermaidStringSyntax, mermaidSyntax.value)
    }
}
