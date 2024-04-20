package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.ModuleType
import java.io.Serializable

internal data class Module(
    val path: String,
    val configName: String? = null,
    val type: ModuleType = ModuleType.Java(),
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 46465844
    }
}
