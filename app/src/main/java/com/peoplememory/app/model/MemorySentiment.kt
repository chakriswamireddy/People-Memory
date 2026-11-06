package com.peoplememory.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class MemorySentiment(val displayName: String, val badgeColorHex: String) {
    CARED_ABOUT_YOU("Cared about you", "#10B981"),
    HURT_YOU("Hurt you", "#EF4444"),
    DID_NOT_LIKE("Didn't like what they said/did", "#F59E0B"),
    NEUTRAL("Neutral note", "#6B7280");

    companion object {
        fun fromString(value: String?): MemorySentiment {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: NEUTRAL
        }
    }
}