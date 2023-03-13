package domain;

public class User {

    private String id;
    private String password;
    private int balance;

    public User(String id, String password) {
        this.id = id;
        this.password = password;
        this.setBalance(balance);
    }

    public String getID() {
        return this.id;
    }

    @SuppressWarnings("unused")
    private String showPassword() {
        return this.password;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    protected void setNewPass(String newPass) {
        this.password = newPass;
    }

}