package org.saphka.locationtracker.dao;

import org.saphka.locationtracker.dao.jooq.tables.records.UsersRecord;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<UsersRecord> getUserByAlias(String alias);

    Mono<UsersRecord> create(UsersRecord newRecord);
}
