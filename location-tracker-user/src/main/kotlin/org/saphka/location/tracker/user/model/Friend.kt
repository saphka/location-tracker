package org.saphka.location.tracker.user.model

class Friend(val userId: Int, val friendId: Int, val status: FriendStatus)

enum class FriendStatus {
    PENDING,
    CONFIRMED
}