package com.mnemos.profile.adapter.out.persistence.profile

import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProfileAppearanceJsonFormatMapperTest {
    private val mapper = Jackson3JsonFormatMapper()

    @Test
    fun `round trip supports Kotlin profile appearance documents`() {
        val appearance =
            ProfileAppearanceDocument.Appearance(
                nickStyle =
                    ProfileAppearanceDocument.NickStyle(
                        personalizationId = "0198f6ce-e934-7000-8000-000000000001",
                        colors =
                            listOf(
                                ProfileAppearanceDocument.GradientStop(
                                    color = "#CFFF35",
                                    position = 0,
                                ),
                                ProfileAppearanceDocument.GradientStop(
                                    color = "#D7A34F",
                                    position = 100,
                                ),
                            ),
                        angleDegrees = 45,
                        glow =
                            ProfileAppearanceDocument.Glow(
                                color = "#CFFF35",
                                strength = 60,
                            ),
                    ),
                profileTheme =
                    ProfileAppearanceDocument.ProfileTheme(
                        personalizationId = "0198f6ce-e934-7000-8000-000000000002",
                        background = "#101010",
                        surface = "#202020",
                        accent = "#CFFF35",
                        text = "#FFFFFF",
                        border = "#303030",
                        glow = null,
                        backgroundSecondary = "#151515",
                        surfaceSecondary = "#252525",
                        gradientAngleDegrees = 120,
                        textureStrength = 15,
                        layers = emptyList(),
                    ),
                avatarFrame =
                    ProfileAppearanceDocument.AvatarFrame(
                        personalizationId = "0198f6ce-e934-7000-8000-000000000003",
                        borderColor = "#CFFF35",
                        borderWidthPx = 2,
                        radiusPx = 16,
                        glow = null,
                        secondaryBorderColor = "#D7A34F",
                        gapPx = 2,
                        style = "DOUBLE",
                        layers = emptyList(),
                    ),
                profileEffect =
                    ProfileAppearanceDocument.ProfileEffect(
                        personalizationId = "0198f6ce-e934-7000-8000-000000000004",
                        effect = "PARTICLES",
                        intensity = 75,
                        primaryColor = "#CFFF35",
                        secondaryColor = "#D7A34F",
                        speed = 65,
                        density = 55,
                        scale = 45,
                        layers = emptyList(),
                    ),
                profileBackground =
                    ProfileAppearanceDocument.ProfileBackground(
                        personalizationId = "0198f6ce-e934-7000-8000-000000000005",
                        effect = "AURORA",
                        intensity = 48,
                        primaryColor = "#101D0A",
                        secondaryColor = "#203D12",
                        speed = 35,
                        density = 50,
                        scale = 80,
                        layers = emptyList(),
                    ),
            )

        val json = mapper.toString(appearance, ProfileAppearanceDocument.Appearance::class.java)
        val restored =
            mapper.fromString<ProfileAppearanceDocument.Appearance>(
                json,
                ProfileAppearanceDocument.Appearance::class.java,
            )

        assertEquals(appearance, restored)
    }

    @Test
    fun `legacy profile appearance json keeps Kotlin default values`() {
        val json =
            """
            {
              "nickStyle": null,
              "profileTheme": {
                "personalizationId": "0198f6ce-e934-7000-8000-000000000002",
                "background": "#101010",
                "surface": "#202020",
                "accent": "#CFFF35",
                "text": "#FFFFFF",
                "border": "#303030",
                "glow": null
              },
              "avatarFrame": {
                "personalizationId": "0198f6ce-e934-7000-8000-000000000003",
                "borderColor": "#CFFF35",
                "borderWidthPx": 2,
                "radiusPx": 16,
                "glow": null
              },
              "profileEffect": {
                "personalizationId": "0198f6ce-e934-7000-8000-000000000004",
                "effect": "PARTICLES",
                "intensity": 50,
                "primaryColor": "#CFFF35",
                "secondaryColor": null
              }
            }
            """.trimIndent()

        val restored =
            mapper.fromString<ProfileAppearanceDocument.Appearance>(
                json,
                ProfileAppearanceDocument.Appearance::class.java,
            )
        val theme = requireNotNull(restored.profileTheme)
        val frame = requireNotNull(restored.avatarFrame)
        val effect = requireNotNull(restored.profileEffect)

        assertEquals("#101010", theme.backgroundSecondary)
        assertEquals("#202020", theme.surfaceSecondary)
        assertEquals(145, theme.gradientAngleDegrees)
        assertEquals(0, theme.textureStrength)
        assertEquals(emptyList<ProfileAppearanceDocument.VisualLayer>(), theme.layers)

        assertEquals("#CFFF35", frame.secondaryBorderColor)
        assertEquals(0, frame.gapPx)
        assertEquals("SOLID", frame.style)
        assertEquals(emptyList<ProfileAppearanceDocument.VisualLayer>(), frame.layers)

        assertEquals(50, effect.speed)
        assertEquals(50, effect.density)
        assertEquals(50, effect.scale)
        assertEquals(emptyList<ProfileAppearanceDocument.VisualLayer>(), effect.layers)
        assertEquals(null, restored.profileBackground)
    }
}
