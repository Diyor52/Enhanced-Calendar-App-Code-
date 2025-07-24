import java.io.Serializable;

//using serializable in order for object persistence
public class User implements Serializable {
    //user credentials including the username and the password
    private String username;
    private String password; //In a real app, this should be hashed

    //constructor for this class
    public User(String username, String password) {
        this.username = username;//creates new user with credentials required
        this.password = password;
    }


    public String getUsername() {
        return username;
    }//getters for the username

    //verifies if the given password for the account matches the user's actual password to login
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}

