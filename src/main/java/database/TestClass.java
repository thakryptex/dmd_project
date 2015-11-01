package database;

import java.sql.SQLException;

public class TestClass {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        DBWorker dbWorker = new DBWorker();
        dbWorker.connectToDB();
        UsersTable.findUser("admin", "admin", dbWorker.statement());


    }

}
