package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class ModuleGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val theme: Property<Theme> = objects.property(Theme::class.java)

    val orientation: Property<Orientation> = objects.property(Orientation::class.java)

    val readmePath: Property<String> = objects.property(String::class.java)

    val heading: Property<String> = objects.property(String::class.java)

    val linkText: Property<LinkText> = objects.property(LinkText::class.java)
}
