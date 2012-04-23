-- $Id: ontodb_journal.sql,v 1.1 2008/06/11 17:24:51 spal Exp $
-- $Source: /export/cvsrepository/dev/people/spal/jrocker/src/main/sql/ontodb_journal.sql,v $
drop table users;
drop table journal;

create table users (
  id int(11) auto_increment not null,
  name varchar(32) not null,
  primary key(id)
) engine=InnoDB;
insert into users(name) values ('sujit');

create table journal (
  log_date timestamp default now() not null,
  user_id int(11) not null,
  tx_id int(11) not null,
  args varchar(64) not null,
  primary key(log_date, user_id, tx_id)
) engine=InnoDB;

