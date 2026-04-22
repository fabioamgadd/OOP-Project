package hotel.services;

import hotel.database.HotelDatabase;
import hotel.enums.Gender;
import hotel.models.Admin;
import hotel.models.Guest;
import hotel.models.Receptionist;
import hotel.models.Staff;

public class AuthService {
public boolean registerGuest(String guestId, String username, String password, String dob, double balance, String address, Gender gender, RoomPreference preference) {
    for (Guest g : HotelDatabase.guests) {
        if (g.getUsername().equals(username)) {
            System.out.println("Username already exists. Please try again.");
            return false;}
        } else{
            new Guest(guestId, username, password, dob, balance, address, gender, preference);
            return true;}

    }
}

public boolean loginGuest(String username, String password) {
    for (Guest g : HotelDatabase.guests) {
        if (g.getUsername().equals(username)) {
            if (g.authenticate(username, password)) {
                System.out.println("Login successful!");
                return true;
            } else {
                System.out.println("Wrong password.");
                return false;
            }

        }

    }
    System.out.println("guest not found");
    return false;
}


}
public boolean loginStaff(String username, String password){
    for (Staff s: HotelDatabase.staffMembers) {
        if (s.getUsername().equals(username)) {
            if (s.authenticate(username, password)) {
                System.out.println("Login successful!");
                return true;
            }
            else {
                System.out.println("Wrong password.");
                return false;
            }
    }   }
    System.out.println("Staff member not found");
    return false;
}
public boolean login(String username, String password) {
    if (loginGuest(username, password)) {
        System.out.println("Logged in as Guest");
        return true;
    }
    if (loginStaff(username, password)) {
        System.out.println("Logged in as Staff");
        return true;
    }
    System.out.println("Invalid username or password.");
    return false;
}

public void createAdmin() {

    Admin admin = new Admin();
    HotelDatabase.staffMembers.add(admin);
}

public void createReceptionist() {

    Receptionist receptionist = new Receptionist();
    HotelDatabase.staffMembers.add(receptionist);
}
}




