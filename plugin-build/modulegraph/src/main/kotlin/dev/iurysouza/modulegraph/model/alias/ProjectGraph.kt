package dev.iurysouza.modulegraph.model.alias

import dev.iurysouza.modulegraph.gradle.Module

internal typealias ProjectGraph = Map<Module, List<Module>>
