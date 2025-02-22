package dev.iurysouza.modulegraph.graph.subgraph

import dev.iurysouza.modulegraph.graph.DigraphModel
import dev.iurysouza.modulegraph.graph.MermaidCode
import dev.iurysouza.modulegraph.graph.ModuleNode
import dev.iurysouza.modulegraph.graph.PathNode

internal class NestedSubgraphBuilder {
    companion object {
        fun build(digraphModels: List<DigraphModel>): MermaidCode {
            val modules = extractUniqueModules(digraphModels)
            val tree = buildModuleTree(modules)
            return MermaidCode(generateNestedSubgraphs(tree))
        }

        private fun ident(level: Int): String = buildString {
            repeat(level * 2) {
                append(" ")
            }
        }

        private fun generateNestedSubgraphs(node: PathNode, depth: Int = 1): String {
            // If empty, do nothing
            if (node.children.isEmpty() && node.modules.isEmpty()) return ""
            return buildString {
                // Get module name from the node's full path
                // For ":libs:crash-reporting", the name is ":crash-reporting"
                // If substringAfterLast(":") is empty we on the top level already
                val moduleName = node.fullPath
                    .substringAfterLast(":")
                    .takeIf { it.isNotBlank() }
                    ?.let { ":$it" }
                    ?: node.fullPath

                val shouldOpenSubgraph = node.name != "root"
                if (shouldOpenSubgraph) {
                    append("${ident(depth)}subgraph $moduleName\n")
                }

                // List all modules belonging to this node, using full path for IDs
                node.modules.forEach { module ->
                    val leafName = module.fullPath.substringAfterLast(":")
                    append("${ident(depth + 1)}${module.fullPath}[\"$leafName\"]\n")
                }

                // Recurse into child nodes
                node.children.values.forEach { child ->
                    append(generateNestedSubgraphs(child, depth + 1))
                }

                // Close subgraph if opened
                if (shouldOpenSubgraph) {
                    append("${ident(depth)}end\n")
                }
            }
        }

        private fun buildModuleTree(nodes: Set<ModuleNode>): PathNode {
            val root = PathNode("root", "")
            nodes.forEach { node ->

                val pathSegments = node.pathSegments
                if (pathSegments.isEmpty()) return@forEach

                // Separate parent segments from the final leaf name
                val parentSegments = pathSegments.dropLast(1)
                var current = root

                // Build out intermediate path nodes
                parentSegments.forEach { segment ->
                    current = current.children.getOrPut(segment) {
                        PathNode(
                            name = segment,
                            fullPath = buildFullPath(current.fullPath, segment),
                        )
                    }
                }
                // Add the module to its parent; do not create a separate subgraph for the final segment
                current.modules.add(node.copy(name = pathSegments.last()))
            }
            return root
        }

        private fun buildFullPath(current: String, segment: String): String {
            return if (current.isEmpty()) ":$segment" else "$current:$segment"
        }

        private fun extractUniqueModules(models: List<DigraphModel>): Set<ModuleNode> {
            return models.flatMap { listOf(it.source, it.target) }.toSet()
        }
    }
}
