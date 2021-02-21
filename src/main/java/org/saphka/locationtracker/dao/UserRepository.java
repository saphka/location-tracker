package org.saphka.locationtracker.dao;

import org.saphka.locationtracker.dao.jooq.tables.records.UserRecord;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<UserRecord> getUserByAlias(String alias);

    Mono<UserRecord> create(UserRecord newRecord);
}
