create table if not exists balance
(
    id      bigserial primary key,
    version int,
    amount  bigint
);

insert into balance(id, amount, version)
values (100, 100000000, 0);
insert into balance(id, amount, version)
values (200, 0, 0);
insert into balance(id, amount, version)
values (300, 50000000, 0);