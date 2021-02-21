package org.saphka.locationtracker.dao;

import org.jooq.DSLContext;
import org.saphka.locationtracker.dao.jooq.Tables;
import org.saphka.locationtracker.dao.jooq.tables.records.UserRecord;
import org.saphka.locationtracker.dao.util.AsyncHelper;
import org.saphka.locationtracker.exception.ErrorCodeException;
import org.saphka.locationtracker.exception.ErrorHandler;
import org.saphka.locationtracker.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final DSLContext dslContext;
    private final AsyncHelper async;
    private final MessageSource messageSource;
    private final UserMapper mapper;

    @Autowired
    public UserRepositoryImpl(DSLContext dslContext, AsyncHelper async, MessageSource messageSource, UserMapper mapper) {
        this.dslContext = dslContext;
        this.async = async;
        this.messageSource = messageSource;
        this.mapper = mapper;
    }

    @Override
    public Mono<UserRecord> getUserByAlias(String alias) {
        return async.from(() -> dslContext.selectFrom(Tables.USER)
                .where(Tables.USER.USER_ALIAS.eq(alias))
                .fetchOptional()
                .orElseThrow(() -> new ErrorCodeException(
                        ErrorHandler.USER_NOT_FOUND,
                        messageSource.getMessage(
                                "user.not.found",
                                new Object[]{alias},
                                LocaleContextHolder.getLocale())
                )));
    }

    @Override
    public Mono<UserRecord> create(UserRecord newRecord) {
        return async.from(() -> {
            UserRecord record = mapper.copySource(newRecord, dslContext.newRecord(Tables.USER));
            record.insert();
            return record;
        });
    }
}
