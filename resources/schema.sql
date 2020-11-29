create table users
(
	id serial not null
		constraint users_pk
			primary key,
	user_alias varchar(512) not null,
	public_key varchar(2048) not null,
	password_hash varchar(60) not null
);

create unique index users_user_alias_uindex
	on users (user_alias);

create table groups
(
	id serial not null
		constraint user_group_pk
			primary key,
	group_name varchar(1024) not null
);

create table user_group_links
(
	id serial not null
		constraint user_group_link_pk
			primary key,
	user_id integer not null
		constraint user_group_link_users_id_fk
			references users
				on update cascade on delete cascade,
	group_id integer not null
		constraint user_group_link_groups_id_fk
			references groups
				on update cascade on delete cascade,
	locations_key varchar(2048) not null,
	places_key varchar(2048) not null
);

create table places
(
	id serial not null
		constraint places_pk
			primary key,
	group_id integer not null
		constraint places_groups_id_fk
			references groups
				on update cascade on delete cascade,
	description varchar(1024) not null,
	latitude varchar(1024) not null,
	longitude varchar(1024) not null
);

create table location
(
	id serial not null
		constraint location_pk
			primary key,
	user_id integer not null
		constraint location_users_id_fk
			references users
				on update cascade on delete cascade,
	group_id integer not null
		constraint location_groups_id_fk
			references groups
				on update cascade on delete cascade,
	location_timestamp timestamp not null,
	latitude varchar(1024) not null,
	longitude varchar(1024) not null
);

