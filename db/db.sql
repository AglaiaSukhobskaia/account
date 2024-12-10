drop table if exists accounts;

create table accounts
(
    id   int generated by default as identity primary key,
    owner varchar(100) not null unique,
    balance decimal          not null
);