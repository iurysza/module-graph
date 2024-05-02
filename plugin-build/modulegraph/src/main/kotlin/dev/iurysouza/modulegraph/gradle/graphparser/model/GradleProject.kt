package dev.iurysouza.modulegraph.gradle.graphparser.model

import dev.iurysouza.modulegraph.ModuleType

/** Interface to abstract use-sites from the actual gradle project class */
internal interface GradleProject {
    /** Fully qualified and unique name of this project */
    val path: String

    /** List containing all the configurations of this project */
    val configurations: List<GradleProjectConfiguration>

    fun getModuleType(customModuleTypes: List<ModuleType>): ModuleType
}
