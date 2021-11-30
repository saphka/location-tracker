package org.saphka.location.tracker.user.model

class Friend(val firstId: Int, val secondId: Int, val status : FriendStatus)

enum class FriendStatus {
    PENDING,
    CONFIRMED
}