package com.mnemos.personalization.application.personalization.builtin

object BuiltinVisualPersonalizationCatalog {
    val definitions: List<BuiltinPersonalizationDefinition> =
        buildList {
            addAll(BuiltinNickStyles.definitions)
            addAll(BuiltinNickStylesPalette.definitions)
            addAll(BuiltinProfileThemes.definitions)
            addAll(BuiltinProfileThemesPalette.definitions)
            addAll(BuiltinProfileBackgrounds.definitions)
            addAll(BuiltinAvatarFrames.definitions)
            addAll(BuiltinAvatarFramesDecorative.definitions)
            addAll(BuiltinProfileEffects.definitions)
            addAll(BuiltinProfileEffectsSpectrum.definitions)
        }
}
