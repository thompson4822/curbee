package com.example.models

sealed interface ChangeType

data class PropertyUpdate(
    val property: String,
    val previous: Any?,
    val current: Any?) : ChangeType {
    override fun toString(): String {
        return """{"property": "$property", "previous": "$previous", "current": "$current"}"""
    }
}

data class ListUpdate(
    val property: String,
    val added: List<Any?>,
    val removed: List<Any?>
) : ChangeType {
    override fun toString(): String {
        return """{"property": "$property", "added": $added, "removed": $removed}"""
    }
}
