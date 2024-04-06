package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Theme
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConfigCodeGeneratorTest {

    @Test
    fun `should generate simple theme code`() {
        val configCode = ConfigCodeBuilder.build(Theme.NEUTRAL)
        assertEquals(
            """
                %%{
                  init: {
                    'theme': 'neutral'
                  }
                }%%
            """.trimIndent(),
            configCode.value,
        )
    }

    @Test
    fun `should generate custom theme code`() {
        val theme = Theme.BASE(
            mapOf(
                "primaryTextColor" to "#fff",
                "primaryColor" to "#5a4f7c",
                "primaryBorderColor" to "#5a4f7c",
                "lineColor" to "#f5a623",
                "tertiaryColor" to "#40375c",
                "fontSize" to "11px",
            ),
        )

        val configCode = ConfigCodeBuilder.build(theme)

        assertEquals(
            """
                %%{
                  init: {
                    'theme': 'base',
                    'themeVariables': {"primaryTextColor":"#fff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","lineColor":"#f5a623","tertiaryColor":"#40375c","fontSize":"11px"}
                  }
                }%%
            """.trimIndent(),
            configCode.value,
        )
    }
}
