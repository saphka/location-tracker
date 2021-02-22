package org.saphka.locationtracker.user.dao;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.saphka.locationtracker.user.dao.jooq.Tables;
import org.saphka.locationtracker.user.dao.jooq.tables.records.UserRecord;
import org.saphka.locationtracker.user.domain.User;
import org.saphka.locationtracker.user.domain.UserFactory;
import org.saphka.locationtracker.user.domain.UserRepository;
import org.saphka.locationtracker.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository, UserDetailsService {

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

        List<Field<?>> fields = Tables.USER.fieldStream()
                .filter(f -> !Tables.USER.ID.equals(f) || record.getId() != null)
                .collect(Collectors.toList());

        record.store(fields);

        return userMapper.toDomain(record, userFactory.make(record.getId()));
    }

    @Override
    public Optional<User> findByAlias(String alias) {
        return dslContext.selectFrom(Tables.USER)
                .where(Tables.USER.USER_ALIAS.eq(alias))
                .fetchOptional()
                .map(r -> userMapper.toDomain(r, userFactory.make(r.getId())));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByAlias(username)
                .map(user -> new org.springframework.security.core.userdetails.User(user.getAlias(),
                        user.getPassword(),
                        Collections.emptyList()))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
