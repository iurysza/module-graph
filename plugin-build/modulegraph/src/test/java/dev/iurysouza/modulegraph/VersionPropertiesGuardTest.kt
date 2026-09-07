package dev.iurysouza.modulegraph

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Mirrors the VERSION guards in plugin-build/modulegraph/build.gradle.kts.
 * Keep these rules identical so CI proves the Portal footgun stays closed.
 */
object VersionPropertiesGuard {
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
}

class VersionPropertiesGuardTest {

    @Test
    fun `rejects inline release-please marker on VERSION line`() {
        val bad = """
            GROUP=dev.iurysouza
            VERSION=0.14.0 # x-release-please-version
            ID=dev.iurysouza.modulegraph
        """.trimIndent()

        val error = assertThrows(IllegalArgumentException::class.java) {
            VersionPropertiesGuard.assertCleanVersionProperties(bad)
        }
        assertTrue(error.message!!.contains("must not contain '#'"), error.message)
        assertTrue(error.message!!.contains("0.14.0 # x-release-please-version"), error.message)
    }

    @Test
    fun `accepts start-end block markers`() {
        val good = """
            GROUP=dev.iurysouza
            # x-release-please-start-version
            VERSION=0.14.0
            # x-release-please-end
            ID=dev.iurysouza.modulegraph
        """.trimIndent()

        VersionPropertiesGuard.assertCleanVersionProperties(good)
    }

    @Test
    fun `sanitize strips accidental mid-line comment but file guard still fails`() {
        assertEquals(
            "0.14.0",
            VersionPropertiesGuard.sanitizePluginVersion("0.14.0 # x-release-please-version"),
        )
        assertThrows(IllegalArgumentException::class.java) {
            VersionPropertiesGuard.assertCleanVersionProperties(
                "VERSION=0.14.0 # x-release-please-version\n",
            )
        }
    }

    @Test
    fun `checked-in gradle dot properties stays clean`() {
        val props = File("../gradle.properties")
        // From test working dir (modulegraph project) this is plugin-build/gradle.properties
        val candidates = listOf(
            File("gradle.properties"),
            File("../gradle.properties"),
            File("../../plugin-build/gradle.properties"),
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: File(System.getProperty("user.dir"), "../gradle.properties")
        assertTrue(file.isFile, "gradle.properties not found from ${System.getProperty("user.dir")}")
        VersionPropertiesGuard.assertCleanVersionProperties(file.readText())
    }
}
