package hotel.models;

import hotel.enums.Gender;
import hotel.interfaces.Authenticatable;
import hotel.utils.PasswordUtils;

import java.time.LocalDate;
import java.util.Objects;

public class Guest implements Authenticatable {

    private static int idCounter = 1;

    private String guestId;
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private double balance;
    private String address;
    private Gender gender;
    private RoomPreference roomPreferences;


    public Guest(String username, String plainPassword, LocalDate dateOfBirth,
                 String address, Gender gender) {
        validateUsername(username);
        validatePassword(plainPassword);

        this.guestId = "G" + String.format("%03d", idCounter++);
        this.username = username;
        this.password = PasswordUtils.hash(plainPassword);
        this.dateOfBirth = dateOfBirth;
        this.balance = 0.0;
        this.address = address;
        this.gender = gender;
        this.roomPreferences = new RoomPreference();
    }

    public Guest(String guestId, String username, String storedPassword, LocalDate dateOfBirth,
                 double balance, String address, Gender gender, RoomPreference roomPreferences) {
        this.guestId = guestId;
        this.username = username;
        this.password = storedPassword;
        this.dateOfBirth = dateOfBirth;
        this.balance = balance;
        this.address = address;
        this.gender = gender;
        this.roomPreferences = roomPreferences != null ? roomPreferences : new RoomPreference();
    }


    @Override
    public boolean authenticate(String plainPassword) {
        return PasswordUtils.matches(plainPassword, this.password);
    }

    @Override
    public String getUsername() { return username; }


    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        this.balance += amount;
    }

    public boolean deduct(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deduction amount must be positive.");
        if (this.balance < amount) return false;
        this.balance -= amount;
        return true;
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


    public String getGuestId() { return guestId; }

    public void setUsername(String username) {
        validateUsername(username);
        this.username = username;
    }

    public String getPassword() { return password; }

    public void setPassword(String plainPassword) {
        validatePassword(plainPassword);
        this.password = PasswordUtils.hash(plainPassword);
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public double getBalance() { return balance; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public RoomPreference getRoomPreferences() { return roomPreferences; }
    public void setRoomPreferences(RoomPreference roomPreferences) { this.roomPreferences = roomPreferences; }


    @Override
    public String toString() {
        return String.format("Guest{id='%s', username='%s', gender=%s, balance=%.2f}",
                guestId, username, gender, balance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest)) return false;
        Guest guest = (Guest) o;
        return Objects.equals(guestId, guest.guestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }
}
