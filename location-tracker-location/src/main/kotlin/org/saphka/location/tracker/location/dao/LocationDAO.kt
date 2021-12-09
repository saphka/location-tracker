package org.saphka.location.tracker.location.dao

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.saphka.location.tracker.location.dao.jooq.Tables.LOCATION
import org.saphka.location.tracker.location.model.Location
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

interface LocationDAO {
    fun addLocations(location: List<Location>): Flux<Location>
    fun getLocations(userId: Int): Flux<Location>
}

@Service
class LocationDAOImpl(private val create: DSLContext) : LocationDAO {
    override fun addLocations(location: List<Location>): Flux<Location> {
        return Flux.fromIterable(
            create.transactionResult { ctx ->
                val txn = DSL.using(ctx)
                location.map {
                    Mono.from(
                        txn.insertInto(LOCATION)
                            .columns(
                                LOCATION.USER_ID,
                                LOCATION.FRIEND_ID,
                                LOCATION.TIMESTAMP,
                                LOCATION.LATITUDE,
                                LOCATION.LONGITUDE
                            )
                            .values(
                                it.userId,
                                it.friendId,
                                LocalDateTime.ofInstant(it.timestamp, ZoneId.of("UTC")),
                                it.latitude,
                                it.longitude
                            )
                            .onConflictDoNothing()
                            .returning()
                    )
                }
            })
            .flatMap { it }
            .map { mapToLocation(it) }
    }

    override fun getLocations(userId: Int): Flux<Location> {
        return Flux.from(
            create.select().from(LOCATION).where(LOCATION.USER_ID.eq(userId))
        ).map { mapToLocation(it) }
    }

    private fun mapToLocation(it: Record): Location {
        return Location(
            it.get(LOCATION.ID),
            it.get(LOCATION.USER_ID),
            it.get(LOCATION.FRIEND_ID),
            it.get(LOCATION.TIMESTAMP).toInstant(ZoneOffset.UTC),
            it.get(LOCATION.LATITUDE),
            it.get(LOCATION.LONGITUDE),
        )
    }

}