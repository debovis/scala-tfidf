create table my_annotated (
  word varchar(64) not null,
  annotation varchar(16) not null,
  num_annot int(11) not null,
  primary key (word, annotation)
) type=InnoDB;

