package org.saphka.location.tracker.user.dao

import io.grpc.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.reactive.asFlow
import org.jooq.DSLContext
import org.jooq.Record
import org.saphka.location.tracker.user.dao.jooq.Tables.FRIEND
import org.saphka.location.tracker.user.model.Friend
import org.saphka.location.tracker.user.model.FriendStatus
import org.springframework.stereotype.Service

interface FriendDAO {
    fun createFriendRequest(userId: Int, friendId: Int): Flow<Friend>
    fun updateRequestStatus(userId: Int, friendId: Int, status: FriendStatus): Flow<Friend>
    fun getFriends(id: Int): Flow<Friend>
}

@Service
class FriendDAOImpl(private val create: DSLContext) : FriendDAO {
    override fun createFriendRequest(userId: Int, friendId: Int): Flow<Friend> {
        val (firstId, secondId) = determineFirstSecond(userId, friendId)

        return create.insertInto(FRIEND)
            .columns(FRIEND.FIRST_ID, FRIEND.SECOND_ID, FRIEND.STATUS)
            .values(firstId, secondId, FriendStatus.PENDING.name)
            .onConflictDoNothing()
            .returning()
            .asFlow()
            .map { mapToFriend(it, userId) }
            .onEmpty {
                Status.ALREADY_EXISTS.withDescription("Friend request between users $userId $friendId already exists")
                    .asRuntimeException()
            }
    }

    override fun updateRequestStatus(userId: Int, friendId: Int, status: FriendStatus): Flow<Friend> {
        val (firstId, secondId) = determineFirstSecond(userId, friendId)

        return create.update(FRIEND)
            .set(FRIEND.STATUS, status.name)
            .where(FRIEND.FIRST_ID.eq(firstId))
            .and(FRIEND.SECOND_ID.eq(secondId))
            .returning()
            .asFlow()
            .map { mapToFriend(it, userId) }
            .onEmpty {
                Status.NOT_FOUND.withDescription("Friend relation between users $userId $friendId not found")
                    .asRuntimeException()
            }
    }

    override fun getFriends(id: Int): Flow<Friend> {
        return create.select()
            .from(FRIEND)
            .where(FRIEND.FIRST_ID.eq(id))
            .or(FRIEND.SECOND_ID.eq(id))
            .asFlow()
            .map { mapToFriend(it, id) }
    }

    private fun determineFirstSecond(userId: Int, friendId: Int): Pair<Int, Int> {
        val firstId = if (userId < friendId) userId else friendId
        val secondId = if (userId < friendId) friendId else userId
        return Pair(firstId, secondId)
    }

    private fun mapToFriend(it: Record, userId: Int): Friend {
        val firstId = it.get(FRIEND.FIRST_ID)
        val secondId = it.get(FRIEND.SECOND_ID)

        val friendId = if (firstId == userId) secondId else firstId

        return Friend(
            userId,
            friendId,
            FriendStatus.valueOf(it.get(FRIEND.STATUS))
        )
    }
}