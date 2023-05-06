-- liquibase formatted sql
-- changeset jerry:1

create table t_user
(
    id            int primary key auto_increment,
    username      varchar(30)  not null,
    password_hash varchar(100) not null,
    email         varchar(100),
    display_name  varchar(30),
    bio           varchar(64),
    created       timestamp                    default current_timestamp,
    updated       timestamp                    default current_timestamp on update current_timestamp,
    `role`        enum ('HOST','ADMIN','USER') default 'USER',
    unique (username)
);

create table t_memo
(
    id         int primary key auto_increment,
    user_id    int                                 not null,
    content    text,
    tags       varchar(128),
    visibility enum ('PUBLIC','PRIVATE','PROTECT') not null default 'PUBLIC',
    `status`   enum ('NORMAL','ARCHIVED')          not null default 'NORMAL',
    created    timestamp                                    default current_timestamp,
    updated    timestamp                                    default current_timestamp on update current_timestamp
);

create table t_tag
(
    name    varchar(50) not null,
    user_id int         not null,
    created timestamp default current_timestamp,
    updated timestamp default current_timestamp on update current_timestamp,
    primary key (name, user_id)
);

create table t_user_config
(
    user_id       int         not null,
    `key`         varchar(50) not null,
    value         text        not null,
    default_value text,
    primary key (`user_id`, `key`)
);

create table t_sys_config
(
    `key`         varchar(50) not null,
    value         text        not null,
    default_value text,
    primary key (`key`)
);

create table t_resource
(
    public_id     varchar(60)  not null primary key,
    memo_id int not null,
    user_id       int          not null,
    file_type     varchar(30)  not null,
    file_name     varchar(128) not null,
    file_hash     varchar(128) not null,
    size          bigint          not null,
    internal_path varchar(256),
    external_link varchar(256),
    storage_type  enum ('LOCAL','FILE','EXTERNAL') default 'FILE',
    created       timestamp                        default current_timestamp,
    updated       timestamp                        default current_timestamp on update current_timestamp,
    unique (file_hash)
);

insert into t_user (username, password_hash, email, bio, display_name, `role`)
    value ('admin', '$2a$10$sKRq1dB51BKam9IPO5GpSOase4L9JEjqlKLAulrnGWbw3OwBYebCy', 'yoyo@openmbox.net', 'free style!',
           'JerryWang', 'ADMIN');


-- changeset jerry:2

alter table t_tag add column memo_count int null;

-- changeset jerry:3

alter table t_resource drop index file_hash;

-- changeset jerry:4

alter table t_memo add column top varchar(1) null;

-- changeset jerry:5

INSERT INTO t_sys_config (`key`, value, default_value) VALUES('OPEN_REGISTER', 'false', 'false');
INSERT INTO t_sys_config (`key`, value, default_value) VALUES('STORAGE_TYPE', 'LOCAL', 'LOCAL');
INSERT INTO t_sys_config (`key`, value, default_value) VALUES('DOMAIN', 'http://127.0.0.1:38321', NULL);
INSERT INTO t_sys_config (`key`, value, default_value) VALUES('CORS_DOMAIN_LIST', 'http://127.0.0.1:3000', NULL);


-- changeset jerry:6

INSERT INTO t_sys_config (`key`, value, default_value) VALUES('WEBSITE_TITLE', '个人时光记录', 'false');

-- changeset jerry:7
alter table t_resource modify column storage_type varchar(20);

-- changeset jerry:8
alter table t_user modify column bio text;
alter table t_user add column avatar_url varchar(150);

-- changeset jerry:9
INSERT INTO t_sys_config (`key`, value, default_value) VALUES('QINIU_PARAM', '{}','{}');

-- changeset jerry:10
alter table t_tag drop primary key;
alter table t_tag add column id int primary key auto_increment;
alter table t_tag add unique index tag_IDX (user_id,name);

-- changeset jerry:11
create table t_dev_token(
    id int primary key auto_increment,
    name varchar(20) unique not null ,
    token varchar(120) not null
);
-- changeset jerry:12
alter table t_dev_token modify token varchar(512);