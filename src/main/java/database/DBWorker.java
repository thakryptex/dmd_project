package database;

import java.sql.*;
import java.util.*;

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

    public List search(HashMap<String, Object> map) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("select ");
        Set<String> columns = map.keySet();
        columns.forEach(c -> {
            query.append(c + ", ");
        });
        query.delete(query.length() - 2, query.length() - 1);

        query.append("from publication natural join person where ");

        map.entrySet().forEach(set -> {
            String key = set.getKey();
            String value = set.getValue().toString();
            query.append(key);
            if (key.equals("pubid") || key.equals("year")) {
                query.append("=");
                query.append(value);
            } else {
                query.append(" like ");
                query.append("'%" + value + "%'");
            }
            query.append(" and ");
        });
        query.delete(query.length() - 5, query.length());
        query.append(";");

        ResultSet resultSet = statement.executeQuery(query.toString());
        ResultSetMetaData meta = resultSet.getMetaData();
        int size = meta.getColumnCount();
        List<HashMap> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            HashMap pub = new HashMap();
            pub.put(meta.getColumnName(i), resultSet.getString(i));
            list.add(pub);
        }

        return list;
    }

    public Statement statement() {
        return statement;
    }
}
