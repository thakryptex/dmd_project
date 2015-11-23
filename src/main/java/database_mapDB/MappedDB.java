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
        System.out.println("Connected...");
    }

    public void disconnectFromDB() {
        db.close();
        System.out.println("Disconnected.");

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
        tables.put("written", new ArrayList<String>(Arrays.asList("pubid", "name")));
        List<String> list = new ArrayList<>(Arrays.asList("written", "article", "book", "inproceeding", "proceedings", "incollection"));
        list.forEach(t -> tables.put(t, new ArrayList<>(Arrays.asList("pubid"))));

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
                    ConcurrentNavigableMap<Object, List> index = db.treeMap("index_" + key + "_" + s);
                    try {
                        Object obj = row.getValue().get(s);
                    } catch (NullPointerException e) {
                        continue;
                    }
                    Object val = row.getValue().get(s);
                    if (!index.containsKey(val))
                        index.put(val, new ArrayList<>(Arrays.asList(rowKey)));
                    else
                        index.get(val).add(rowKey);
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

    public List search(HashMap<String, Object> where) throws SQLException {
        System.out.println("Entering into search method...");
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> pblctn = db.treeMap("publication");
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> wrttn = db.treeMap("written");
        List<HashMap> join = naturaljoin(pblctn, wrttn, where);
        System.out.println("End of search method.");
        return join;
    }

    public HashMap getPubInfo(int pubid) throws SQLException {
        System.out.println("Entering into get publication method...");
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> pblctn = db.treeMap("publication");
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> wrttn = db.treeMap("written");
        ConcurrentNavigableMap<Object, List<Integer>> index = db.treeMap("index_publication_pubid");
        int key = index.get(pubid).get(0);
        String type = null;
        for (Map.Entry entry: pblctn.get(key).entrySet()) {
            if (entry.getKey().equals("type"))
                type = String.valueOf(entry.getValue());
        }
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> table = db.treeMap(type);
        HashMap join = naturaljoin(pblctn, "publication", wrttn, "written", table, type, pubid);
        System.out.println("End of get publication method.");
        return join;
    }

    public void delete(int pubid) throws SQLException {
        System.out.println("Entering into deletion method...");
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> pblctn = db.treeMap("publication");
        ConcurrentNavigableMap<Object, List<Integer>> index1 = db.treeMap("index_publication_pubid");
        int key1 = index1.get(pubid).get(0);
        String type = null;
        for (Map.Entry entry: pblctn.get(key1).entrySet()) {
            if (entry.getKey().equals("type"))
                type = String.valueOf(entry.getValue());
        }
        if (type != null) {
            ConcurrentNavigableMap<Integer, HashMap<String, Object>> table = db.treeMap(type);
            ConcurrentNavigableMap<Object, List<Integer>> index2 = db.treeMap("index_" + type + "_pubid");
            int key2 = index2.get(pubid).get(0);
            pblctn.remove(key1);
            table.remove(key2);
        }

        System.out.println("End of deletion method.");
    }

    private List<HashMap> naturaljoin(ConcurrentNavigableMap<Integer, HashMap<String, Object>> t1, ConcurrentNavigableMap<Integer, HashMap<String, Object>> t2, HashMap<String, Object> where) {
        System.out.println("Entering into natural join method...");
        List<HashMap> join = new ArrayList<>();
        ConcurrentNavigableMap<Object, List<Integer>> index1 = db.treeMap("index_publication_pubid");
        ConcurrentNavigableMap<Object, List<Integer>> index2 = db.treeMap("index_written_pubid");
        int i = 1;

        // cycle of entry sets of first table
        for (Map.Entry<Object, List<Integer>> entry: index1.entrySet()) {
            // map of one row
            HashMap<Integer, HashMap> map = null;

            // if second table has key from this entry
            if (index2.containsKey(entry.getKey())) {
                map = new HashMap<>();
                // values of this row
                HashMap<String, Object> values = t1.get(entry.getValue().get(0));

                List list = index2.get(entry.getKey());
                List names = new ArrayList<>();
                for (Object p: list) {
                    names.add(t2.get(p).get("name"));
                }
                values.put("name", names);

                if (satisfies(where, values))
                    map.put(entry.getValue().get(0), values);
                else
                    map = null;
            }

            if (map != null) {
                join.add(map);
            }
            System.out.println(i++);
        }

        System.out.println("End of natural join method.");
        return join;
    }

    private HashMap naturaljoin(ConcurrentNavigableMap<Integer, HashMap<String, Object>> t1, String name1, ConcurrentNavigableMap<Integer, HashMap<String, Object>> t2, String name2, ConcurrentNavigableMap<Integer, HashMap<String, Object>> t3, String name3, int id) {
        System.out.println("Entering into natural join method...");
        ConcurrentNavigableMap<Object, List<Integer>> index1 = db.treeMap("index_" + name1 + "_pubid");
        ConcurrentNavigableMap<Object, List<Integer>> index2 = db.treeMap("index_" + name2 + "_pubid");
        ConcurrentNavigableMap<Object, List<Integer>> index3 = db.treeMap("index_" + name3 + "_pubid");
        HashMap<String, Object> join = t1.get(index1.get(id).get(0));

        List<Integer> list = index2.get(id);
        List names = new ArrayList<>();
        for (Object p: list) {
            names.add(t2.get(p).get("name"));
        }
        join.put("name", names);

        List third = index3.get(id);
        for (Object p: third) {
            join.putAll(t3.get(p));
        }

        System.out.println("End of natural join method.");
        return join;
    }

    private boolean satisfies(HashMap<String, Object> where, HashMap<String, Object> values) {
        for (String col: where.keySet()) {
            if (col.equals("pubid") || col.equals("year")) {
                if (!values.get(col).equals(where.get(col)))
                    return false;
            } else if (col.equals("name")) {
                List<String> list = (List) values.get(col);
                for (String s: list) {
                    if (!s.toLowerCase().contains(String.valueOf(where.get(col)).toLowerCase()))
                        return false;
                }
            } else {
                String val = String.valueOf(values.get(col)).toLowerCase();
                if (!val.contains(String.valueOf(where.get(col)).toLowerCase()))
                    return false;
            }
        }
        return true;
    }

    private String findSharedColumn(HashMap<String, Object> list1, HashMap<String, Object> list2) {
        for (String o: list1.keySet()) {
            if (list2.containsKey(o))
                return o;
        }
        return null;
    }

    public DB db() {
        return db;
    }
}