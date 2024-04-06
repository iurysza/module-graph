package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Theme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ConfigCodeBuilder {
    /**
     * This function generates a `MermaidCode` configuration
     *
     * @param theme The theme
     * @return `MermaidCode` is returned with the configuration specific to the provided `Theme`.
     *
     * If the `Theme` is the base theme and it has theme variables,
     * those will be added as `themeVariables` to the configuration.
     *
     *This might generate a `MermaidCode` configuration like:
     *
     *```mermaid
     *%%{
     *  init: {
     *    'theme': 'base',
     *    'themeVariables': {"primaryTextColor":"#F6F8FAff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","tertiaryColor":"#40375c","lineColor":"#f5a623","fontSize":"12px"}
     *  }
     *}%%
     *```
     */
    fun build(theme: Theme): MermaidCode = MermaidCode(
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
