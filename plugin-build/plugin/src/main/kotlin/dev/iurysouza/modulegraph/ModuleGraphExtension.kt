package dev.iurysouza.modulegraph

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

@Suppress("UnnecessaryAbstractClass")
abstract class ModuleGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val readmePath: Property<String> = objects.property(String::class.java)

    val heading: Property<String> = objects.property(String::class.java)

    val outputFile: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(readmePath)
    )
}
