package org.saphka.locationtracker.dao;

import org.saphka.locationtracker.dao.jooq.tables.records.LocationsRecord;
import reactor.core.publisher.Flux;

public interface LocationsRepository {

    Flux<LocationsRecord> getLocationsByReceiver(Integer receiverId);

}
