import java.io.*;
import java.util.HashMap;
import java.util.Map;

//used to check registration and if the user is the actual user
public class UserManager {
    //stores the user data in a file
    private static final String USERS_FILE = "users.dat";
    private Map<String, User> users;//memory storage of the users

    //constructor here
    public UserManager() {
        users = new HashMap<>();//empty user map here
        loadUsers();//loads the existing users in
    }

    //registers a new user
    public boolean register(String username, String password) {
        if (users.containsKey(username)) {//if the email already exists cannot allow it
            return false;
        }

        users.put(username, new User(username, password));//makes the new user
        saveUsers();//saves it
        return true;
    }

    //logs the user in now
    public boolean login(String username, String password) {
        User user = users.get(username);//gets the email that is given and only lets them in if the user has an account and the password matches it
        return user != null && user.checkPassword(password);
    }

    //loads the existing users in from the disk
    private void loadUsers() {
        //uses the file and the hashmap in order to cast the user map
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (Map<String, User>) ois.readObject();
        } catch (FileNotFoundException e) {
            //file does not exist on the first every run
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //saves the users into disk
    private void saveUsers() {
        //writes the user to the file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

