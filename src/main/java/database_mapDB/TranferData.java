package database_mapDB;

import database_pgsql.DBWorker;

import java.sql.SQLException;

public class TranferData {

    public static DBWorker psql;
    public static MappedDB mapDB;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

//        psql = new DBWorker();
//        psql.connectToDB();

        mapDB = new MappedDB();
        mapDB.connectToDB("publib.db");

        mapDB.copyTablesAndDataFrom(psql);

//        table.values().forEach(System.out::println);

        mapDB.disconnectFromDB();

    }
}
