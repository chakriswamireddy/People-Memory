package com.peoplememory.app.model

enum class MemoryCategory(val displayName: String) {
    PERSONAL("Personal"),
    WORK("Work"),
    FAMILY("Family"),
    FRIENDS("Friends"),
    HEALTH("Health"),
    FINANCES("Finances"),
    EVENTS("Events"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): MemoryCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PERSONAL
        }
    }
}
