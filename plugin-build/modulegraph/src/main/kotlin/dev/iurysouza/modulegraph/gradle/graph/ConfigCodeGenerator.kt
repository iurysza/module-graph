package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.Theme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ConfigCodeGenerator {
    fun createConfig(theme: Theme): MermaidSyntax = MermaidSyntax(
        """
        |%%{
        |  init: {
        |    'theme': '${theme.name}'${theme.themeVariablesJson()}
        |  }
        |}%%
        """.trimMargin()
    )

    private fun Theme.themeVariablesJson() = if (this is Theme.BASE && themeVariables.isNotEmpty()) {
        """
            |,
            |    'themeVariables': ${Json.encodeToString(themeVariables).trimIndent()}""".trimMargin()
    } else {
        ""
    }

}
