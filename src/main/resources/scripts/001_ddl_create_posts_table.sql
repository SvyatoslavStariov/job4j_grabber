create table posts (
    id serial primary key,
    name varchar(100),
    text text,
    link text unique,
    created timestamp
);