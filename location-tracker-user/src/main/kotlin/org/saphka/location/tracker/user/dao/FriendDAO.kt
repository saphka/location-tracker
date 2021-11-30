package org.saphka.location.tracker.user.dao

import io.grpc.Status
import org.jooq.DSLContext
import org.jooq.Record
import org.saphka.location.tracker.user.dao.jooq.Tables.FRIEND
import org.saphka.location.tracker.user.model.Friend
import org.saphka.location.tracker.user.model.FriendStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FriendDAO {
    fun createFriendRequest(firstId: Int, secondId: Int): Mono<Friend>
    fun updateRequestStatus(firstId: Int, secondId: Int, status: FriendStatus): Mono<Friend>
    fun getFriends(id: Int): Flux<Friend>
}

@Service
class FriendDAOImpl(private val create: DSLContext) : FriendDAO {
    override fun createFriendRequest(firstId: Int, secondId: Int): Mono<Friend> {
        return Mono.from(
            create.insertInto(FRIEND)
                .columns(FRIEND.FIRST_ID, FRIEND.SECOND_ID, FRIEND.STATUS)
                .values(firstId, secondId, FriendStatus.PENDING.name)
                .onConflictDoNothing()
                .returning()
        ).map { mapToFriend(it) }
            .switchIfEmpty(Mono.error {
                Status.ALREADY_EXISTS.withDescription("Friend request between users $firstId $secondId already exists")
                    .asRuntimeException()
            })
    }

    override fun updateRequestStatus(firstId: Int, secondId: Int, status: FriendStatus): Mono<Friend> {
        return Mono.from(
            create.update(FRIEND)
                .set(FRIEND.STATUS, status.name)
                .where(FRIEND.FIRST_ID.eq(firstId))
                .and(FRIEND.SECOND_ID.eq(secondId))
                .returning()
        ).map { mapToFriend(it) }
            .switchIfEmpty(Mono.error {
                Status.NOT_FOUND.withDescription("Friend relation between users $firstId $secondId not found")
                    .asRuntimeException()
            })
    }

    override fun getFriends(id: Int): Flux<Friend> {
        return Flux.from(
            create.select()
                .from(FRIEND)
                .where(FRIEND.FIRST_ID.eq(id))
                .or(FRIEND.SECOND_ID.eq(id))
        ).map { mapToFriend(it) }
    }

    private fun mapToFriend(it: Record) = Friend(
        it.get(FRIEND.FIRST_ID),
        it.get(FRIEND.SECOND_ID),
        FriendStatus.valueOf(it.get(FRIEND.STATUS))
    )
}