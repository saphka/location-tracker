package org.saphka.locationtracker.dao;

import org.saphka.locationtracker.dao.jooq.tables.records.UsersRecord;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<UsersRecord> getUserByIAlias(String alias);

}
