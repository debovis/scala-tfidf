# $Id: tmdb_load.sql,v 1.1 2008/06/11 17:24:51 spal Exp $
# $Source: /export/cvsrepository/dev/people/spal/jrocker/src/main/sql/tmdb_load.sql,v $
delete from co_abbrev where 1=1;
load data local infile 'co_abbrev.dat' replace into table co_abbrev fields terminated by ':' escaped by '!';
delete from co_colloc where 1=1;
load data local infile 'co_colloc.dat' replace into table co_colloc fields terminated by ':' escaped by '!';
delete from co_colloc_lead where 1=1;
load data local infile 'co_colloc_lead.dat' replace into table co_colloc_lead fields terminated by ':' escaped by '!';
delete from co_entity where 1=1;
load data local infile 'co_entity.dat' replace into table co_entity fields terminated by ':' escaped by '!';
delete from co_org where 1=1;
load data local infile 'co_org.dat' replace into table co_org fields terminated by ':' escaped by '!';
delete from co_person where 1=1;
load data local infile 'co_person.dat' replace into table co_person fields terminated by ':' escaped by '!';
delete from co_place where 1=1;
load data local infile 'co_place.dat' replace into table co_place fields terminated by ':' escaped by '!';
delete from co_ruleent where 1=1;
load data local infile 'co_ruleent.dat' replace into table co_ruleent fields terminated by ':' escaped by '!';
delete from co_rulepos where 1=1;
load data local infile 'co_rulepos.dat' replace into table co_rulepos fields terminated by ':' escaped by '!';
delete from ne_sources where 1=1;
load data local infile 'ne_sources.dat' replace into table ne_sources fields terminated by ':' escaped by '!';
delete from qu_categories where 1=1;
load data local infile 'qu_categories.dat' replace into table qu_categories fields terminated by ':' escaped by '!';
delete from qu_perlfaq where 1=1;
load data local infile 'qu_perlfaq.dat' replace into table qu_perlfaq fields terminated by ':' escaped by '!';
delete from wn_exc_words where 1=1;
load data local infile 'wn_exc_words.dat' replace into table wn_exc_words fields terminated by ':' escaped by '!';
delete from wn_synsets where 1=1;
load data local infile 'wn_synsets.dat' replace into table wn_synsets fields terminated by ':' escaped by '!';
delete from wn_synsets_rel where 1=1;
load data local infile 'wn_synsets_rel.dat' replace into table wn_synsets_rel fields terminated by ':' escaped by '!';
delete from wn_words_a_b where 1=1;
load data local infile 'wn_words_a_b.dat' replace into table wn_words_a_b fields terminated by ':' escaped by '!';
delete from wn_words_all where 1=1;
load data local infile 'wn_words_all.dat' replace into table wn_words_all fields terminated by ':' escaped by '!';
delete from wn_words_c_d where 1=1;
load data local infile 'wn_words_c_d.dat' replace into table wn_words_c_d fields terminated by ':' escaped by '!';
delete from wn_words_e_g where 1=1;
load data local infile 'wn_words_e_g.dat' replace into table wn_words_e_g fields terminated by ':' escaped by '!';
delete from wn_words_h_l where 1=1;
load data local infile 'wn_words_h_l.dat' replace into table wn_words_h_l fields terminated by ':' escaped by '!';
delete from wn_words_m_p where 1=1;
load data local infile 'wn_words_m_p.dat' replace into table wn_words_m_p fields terminated by ':' escaped by '!';
delete from wn_words_q_s where 1=1;
load data local infile 'wn_words_q_s.dat' replace into table wn_words_q_s fields terminated by ':' escaped by '!';
delete from wn_words_rel where 1=1;
load data local infile 'wn_words_rel.dat' replace into table wn_words_rel fields terminated by ':' escaped by '!';
delete from wn_words_t_z where 1=1;
load data local infile 'wn_words_t_z.dat' replace into table wn_words_t_z fields terminated by ':' escaped by '!';



