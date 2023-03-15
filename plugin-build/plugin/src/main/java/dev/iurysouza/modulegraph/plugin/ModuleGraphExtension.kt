package dev.iurysouza.modulegraph.plugin

import java.io.File
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property

@Suppress("UnnecessaryAbstractClass")
abstract class ModuleGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    // Example of a property that is mandatory. The task will
    // fail if this property is not set as is annotated with @Optional.

    val readmeFile: Property<File> = objects.property(File::class.java)

    val heading: Property<String> = objects.property(String::class.java)
    val tag: Property<String> = objects.property(String::class.java)
}
