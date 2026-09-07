@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.pluginPublish)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(libs.kotlinxSerializationJson)

    testImplementation(libs.junit5Api)
    testRuntimeOnly(libs.junit5Engine)
    testImplementation(gradleTestKit())
}

// Configure the test task to use JUnit 5
tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}

/** Plugin Portal version rules (must stay in sync with VersionPropertiesGuardTest). */
fun sanitizePluginVersion(raw: String): String {
    val stripped = raw.trim().substringBefore("#").trim()
    require(stripped.isNotEmpty()) {
        "VERSION is empty after sanitizing '$raw'"
    }
    require(' ' !in stripped && '\t' !in stripped) {
        "VERSION must not contain whitespace: '$stripped' (from '$raw')"
    }
    require(stripped.length < 50) {
        "VERSION must be under 50 characters: '$stripped'"
    }
    require(stripped.any { it.isDigit() }) {
        "VERSION must include a number: '$stripped'"
    }
    require(stripped.matches(Regex("""^[a-zA-Z0-9.\-\[\]:+]+$"""))) {
        "VERSION '$stripped' is not Plugin Portal-safe (from '$raw'). " +
            "Never put release-please markers on the VERSION= line."
    }
    return stripped
}

fun assertCleanVersionProperties(content: String) {
    val versionLines = content.lineSequence()
        .map { it.trimEnd() }
        .filter { it.trimStart().startsWith("VERSION=") }
        .toList()
    require(versionLines.size == 1) {
        "Expected exactly one VERSION= line in gradle.properties, found ${versionLines.size}"
    }
    val line = versionLines.single()
    require('#' !in line) {
        "VERSION line must not contain '#': '$line'. " +
            "Java Properties treats mid-line # as part of the value. " +
            "Use # x-release-please-start-version / # x-release-please-end on their own lines."
    }
    sanitizePluginVersion(line.substringAfter("VERSION="))
}


val pluginVersion = sanitizePluginVersion(property("VERSION").toString())

gradlePlugin {
    plugins {
        create(property("ID").toString()) {
            id = property("ID").toString()
            implementationClass = property("IMPLEMENTATION_CLASS").toString()
            version = pluginVersion
            description = property("DESCRIPTION").toString()
            displayName = property("DISPLAY_NAME").toString()
            tags.set(listOf("mermaid", "diagram"))
        }
        create("${property("ID")}.settings") {
            id = "${property("ID")}.settings"
            implementationClass = property("IMPLEMENTATION_CLASS_SETTINGS").toString()
            version = pluginVersion
            description = property("DESCRIPTION").toString()
            displayName = "${property("DISPLAY_NAME")} (Settings)"
            tags.set(listOf("mermaid", "diagram"))
        }
    }
}

gradlePlugin {
    website.set(property("WEBSITE").toString())
    vcsUrl.set(property("VCS_URL").toString())
}


tasks.register("validateVersionProperties") {
    group = "verification"
    description = "Fails if plugin-build/gradle.properties VERSION is not Plugin Portal-safe."
    val propsFile = rootProject.file("gradle.properties")
    inputs.file(propsFile)
    doLast {
        assertCleanVersionProperties(propsFile.readText())
    }
}

tasks.named("check") {
    dependsOn("validateVersionProperties")
}

tasks.create("setupPluginUploadFromEnvironment") {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}
