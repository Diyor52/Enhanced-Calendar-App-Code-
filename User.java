import java.io.Serializable;


public class User implements Serializable {
    private String username;
    private String password; //In a real app, this should be hashed


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }


    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}

