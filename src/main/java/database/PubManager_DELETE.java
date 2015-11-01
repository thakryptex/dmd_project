package database;

import spark.*;

import java.sql.*;

import static spark.Spark.*;

public class PubManager_DELETE {

    private static final String url = "jdbc:postgresql://localhost:5432/dmdproject";
    private static final String user = "dmd";
    private static final String pass = "dmd";
    private static Connection connection;
    private static Statement statement;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(url, user, pass);
        statement = connection.createStatement();
    }

    public void disconnect() throws SQLException {
        connection.close();
        statement.close();
    }

    public String executeQuery(String query) {
        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next())
                return rs.getString("title");
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPublication(Request req, Response res) {
        String request = "SELECT * FROM publication limit 2";
        return executeQuery(request);

//        String type = req.params(":type");
//        String number = req.params(":number");
//        return "You requested a publication by " + type + " and number " + number;
    }
}
