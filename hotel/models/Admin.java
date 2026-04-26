package hotel.models;

import hotel.enums.Gender;
import hotel.enums.Role;

import java.time.LocalDate;

public class Admin extends Staff {


    public Admin(String username, String plainPassword, LocalDate dateOfBirth,
                 int workingHours, Gender gender) {
        super(username, plainPassword, dateOfBirth, Role.ADMIN, workingHours, gender);
    }

    public Admin(String staffId, String username, String hashedPassword,
                 LocalDate dateOfBirth, int workingHours, Gender gender) {
        super(staffId, username, hashedPassword, dateOfBirth, Role.ADMIN, workingHours, gender);
    }


    @Override
    public String getRoleDescription() {
        return "Admin: Full CRUD on rooms, amenities, room types, and guest management.";
    }


    @Override
    public String toString() {
        return String.format("Admin{%s}", super.toString());
    }
}
