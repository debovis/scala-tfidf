# $Id: ontodb_rel_reverse.sql,v 1.1 2008/06/11 17:24:51 spal Exp $
# $Source: /export/cvsrepository/dev/people/spal/jrocker/src/main/sql/ontodb_rel_reverse.sql,v $
# Adding in negative relations to traverse links backwards.
#
delete from relations where id < 0;

insert into relations(id,name) values (-1,'superclassOf');
insert into relations(id,name) values (-2,'contains');
insert into relations(id,name) values (-3,'makes');
insert into relations(id,name) values (-4,'sugarProperty');
insert into relations(id,name) values (-5,'flavorProperty');
insert into relations(id,name) values (-6,'bodyProperty');
insert into relations(id,name) values (-7,'mainIngredient');
insert into relations(id,name) values (-8,'vintageYearProperty');
insert into relations(id,name) values (-9,'colorProperty');
