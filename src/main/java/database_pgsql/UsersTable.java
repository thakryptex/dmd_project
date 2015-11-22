package database_pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

public class UsersTable {

    public static void createTable(Statement statement) throws SQLException, ClassNotFoundException {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE public.users\n" +
                "(login TEXT PRIMARY KEY NOT NULL,\n" +
                "password TEXT NOT NULL);\n" +
                "ALTER TABLE public.users\n" +
                "ADD CONSTRAINT unique_login UNIQUE (login);" +
                "INSERT INTO users VALUES ('admin', 'admin');");
        statement.executeUpdate(query.toString());
        System.out.println("Users table created!");
    }

    public static void findUser(String login, String password, Statement statement) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM users WHERE login='" + login +"' and password='" + password + "';");
        ResultSet resultSet = statement.executeQuery(query.toString());
        boolean found = resultSet.next();
        if (!found)
            throw new NoSuchElementException();
        System.out.println("User found!");
    }

    public static void addUser(String login, String password, Statement statement) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO users VALUES ('" + login +"', '" + password + "');");
        statement.executeUpdate(query.toString());
        System.out.println("User added!");
    }

    public static void deleteUser(String login, Statement statement) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM users WHERE login='" + login +"';");
        statement.executeUpdate(query.toString());
        System.out.println("User deleted!");
    }

}
