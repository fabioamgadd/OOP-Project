package hotel.models;

import hotel.enums.Gender;
import hotel.enums.Role;
import hotel.interfaces.Authenticatable;
import hotel.utils.PasswordUtils;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Staff implements Authenticatable {

    private static int idCounter = 1;

    private String staffId;
    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private Role role;
    private int workingHours;
    private Gender gender;


    protected Staff(String username, String plainPassword, LocalDate dateOfBirth,
                    Role role, int workingHours, Gender gender) {
        this.staffId = "S" + String.format("%03d", idCounter++);
        this.username = username;
        this.password = PasswordUtils.hash(plainPassword);
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        this.workingHours = workingHours;
        this.gender = gender;
    }

    protected Staff(String staffId, String username, String storedPassword,
                    LocalDate dateOfBirth, Role role, int workingHours, Gender gender) {
        this.staffId = staffId;
        this.username = username;
        this.password = storedPassword;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        this.workingHours = workingHours;
        this.gender = gender;
    }


    @Override
    public boolean authenticate(String plainPassword) {
        return PasswordUtils.matches(plainPassword, this.password);
    }

    @Override
    public String getUsername() { return username; }


    public abstract String getRoleDescription();


    public String getStaffId() { return staffId; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String plainPassword) {
        this.password = PasswordUtils.hash(plainPassword);
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Role getRole() { return role; }

    public int getWorkingHours() { return workingHours; }
    public void setWorkingHours(int workingHours) {
        if (workingHours < 0 || workingHours > 168) {
            throw new IllegalArgumentException("Working hours must be between 0 and 168 per week.");
        }
        this.workingHours = workingHours;
    }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }


    @Override
    public String toString() {
        return String.format("Staff{id='%s', username='%s', role=%s, workingHours=%d}",
                staffId, username, role, workingHours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff staff = (Staff) o;
        return Objects.equals(staffId, staff.staffId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId);
    }
}
