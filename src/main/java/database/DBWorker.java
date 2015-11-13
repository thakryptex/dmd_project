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
        query.append("select p.pubid, title, w.name, year, type from publication p left join written w on p.pubid = w.pubid where ");

        String name = null;
        for (Map.Entry<String, Object> set: map.entrySet()) {
            String key = set.getKey();
            String value = set.getValue().toString();
//            if (!key.equals("name"))
                query.append(key);
//            if (key.equals("name")) {
//                name = value;
//            } else
            if(key.equals("pubid") || key.equals("year")) {
                query.append("=");
                query.append(value);
            } else {
                query.append(" like ");
                query.append("'%" + value + "%'");
            }
//            if (!key.equals("name"))
                query.append(" and ");
        }
        query.delete(query.length() - 5, query.length());
        query.append(" limit 500;");

//        if (name != null)
//            query.append(" where w.name like '%" + name + "%'");
//        query.append(";");

        System.out.println(query.toString());

        List<HashMap> list = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery(query.toString());
        ResultSetMetaData metaData = resultSet.getMetaData();
        int colCount = metaData.getColumnCount();
        Object prevID = null;
        List authors = null;

        while (resultSet.next()) {
            HashMap columns = new HashMap();

            if (!resultSet.getObject(1).equals(prevID)) {

                for (int i = 1; i <= colCount; i++) {
                    if (i == 3) {
                        authors = new ArrayList<>();
                        authors.add(resultSet.getObject(i));
                        columns.put(metaData.getColumnLabel(i), authors);
                    } else {
                        columns.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                    }
                }
                list.add(columns);
            } else {
                authors.add(resultSet.getObject(3));
                list.get(list.size()-1).put(metaData.getColumnLabel(3), authors);
            }
            prevID = resultSet.getObject(1);
        }

        return list;
    }

    public HashMap getPubInfo(int pubid) throws SQLException {
        ResultSet getType = statement.executeQuery("select type from publication where pubid=" + pubid);
        String type = null;
        while (getType.next()) {
            type = getType.getObject(1).toString();
        }
        getType.close();

        StringBuilder sb = new StringBuilder();
        sb.append("select p.pubid, title, name, year, type, doi ");

        switch (type) {
            case "article":
                sb.append("journal, volume, month");
                break;
            case "book":
                sb.append("publisher, isbn, series");
                break;
            case "proceedings":
                sb.append("booktitle, publisher, isbn, series, volume");
                break;
            case "inproceeding":
                sb.append("booktitle, month");
                break;
            case "incollection":
                sb.append("booktitle, pages");
                break;
        }

        sb.append(" from publication p natural join " + type + " left join written w on p.pubid = w.pubid where p.pubid=" + pubid + ";");

        ResultSet pubInfo = statement.executeQuery(sb.toString());
        ResultSetMetaData metaData = pubInfo.getMetaData();
        int colCount = metaData.getColumnCount();
        HashMap pub = new HashMap();
        Object prevID = null;
        List authors = null;

        while (pubInfo.next()) {

            if (!pubInfo.getObject(1).equals(prevID)) {

                for (int i = 1; i <= colCount; i++) {
                    if (i == 3) {
                        authors = new ArrayList<>();
                        authors.add(pubInfo.getObject(i));
                        pub.put(metaData.getColumnLabel(i), authors);
                    } else {
                        pub.put(metaData.getColumnLabel(i), pubInfo.getObject(i));
                    }
                }
            } else {
                authors.add(pubInfo.getObject(3));
                pub.put(metaData.getColumnLabel(3), authors);
            }

            prevID = pubInfo.getObject(1);
        }

        return pub;
    }

    public Statement statement() {
        return statement;
    }
}
