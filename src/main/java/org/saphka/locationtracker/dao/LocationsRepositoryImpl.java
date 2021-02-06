package org.saphka.locationtracker.dao;

import org.jooq.DSLContext;
import org.saphka.locationtracker.dao.jooq.Tables;
import org.saphka.locationtracker.dao.jooq.tables.records.LocationsRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.stream.Stream;

@Repository
public class LocationsRepositoryImpl implements LocationsRepository {

    @Qualifier("jdbcScheduler")
    private final Scheduler scheduler;

    private final DSLContext dslContext;

    @Autowired
    public LocationsRepositoryImpl(Scheduler scheduler, DSLContext dslContext) {
        this.scheduler = scheduler;
        this.dslContext = dslContext;
    }

    @Override
    public Flux<LocationsRecord> getLocationsByReceiver(Integer receiverId) {
        return Mono.fromCallable(() -> dslContext
                .selectFrom(Tables.LOCATIONS)
                .where(Tables.LOCATIONS.RECEIVER_ID.eq(receiverId))
                .fetchArray())
                .flatMapMany(Flux::fromArray)
                .subscribeOn(scheduler)
                .publishOn(Schedulers.parallel());
    }


}
