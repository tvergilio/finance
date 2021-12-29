SET GLOBAL general_log_file='/var/log/mysql/mariadb.log';
SET GLOBAL general_log = 1;

create schema finance;
use finance;

create or replace table account
(
    id         bigint auto_increment
        primary key,
    student_id varchar(255) unique null
);

INSERT INTO finance.account (id, student_id) VALUES (1, 'c3781247');
INSERT INTO finance.account (id, student_id) VALUES (2, 'c3922382');

create or replace table invoice
(
    id         bigint auto_increment
        primary key,
    reference varchar(255) unique null,
    amount     double   null,
    due_date   datetime null,
    status     int      null,
    type       int      null,
    account_fk bigint   null,
    constraint FK237udfnwpogi0olckbos14vma
        foreign key (account_fk) references account (id)
);

INSERT INTO finance.invoice (id, reference, amount, due_date, status, type, account_fk) VALUES (1, 'ABCD1234', 350.4, '2022-01-06 09:29:04', 0, 1, 1);
INSERT INTO finance.invoice (id, reference, amount, due_date, status, type, account_fk) VALUES (2, 'AABB1122', 30.0, '2021-10-07 09:29:04', 1, 1, 2);
INSERT INTO finance.invoice (id, reference, amount, due_date, status, type, account_fk) VALUES (3, 'CCDD3344', 37.85, '2021-11-07 09:29:04', 2, 0, 1);
INSERT INTO finance.invoice (id, reference, amount, due_date, status, type, account_fk) VALUES (4, '99EEFF22', 1000, '2021-03-07 09:29:04', 1, 1, 1);
INSERT INTO finance.invoice (id, reference, amount, due_date, status, type, account_fk) VALUES (5, 'ZZ666666', 15.6, '2021-12-22 09:29:04', 1, 0, 2);

create or replace table account_invoice_list
(
    account_id      bigint not null,
    invoice_list_id bigint not null,
    constraint UK_bk0qum00dfjw1le2hlvd2qxlj
        unique (invoice_list_id),
    constraint FK2md7wefo9fyru31lawts2qw9b
        foreign key (invoice_list_id) references invoice (id),
    constraint FKf6bf69pytpp25ewt2dysq1muu
        foreign key (account_id) references account (id)
);

CREATE USER 'finance-spring-user'@'%' IDENTIFIED BY 'finance-secret';
GRANT ALL PRIVILEGES on finance.* to `finance-spring-user`;
FLUSH PRIVILEGES;