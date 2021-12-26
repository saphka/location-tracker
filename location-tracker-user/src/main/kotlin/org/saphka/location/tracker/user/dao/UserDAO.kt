package org.saphka.location.tracker.user.dao

import io.grpc.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.reactive.asFlow
import org.jooq.DSLContext
import org.jooq.Record
import org.saphka.location.tracker.user.dao.jooq.Keys.C_USER_ALIAS_UNIQUE
import org.saphka.location.tracker.user.dao.jooq.Tables.FRIEND
import org.saphka.location.tracker.user.dao.jooq.Tables.USER_TABLE
import org.saphka.location.tracker.user.model.FriendStatus
import org.saphka.location.tracker.user.model.User
import org.springframework.stereotype.Service

interface UserDAO {
    fun findUser(id: Int): Flow<User>
    fun findUser(alias: String): Flow<User>
    fun createUser(alias: String, publicKey: ByteArray, passwordHash: String): Flow<User>
    fun updateUser(user: User): Flow<User>
    fun findUsersFriends(id: Int): Flow<User>
}

@Service
class UserDAOImpl(private val create: DSLContext) : UserDAO {
    override fun findUser(id: Int): Flow<User> {
        return create.select().from(USER_TABLE).where(USER_TABLE.ID.eq(id))
            .asFlow()
            .map { mapUserRecord(it) }
            .onEmpty {
                Status.NOT_FOUND.withDescription("User with id $id not found").asRuntimeException()
            }
    }

    override fun findUser(alias: String): Flow<User> {
        return create.select().from(USER_TABLE).where(USER_TABLE.USER_ALIAS.eq(alias))
            .asFlow()
            .map { mapUserRecord(it) }
            .onEmpty {
                Status.NOT_FOUND.withDescription("User with alias $alias not found").asRuntimeException()
            }
    }

    override fun createUser(alias: String, publicKey: ByteArray, passwordHash: String): Flow<User> {
        return create.insertInto(USER_TABLE)
            .columns(USER_TABLE.USER_ALIAS, USER_TABLE.PUBLIC_KEY, USER_TABLE.PASSWORD_HASH)
            .values(alias, publicKey, passwordHash)
            .onConflictOnConstraint(C_USER_ALIAS_UNIQUE)
            .doNothing()
            .returning()
            .asFlow()
            .onEach { print(it) }
            .map { mapUserRecord(it) }
            .onEmpty {
                Status.ALREADY_EXISTS.withDescription("User with alias $alias already exists").asRuntimeException()
            }
    }

    override fun updateUser(user: User): Flow<User> {
        return create.update(USER_TABLE)
            .set(USER_TABLE.PUBLIC_KEY, user.publicKey)
            .returning()
            .asFlow()
            .map { mapUserRecord(it) }
    }

    override fun findUsersFriends(id: Int): Flow<User> {
        return create.select().from(USER_TABLE).where(
            USER_TABLE.ID.`in`(
                create.select(FRIEND.SECOND_ID).from(FRIEND)
                    .where(FRIEND.FIRST_ID.eq(id).and(FRIEND.STATUS.eq(FriendStatus.CONFIRMED.name)))
                    .unionAll(
                        create.select(FRIEND.FIRST_ID).from(FRIEND)
                            .where(FRIEND.SECOND_ID.eq(id).and(FRIEND.STATUS.eq(FriendStatus.CONFIRMED.name)))
                    )
            )
        )
            .asFlow()
            .map { mapUserRecord(it) }
    }

    private fun mapUserRecord(it: Record) = User(
        it.get(USER_TABLE.ID),
        it.get(USER_TABLE.USER_ALIAS),
        it.get(USER_TABLE.PUBLIC_KEY),
        it.get(USER_TABLE.PASSWORD_HASH)
    )
}