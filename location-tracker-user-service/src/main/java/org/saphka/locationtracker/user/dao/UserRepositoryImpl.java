package org.saphka.locationtracker.user.dao;

import org.jooq.DSLContext;
import org.saphka.locationtracker.user.dao.jooq.Tables;
import org.saphka.locationtracker.user.dao.jooq.tables.records.UserRecord;
import org.saphka.locationtracker.user.domain.User;
import org.saphka.locationtracker.user.domain.UserFactory;
import org.saphka.locationtracker.user.domain.UserRepository;
import org.saphka.locationtracker.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final DSLContext dslContext;
    private final UserMapper userMapper;
    private final UserFactory userFactory;

    @Autowired
    public UserRepositoryImpl(DSLContext dslContext, UserMapper userMapper, UserFactory userFactory) {
        this.dslContext = dslContext;
        this.userMapper = userMapper;
        this.userFactory = userFactory;
    }

    @Override
    public User save(User user) {
        UserRecord record = userMapper.toRecord(user, dslContext.newRecord(Tables.USER));
        record.insert();

        return userMapper.toDomain(record, userFactory.make(record.getId()));
    }
}
