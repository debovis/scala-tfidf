-- $Id: tmdb_my_colloc.sql,v 1.1 2008/06/11 17:24:51 spal Exp $
-- $Source: /export/cvsrepository/dev/people/spal/jrocker/src/main/sql/tmdb_my_colloc.sql,v $
-- Storing phrases differently for phrase recognition
create table my_colloc (
  coc_lead_word char(30) not null,
  coc_phrase char(255) not null,
  coc_num_words int(11) not null,
  coc_prob float(8,3) default 0 not null
)engine=InnoDB;
create index ux1_my_colloc on my_colloc(coc_lead_word, coc_num_words, coc_phrase);
