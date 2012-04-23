-- $Id: ontodb_schema.sql,v 1.1 2008/06/11 17:24:51 spal Exp $
-- $Source: /export/cvsrepository/dev/people/spal/jrocker/src/main/sql/ontodb_schema.sql,v $
drop database ontodb;
create database ontodb;
use ontodb;

create table attribute_types (
  id int(11) auto_increment not null,
  attr_name varchar(64) not null,
  primary key(id)
) engine=InnoDB;

create table attributes (
  id int(11) auto_increment not null,
  entity_id int(11) not null,
  attr_id int(11) not null,
  value varchar(255) not null,
  primary key(id)
) engine=InnoDB;

create table entities (
  id int(11) auto_increment not null,
  name varchar(64) not null,
  primary key(id)
) engine=InnoDB;

create table relations (
  id int(11) auto_increment not null,
  name varchar(64) not null,
  primary key(id)
) engine=InnoDB;

create table facts (
  id int(11) auto_increment not null,
  src_entity_id int(11) not null,
  trg_entity_id int(11) not null,
  relation_id int(11) not null,
  primary key(id)
);
