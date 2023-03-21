package dev.iurysouza.modulegraph

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property

@Suppress("UnnecessaryAbstractClass")
abstract class ModuleGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val theme: Property<Theme> = objects.property(Theme::class.java)

    val readmePath: Property<String> = objects.property(String::class.java)

    val heading: Property<String> = objects.property(String::class.java)
}
