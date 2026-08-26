package com.mnemos.personalization.application.personalization.builtin

object BuiltinVisualPersonalizationCatalog {
    val definitions: List<BuiltinPersonalizationDefinition> =
        buildList {
            addAll(BuiltinNickStyles.definitions)
            addAll(BuiltinNickStylesExpansion.definitions)
            addAll(BuiltinProfileThemes.definitions)
            addAll(BuiltinProfileThemesExpansion.definitions)
            addAll(BuiltinProfileBackgrounds.definitions)
            addAll(BuiltinAvatarFrames.definitions)
            addAll(BuiltinAvatarFramesExpansion.definitions)
            addAll(BuiltinProfileEffects.definitions)
            addAll(BuiltinProfileEffectsExpansion.definitions)
        }
}
