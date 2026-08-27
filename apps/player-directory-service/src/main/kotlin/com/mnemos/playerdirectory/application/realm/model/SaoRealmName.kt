package com.mnemos.playerdirectory.application.realm.model

data class SaoRealmName(
    val number: Int,
) : Comparable<SaoRealmName> {
    init {
        require(number > 0) {
            "SAO realm number must be positive"
        }
    }

    val value: String
        get() = "SAO-$number"

    override fun compareTo(other: SaoRealmName): Int = number.compareTo(other.number)

    companion object {
        private val PATTERN = Regex("^SAO-([1-9][0-9]*)$")

        fun parseOrNull(value: String?): SaoRealmName? {
            val match = value?.let(PATTERN::matchEntire) ?: return null
            val number = match.groupValues[1].toIntOrNull() ?: return null

            return SaoRealmName(number)
        }
    }
}
