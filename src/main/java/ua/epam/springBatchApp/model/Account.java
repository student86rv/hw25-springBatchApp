package ua.epam.springBatchApp.model;

public class Account {

    private long id;

    private String name;

    private double balance;

    private String email;

    public Account() {
    }

    public Account(long id, String name, double balance, String email) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", email='" + email + '\'' +
                '}';
    }
}
