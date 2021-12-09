package org.saphka.location.tracker.location.model

import java.time.Instant

data class Location(
    val id: Int,
    val userId: Int,
    val friendId: Int,
    val timestamp: Instant,
    val latitude: ByteArray,
    val longitude: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (friendId != other.friendId) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + friendId
        result = 31 * result + timestamp.hashCode()
        return result
    }
}