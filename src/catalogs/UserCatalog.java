package catalogs;

import java.util.ArrayList;
import domain.User;

public class UserCatalog {

    private static UserCatalog instance;
    private ArrayList<User> userCatalog;

    private UserCatalog() {
        userCatalog = new ArrayList<User>();
    }

    public static UserCatalog getUserCatalog() {
        if (instance == null)
            instance = new UserCatalog();

        return instance;
    }

    public User getUserByID(String id) {

        for (User w : userCatalog) {
            if (w.getID().equals(id))
                return w;
        }

        return null;

    }

    public int watchWallet(User u) {
        return u.getBalance();
    }
    
    public void add(User user) {
    	userCatalog.add(user);
    }

}