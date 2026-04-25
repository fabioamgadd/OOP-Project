package hotel.models;

import hotel.enums.Gender;
import hotel.interfaces.Authenticatable;
import hotel.utils.PasswordUtils;

import java.util.Objects;

public class Guest implements Authenticatable {
    private String guestId;
    private String username;
    private String password ;
    private String dob;
    private double balance;
    private String address;
    Gender gender;
    RoomPreference preference;
    private static int idCounter=0;

    public Guest( String username, String password, String dob, double balance, String address, Gender gender, RoomPreference preference) {
        this.guestId = "G" + String.format("%03d",idCounter++);
        this.username = username;
        this.password = password;
        this.dob = dob;
        this.balance = balance;
        this.address = address;
        this.gender = gender;
        this.preference = preference;
    }
    public Guest(String guestId, String username, String password, String dob, double balance, String address, Gender gender, RoomPreference preference) {
        this.guestId =guestId ;
        this.username = username;
        this.password = password;
        this.dob = dob;
        this.balance = balance;
        this.address = address;
        this.gender = gender;
        this.preference = preference;
    }


    @Override
    public boolean authenticate(String plainpassword) {
        if (PasswordUtils.matches(password, this.password))
            return true;

    }

    @Override
    public String getUsername() {
        return username ;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getPassword() {
        return password;
    }

    public String getDob() {
        return dob;
    }

    public double getBalance() {
        return balance;
    }

    public String getAddress() {
        return address;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public void deposit(double amount){
        balance+=amount;
    }
    public void deduct(double amount){
        balance-=amount;
    }
    @Override
    public String toString() {
        return("id= "+ guestId + "username= " + username +"gender=" +gender+ "balance="+balance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest guest)) return false;
        return Objects.equals(guestId, guest.guestId);
    }
    private static void validateUsername(String username) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, digits, and underscores.");
        }
    }
    public static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
    }

}
