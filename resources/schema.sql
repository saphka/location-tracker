create table users
(
    id serial not null
        constraint users_pk
            primary key,
    user_alias varchar(512) not null,
    public_key varchar(2048) not null,
    password_hash varchar(60) not null
);

alter table users owner to postgres;

create unique index users_user_alias_uindex
	on users (user_alias);

create table groups
(
    id serial not null
        constraint groups_pk
            primary key,
    group_name varchar(1024) not null,
    color varchar(6),
    owner_id integer not null
        constraint groups_owner_id_fk
            references users
            on update cascade on delete cascade
);

alter table groups owner to postgres;

create index groups_owner_id_index
	on groups (owner_id);

create table places
(
    id serial not null
        constraint places_pk
            primary key,
    owner_id integer not null
        constraint places_owner_id_fk
            references users
            on update cascade on delete cascade,
    description varchar(1024) not null,
    latitude varchar(1024) not null,
    longitude varchar(1024) not null
);

alter table places owner to postgres;

create index places_owner_id_index
	on places (owner_id);

create table locations
(
    id serial not null
        constraint locations_pk
            primary key,
    sender_id integer not null
        constraint location_sender_id_fk
            references users
            on update cascade on delete cascade,
    receiver_id integer not null
        constraint location_receiver_id_fk
            references users
            on update cascade on delete cascade,
    location_timestamp timestamp not null,
    latitude varchar(1024) not null,
    longitude varchar(1024) not null
);

alter table locations owner to postgres;

create index locations_receiver_id_location_timestamp_index
	on locations (receiver_id, location_timestamp);

create index locations_sender_id_location_timestamp_index
	on locations (sender_id, location_timestamp);

create table user_group_links
(
    id serial not null
        constraint user_group_links_pk
            primary key,
    member_id integer not null
        constraint user_group_link_member_id_fk
            references users
            on update cascade on delete cascade,
    group_id integer not null
        constraint user_group_link_group_id_fk
            references groups
            on update cascade on delete cascade
);

alter table user_group_links owner to postgres;

create index user_group_links_group_id_index
	on user_group_links (group_id);

