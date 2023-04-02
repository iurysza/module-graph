package dev.iurysouza.modulegraph

/**
 * More info at [mermaid docs](https://mermaid.js.org/syntax/flowchart.html#flowchart-orientation)
 */
enum class Orientation(val value: String) {
    /**
     * The default [Orientation] if none is specified
     */
    LEFT_TO_RIGHT("LR"),
    RIGHT_TO_LEFT("RL"),
    TOP_TO_BOTTOM("TB"),
    BOTTOM_TO_TOP("BT"),
}
