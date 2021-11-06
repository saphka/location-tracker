package org.saphka.location.tracker.user.dao

import org.jooq.DSLContext
import org.jooq.Record
import org.saphka.location.tracker.user.dao.jooq.Tables.USER_TABLE
import org.saphka.location.tracker.user.model.User
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserDAO {

    fun findUser(id: Int): Mono<User>

    fun findUser(alias: String): Mono<User>

    fun createUser(alias: String, publicKey: ByteArray, passwordHash: String): Mono<User>

    fun updateUser(user: User): Mono<User>
}

@Service
class UserDAOImpl(private val create: DSLContext) : UserDAO {
    override fun findUser(id: Int): Mono<User> {
        return Flux.from(
            create.select().from(USER_TABLE).where(USER_TABLE.ID.eq(id))
        ).next().map { mapUserRecord(it) }
    }

    override fun findUser(alias: String): Mono<User> {
        return Flux.from(
            create.select().from(USER_TABLE).where(USER_TABLE.USER_ALIAS.eq(alias))
        ).next().map { mapUserRecord(it) }
    }

    override fun createUser(alias: String, publicKey: ByteArray, passwordHash: String): Mono<User> {
        return Mono.from(
            create.insertInto(USER_TABLE).columns(USER_TABLE.USER_ALIAS, USER_TABLE.PUBLIC_KEY, USER_TABLE.PASSWORD_HASH)
                .values(alias, publicKey, passwordHash)
                .returning()
        ).map { mapUserRecord(it) }
    }

    override fun updateUser(user: User): Mono<User> {
        return Mono.from(
            create.update(USER_TABLE)
                .set(USER_TABLE.USER_ALIAS, user.alias)
                .set(USER_TABLE.PUBLIC_KEY, user.publicKey)
                .returning()
        ).map { mapUserRecord(it) }
    }

    private fun mapUserRecord(it: Record) = User(
        it.get(USER_TABLE.ID),
        it.get(USER_TABLE.USER_ALIAS),
        it.get(USER_TABLE.PUBLIC_KEY),
        it.get(USER_TABLE.PASSWORD_HASH)
    )
}