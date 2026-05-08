package hotel.services;

import hotel.database.HotelDatabase;
import hotel.enums.Gender;
import hotel.interfaces.Authenticatable;
import hotel.models.Admin;
import hotel.models.Guest;
import hotel.models.Receptionist;
import hotel.models.Staff;

import java.time.LocalDate;

public class AuthService {


    public Guest registerGuest(String username, String plainPassword,
                               LocalDate dateOfBirth, String address, Gender gender) {
        if (usernameExistsForGuest(username) || usernameExistsForStaff(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        Guest.validatePassword(plainPassword);

        Guest guest = new Guest(username, plainPassword, dateOfBirth, address, gender);
        HotelDatabase.guests.add(guest);
        return guest;
    }


    public Guest loginGuest(String username, String plainPassword) {
        for (Guest g : HotelDatabase.guests) {
            if (g.getUsername().equals(username) && g.authenticate(plainPassword)) {
                return g;
            }
        }
        return null;
    }


    public Staff loginStaff(String username, String plainPassword) {
        for (Staff s : HotelDatabase.staffMembers) {
            if (s.getUsername().equals(username) && s.authenticate(plainPassword)) {
                return s;
            }
        }
        return null;
    }

    public Authenticatable login(String username, String plainPassword) {
        Guest g = loginGuest(username, plainPassword);
        if (g != null) return g;
        return loginStaff(username, plainPassword);
    }


    public Staff createReceptionist(String username, String plainPassword,
                                     LocalDate dateOfBirth, int workingHours, Gender gender) {
        if (usernameExistsForStaff(username) || usernameExistsForGuest(username)) {
            throw new IllegalArgumentException("Staff username '" + username + "' is already taken.");
        }
        Receptionist r = new Receptionist(username, plainPassword, dateOfBirth, workingHours, gender);
        HotelDatabase.staffMembers.add(r);
        return r;
    }

    public Staff createAdmin(String username, String plainPassword,
                              LocalDate dateOfBirth, int workingHours, Gender gender) {
        if (usernameExistsForStaff(username) || usernameExistsForGuest(username)) {
            throw new IllegalArgumentException("Staff username '" + username + "' is already taken.");
        }
        Admin a = new Admin(username, plainPassword, dateOfBirth, workingHours, gender);
        HotelDatabase.staffMembers.add(a);
        return a;
    }

    public boolean usernameExistsForGuest(String username) {
        return HotelDatabase.guests.stream()
                .anyMatch(g -> g.getUsername().equalsIgnoreCase(username));
    }

    public boolean usernameExistsForStaff(String username) {
        return HotelDatabase.staffMembers.stream()
                .anyMatch(s -> s.getUsername().equalsIgnoreCase(username));
    }
}
