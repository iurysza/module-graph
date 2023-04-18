package dev.iurysouza.modulegraph

/**
 * What text (if any) to attach to the links.
 */
enum class LinkText() {
    /**
     * Attaches no text to the links. This is the default [LinkText] if none is specified.
     */
    NONE,

    /**
     * Attaches the name of the configuration to which the dependency belongs.
     */
    CONFIGURATION,
}
