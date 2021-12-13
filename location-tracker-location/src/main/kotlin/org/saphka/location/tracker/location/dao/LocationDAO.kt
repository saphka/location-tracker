package org.saphka.location.tracker.location.dao

import org.jooq.DSLContext
import org.jooq.Record
import org.saphka.location.tracker.location.dao.jooq.Tables.LOCATION
import org.saphka.location.tracker.location.model.Location
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

interface LocationDAO {
    fun addLocations(location: List<Location>): Mono<Int>
    fun getLocations(userId: Int, page: Int, size: Int): Flux<Location>
}

@Service
class LocationDAOImpl(private val create: DSLContext) : LocationDAO {
    override fun addLocations(location: List<Location>): Mono<Int> {
        return Mono.from {
            create.batch(location.map {
                create.insertInto(LOCATION)
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
            })
        }
    }

    override fun getLocations(userId: Int, page: Int, size: Int): Flux<Location> {
        return Flux.from(
            create.select()
                .from(LOCATION)
                .where(LOCATION.USER_ID.eq(userId))
                .orderBy(LOCATION.TIMESTAMP.desc())
                .limit(size)
                .offset(page * size)
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