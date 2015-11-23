package database_mapDB;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentNavigableMap;

public class UsersTable {

    public static void findUser(String login, String password, MappedDB db) throws SQLException {
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> table = db.db().treeMap("users");
        boolean found = false;
        for (HashMap<String, Object> map: table.values()) {
            if (map.containsKey(login) && map.containsValue(password))
                found = true;
        }
        if (!found)
            throw new NoSuchElementException();
        System.out.println("User found!");
    }

    public static void addUser(String login, String password, MappedDB db) throws SQLException, UserAlreadyExistsException {
        ConcurrentNavigableMap<Integer, HashMap<String, Object>> table = db.db().treeMap("users");
        for (HashMap<String, Object> map: table.values()) {
            if (map.containsKey(login))
                throw new UserAlreadyExistsException("User already exists.");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put(login, password);
        table.put(table.lastKey() + 1, map);
        System.out.println("User added!");
    }

    public static class UserAlreadyExistsException extends Exception {
        public UserAlreadyExistsException(String description) {
            super(description);
        }
    }

}
