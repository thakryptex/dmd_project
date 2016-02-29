package database_mapDB;

import database_pgsql.DBWorker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class TranferData {

    public static DBWorker psql;
    public static MappedDB mapDB;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

//        psql = new DBWorker();
//        psql.connectToDB();

        mapDB = new MappedDB();
        mapDB.connectToDB("publib.db");

        try {

//            mapDB.copyTablesAndDataFrom(psql);
//            printTest("publication", "users", "written", "article", "book", "publisher", "index_publication_title", "index_written_name");
//            testUserTable(mapDB);

//            System.out.println(mapDB.db().treeMap("tables_info").toString());
            testSearch();
//            testDelete(50895);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mapDB.disconnectFromDB();
        }
    }

    private static void testUserTable(MappedDB mapDB) throws SQLException {
        UsersTable.findUser("admin", "admin", mapDB);
    }

    public static void printTest(String... names) {
        ConcurrentNavigableMap<String, List> table = mapDB.db().treeMap("tables_info");
        System.out.println(table.toString());

        for (String s: names) {
            ConcurrentNavigableMap<Integer, HashMap> t = mapDB.db().treeMap(s);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 50; i++) {
                sb.append(t.get(i).toString());
                if (i != 50) sb.append(",  ");
                else sb.append(".");

                if (i >= t.size()) break;
            }
            System.out.println(sb.toString());
        }
    }

    private static void testSearch() throws SQLException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("title", "data");
        List list = mapDB.search(map);
    }

    private static void testDelete(int i) throws SQLException {
        mapDB.delete(i);
        testSearch();
    }


}
