package database;

import java.sql.*;

public class DBWorker {

    private final String url = "jdbc:postgresql://localhost:5432/dmdproject";
    private final String user = "dmd";
    private final String pass = "dmd";
    private Connection connection;
    private Statement statement;


    public void connectToDB() throws SQLException, ClassNotFoundException {
        connection = DriverManager.getConnection(url, user, pass);
        statement = connection.createStatement();
    }

    public void disconnectToDB() throws SQLException {
        statement.close();
        connection.close();
    }

    public void executeUpdate(String query) throws SQLException {
        statement.executeUpdate(query);
        System.out.println("Successful execution!");
    }

    public ResultSet executeQuery(String query) throws SQLException {
        ResultSet res = statement.executeQuery(query);
        System.out.println("Successful query!");
        return res;
    }

    public void createTables() throws SQLException, ClassNotFoundException {
        StringBuilder query = new StringBuilder();
        query.append("create sequenc");
        statement.executeUpdate(query.toString());
        System.out.println("All tables created!");
    }

    public Statement statement() {
        return statement;
    }
}
