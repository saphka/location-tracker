package org.saphka.location.tracker.user.dao

import org.jooq.DSLContext
import org.jooq.Record
import org.saphka.location.tracker.user.dao.jooq.Tables.USER
import org.saphka.location.tracker.user.model.User
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserDAO {

    fun findUser(id: Int): Mono<User>

    fun findUser(alias: String): Mono<User>

    fun createUser(user: User): Mono<User>

}

@Service
class UserDAOImpl(private val create: DSLContext) : UserDAO {
    override fun findUser(id: Int): Mono<User> {
        return Flux.from(
            create.select().from(USER).where(USER.ID.eq(id))
        ).next().map { mapUserRecord(it) }
    }

    override fun findUser(alias: String): Mono<User> {
        return Flux.from(
            create.select().from(USER).where(USER.USER_ALIAS.eq(alias))
        ).next().map { mapUserRecord(it) }
    }

    override fun createUser(user: User): Mono<User> {
        return Mono.from(
            create.insertInto(USER).columns(USER.ID, USER.USER_ALIAS, USER.PUBLIC_KEY, USER.PASSWORD_HASH)
                .values(user.id, user.alias, user.publicKey, user.passwordHash)
                .returning()
        ).map { mapUserRecord(it) }
    }

    private fun mapUserRecord(it: Record) = User(
        it.get(USER.ID),
        it.get(USER.USER_ALIAS),
        it.get(USER.PUBLIC_KEY),
        it.get(USER.PASSWORD_HASH)
    )
}