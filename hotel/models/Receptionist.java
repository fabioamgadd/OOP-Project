package hotel.models;

import hotel.enums.Gender;
import hotel.enums.Role;

import java.time.LocalDate;

public class Receptionist extends Staff {

    public Receptionist(String username, String plainPassword, LocalDate dateOfBirth,
                        int workingHours, Gender gender) {
        super(username, plainPassword, dateOfBirth, Role.RECEPTIONIST, workingHours, gender);
    }

    public Receptionist(String staffId, String username, String hashedPassword,
                        LocalDate dateOfBirth, int workingHours, Gender gender) {
        super(staffId, username, hashedPassword, dateOfBirth, Role.RECEPTIONIST, workingHours, gender);
    }


    @Override
    public String getRoleDescription() {
        return "Receptionist: Manages guest check-in and check-out operations.";
    }


    @Override
    public String toString() {
        return String.format("Receptionist{%s}", super.toString());
    }
}
