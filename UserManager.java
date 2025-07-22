import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class UserManager {
    private static final String USERS_FILE = "users.dat";
    private Map<String, User> users;


    public UserManager() {
        users = new HashMap<>();
        loadUsers();
    }


    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // Username already exists
        }
        users.put(username, new User(username, password));
        saveUsers();
        return true;
    }


    public boolean login(String username, String password) {
        User user = users.get(username);
        return user != null && user.checkPassword(password);
    }


    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (Map<String, User>) ois.readObject();
        } catch (FileNotFoundException e) {
            // First run - no users file yet
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

