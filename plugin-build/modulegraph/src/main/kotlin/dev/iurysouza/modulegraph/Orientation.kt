package dev.iurysouza.modulegraph

/**
 * The orientation of the flowchart in the generated graph.
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
