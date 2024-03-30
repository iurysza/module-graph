package dev.iurysouza.modulegraph

import java.io.Serializable as JavaSerializable
import kotlinx.serialization.Serializable

/**
 * More info at [mermaid docs](https://mermaid.js.org/config/theming.html#theme-configuration)
 */
@Serializable
sealed class Theme(val name: String) : JavaSerializable {

    /**
     * This theme goes well with dark-colored elements or dark-mode.
     */
    object DARK : Theme("dark")

    /**
     * This is the default theme for all diagrams.
     */
    object DEFAULT : Theme("default")

    /**
     * This theme contains shades of green.
     */
    object FOREST : Theme("forest")

    /**
     * This theme is great for black and white documents that will be printed.
     */
    object NEUTRAL : Theme("neutral")

    /**
     * The BASE theme can be used for customization. You just need to provide the themeVariables according to the specs.
     * You can read more about it [here](https://mermaid.js.org/config/theming.html#theme-variables).
     */
    data class BASE(
        val themeVariables: Map<String, String> = emptyMap(),
        val focusColor: FocusColor = DEFAULT_FOCUS_COLOR,
    ) : Theme("base")
}

typealias FocusColor = String

internal const val DEFAULT_FOCUS_COLOR = "#769566"
