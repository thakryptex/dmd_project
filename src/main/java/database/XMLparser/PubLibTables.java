package database.XMLparser;

import java.sql.*;

public class PubLibTables {

    public static void createTables(Statement statement) throws SQLException, ClassNotFoundException {
        StringBuilder query = new StringBuilder();
        query.append("create sequence auto_id start 1;\n" +
                "create table publication (" +
                "pubid integer not null default nextval('auto_id')," +
                "title text not null," +
                "year integer not null," +
                "addeddate date not null," +
                "doi text," +
                "note text," +
                "url text," +
                "constraint publication_pk primary key (pubid)" +
                ") with (oids=true);\n" +
                "create table article (" +
                "pubid integer not null," +
                "month text," +
                "journal text not null," +
                "volume text," +
                "constraint article_pk primary key (pubid)" +
                ") with (oids=true);\n" +
                "create table inproceeding (" +
                "pubid integer not null," +
                "month text," +
                "booktitle text," +
                "constraint inproceeding_pk primary key (pubid)" +
                ") with (oids=true);\n" +
                "create table book (" +
                "pubid integer not null," +
                "publisher text not null," +
                "isbn text not null," +
                "series text," +
                "constraint book_pk primary key (pubid)" +
                ") with (oids=true);\n" +
                "create table written (\n" +
                "pubid integer not null,\n" +
                "name text not null,\n" +
                "constraint written_pk primary key (pubid,name)\n" +
                ") with (oids=true);\n" +
                "create table incollection (\n" +
                "pubid integer not null,\n" +
                "booktitle text not null,\n" +
                "pages text,\n" +
                "constraint incollection_pk primary key (pubid)\n" +
                ") with (oids=true);\n" +
                "create table publisher (\n" +
                "name text not null,\n" +
                "constraint publisher_pk primary key (name)\n" +
                ") with (oids=true);\n" +
                "create table edited (\n" +
                "pubid integer not null,\n" +
                "name text not null,\n" +
                "constraint edited_pk primary key (pubid,name)\n" +
                ") with (oids=true);\n" +
                "create table journal (\n" +
                "journal text not null,\n" +
                "constraint journal_pk primary key (journal)\n" +
                ") with (oids=true);\n" +
                "create table person (\n" +
                "name text not null,\n" +
                "lab text,\n" +
                "constraint person_pk primary key (name)\n" +
                ") with (oids=true);\n" +
                "create table phdthesis  (\n" +
                "pubid integer not null,\n" +
                "school text not null,\n" +
                "pages text,\n" +
                "series text,\n" +
                "volume text,\n" +
                "constraint phdthesis_pk primary key (pubid)\n" +
                ") with (oids=true);\n" +
                "create table masterthesis (\n" +
                "pubid integer not null,\n" +
                "school text not null,\n" +
                "pages text,\n" +
                "series text,\n" +
                "volume text,\n" +
                "constraint masterthesis_pk primary key (pubid)\n" +
                ") with (oids=true);\n" +
                "create table proceedings (\n" +
                "pubid integer not null,\n" +
                "booktitle text not null,\n" +
                "publisher text,\n" +
                "isbn text not null,\n" +
                "series text,\n" +
                "volume text,\n" +
                "constraint proceedings_pk primary key (pubid)\n" +
                ") with (oids=true);\n" +
                "alter table article add constraint article_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table article add constraint article_fk1 foreign key (journal) references journal(journal);\n" +
                "alter table inproceeding add constraint inproceeding_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table book add constraint book_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table book add constraint book_fk1 foreign key (publisher) references publisher(name);\n" +
                "alter table written add constraint written_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table written add constraint written_fk1 foreign key (name) references person(name);\n" +
                "alter table incollection add constraint incollection_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table edited add constraint edited_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table edited add constraint edited_fk1 foreign key (name) references person(name);\n" +
                "alter table phdthesis  add constraint phdthesis_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table masterthesis add constraint masterthesis_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table proceedings add constraint proceedings_fk0 foreign key (pubid) references publication(pubid);\n" +
                "alter table proceedings add constraint proceedings_fk1 foreign key (publisher) references publisher(name);\n" +
                "create function fperson() returns trigger as '\n" +
                "begin\n" +
                "if (select count(*) from person where name = new.name) > 0\n" +
                "then new = null;\n" +
                "end if;\n" +
                "return new;\n" +
                "end;\n" +
                "' language plpgsql;\n" +
                "create trigger trigperson\n" +
                "before insert on person for each row\n" +
                "execute procedure fperson();\n" +
                "create function fpublisher() returns trigger as '\n" +
                "begin\n" +
                "if (select count(*) from publisher where name = new.name) > 0\n" +
                "then new = null;\n" +
                "end if;\n" +
                "return new;\n" +
                "end;\n" +
                "' language plpgsql;\n" +
                "create trigger trigpublisher\n" +
                "before insert on publisher for each row\n" +
                "execute procedure fpublisher();\n" +
                "create function fjournal() returns trigger as '\n" +
                "begin\n" +
                "if (select count(*) from journal where journal = new.journal) > 0\n" +
                "then new = null;\n" +
                "end if;\n" +
                "return new;\n" +
                "end;\n" +
                "' language plpgsql;\n" +
                "create trigger trigjournal\n" +
                "before insert on journal for each row\n" +
                "execute procedure fjournal();" +
                "create function fwritten() returns trigger as '\n" +
                "begin\n" +
                "if (select count(*) from written where pubid = new.pubid and name = new.name) > 0\n" +
                "then new = null;\n" +
                "end if;\n" +
                "return new;\n" +
                "end;\n" +
                "' language plpgsql;\n" +
                "create trigger trigwritten\n" +
                "before insert on written for each row\n" +
                "execute procedure fwritten();\n" +
                "create function fedited() returns trigger as '\n" +
                "begin\n" +
                "if (select count(*) from edited where pubid = new.pubid and name = new.name) > 0\n" +
                "then new = null;\n" +
                "end if;\n" +
                "return new;\n" +
                "end;\n" +
                "' language plpgsql;\n" +
                "create trigger trigedited\n" +
                "before insert on edited for each row\n" +
                "execute procedure fedited();");
        statement.executeUpdate(query.toString());
        System.out.println("All tables created!");
    }

    public static void deleteTables(Statement statement) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("drop sequence auto_id cascade;" +
                "drop table publication cascade;\n" +
                "drop table article cascade;\n" +
                "drop table book cascade;\n" +
                "drop table proceedings cascade;\n" +
                "drop table inproceeding cascade;\n" +
                "drop table written cascade;\n" +
                "drop table edited cascade;\n" +
                "drop table person cascade;\n" +
                "drop table journal cascade;\n" +
                "drop table incollection cascade;\t\n" +
                "drop table publisher cascade;\n" +
                "drop table masterthesis cascade;\n" +
                "drop table phdthesis cascade;\n" +
                "drop function fjournal() cascade;\t\n" +
                "drop function fpublisher() cascade;\n" +
                "drop function fperson() cascade;" +
                "drop function fedited() cascade;\n" +
                "drop function fwritten() cascade;");
        statement.executeUpdate(query.toString());
        System.out.println("All tables deleted!");
    }

    public static void clearTables(Statement statement) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("truncate table book cascade;\n" +
                "truncate table publication cascade;\n" +
                "truncate table article cascade;\n" +
                "truncate table book cascade;\n" +
                "truncate table proceedings cascade;\n" +
                "truncate table inproceeding cascade;\n" +
                "truncate table written cascade;\n" +
                "truncate table edited cascade;\n" +
                "truncate table person cascade;\n" +
                "truncate table journal cascade;\n" +
                "truncate table incollection cascade;\t\n" +
                "truncate table publisher cascade;\n" +
                "truncate table masterthesis cascade;\n" +
                "truncate table phdthesis cascade;\n" +
                "truncate function fjournal() cascade;\n" +
                "truncate function fpublisher() cascade;\n" +
                "truncate function fperson() cascade;" +
                "truncate function fedited() cascade;\n" +
                "truncate function fwritten() cascade;");
        statement.executeUpdate(query.toString());
        System.out.println("All tables deleted!");
    }

}
