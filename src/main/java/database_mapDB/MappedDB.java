package database_mapDB;

import database_pgsql.DBWorker;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

public class MappedDB {

    private DB db;
    private File file;

    public void connectToDB(String name) {
        file = new File(name);
        db = DBMaker.fileDB(file)
                .closeOnJvmShutdown()
                .make();
    }

    public void disconnectFromDB() {
        db.close();
    }

    public void copyTablesAndDataFrom(DBWorker psql) throws SQLException {
        copyTablesInfo(psql);
        copyData(psql);
        createIndexes();
    }

    private void copyTablesInfo(DBWorker psql) throws SQLException {
        String tables_names = "select table_name from information_schema.tables WHERE table_schema='public';";
        String tables_columns = "select table_name, column_name from information_schema.columns where table_schema='public'";
        ConcurrentNavigableMap<String, List> tables_info = db.treeMap("tables_info");
        ResultSet resultSet;
        ResultSetMetaData metaData;
        int colCount;

        resultSet = psql.executeQuery(tables_names);
        metaData = resultSet.getMetaData();
        colCount = metaData.getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= colCount; i++) {
                tables_info.put((String) resultSet.getObject(i), new ArrayList<>());
            }
        }

        resultSet = psql.executeQuery(tables_columns);
        metaData = resultSet.getMetaData();
        colCount = metaData.getColumnCount();
        while (resultSet.next()) {
            List cols = tables_info.get((String) resultSet.getObject(1));
            cols.add((String) resultSet.getObject(2));
        }

        db.commit();
    }

    private void copyData(DBWorker psql) throws SQLException {
        List tables = new ArrayList<>(db.treeMap("tables_info").keySet());

        for (Object o: tables) {
            String name = (String) o;
            String query = "select * from " + name;
            ConcurrentNavigableMap<Integer, HashMap> table = db.treeMap(name);
            ResultSet resultSet = psql.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int colCount = metaData.getColumnCount();

            for (int row = 1; resultSet.next(); row++) {
                HashMap map = new HashMap();
                for (int i = 1; i <= colCount; i++) {
                    map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                table.put(row, map);
                System.out.println(row);
                if (row % 100000 == 0) {
                    db.commit();
                }

                //TODO delete this
                if (row == 100000) break;
                //TODO delete this

            }
            System.out.println("Table " + name + " copied.");
            db.commit();
            resultSet.close();
            try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void createIndexes() {
        HashMap<String, List<String>> tables = new HashMap<>();
        tables.put("publication", new ArrayList<String>(Arrays.asList("title", "year", "pubid", "type")));
        tables.put("written", new ArrayList<String>(Arrays.asList("name")));

        System.out.println(tables.toString());

        // cycle for tables that need to be indexed
        for (Map.Entry<String, List<String>> table: tables.entrySet()) {
            String key = table.getKey();
            ConcurrentNavigableMap<Integer, HashMap> rowMap = db.treeMap(key);
            System.out.println("Indexing table: " + key);

            // cycle for all rows of the table
            for (Map.Entry<Integer, HashMap> row: rowMap.entrySet()) {
                Integer rowKey = row.getKey();

                // cycle for searched columns of table
                for (String s: table.getValue()) {
                    ConcurrentNavigableMap<Object, Integer> index = db.treeMap("index_" + key + "_" + s);
                    try {
                        Object obj = row.getValue().get(s);
                    } catch (NullPointerException e) {
                        continue;
                    }
                    index.put(row.getValue().get(s), rowKey);
                }

                System.out.println("Indexing row: " + rowKey);

                if (rowKey % 100000 == 0) {
                    db.commit();
                }
            }
            db.commit();
        }
        System.out.println("Indexing is over.");
    }

    public DB db() {
        return db;
    }
}