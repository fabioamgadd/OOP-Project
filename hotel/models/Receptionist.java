package hotel.models;

import hotel.enums.Role;

public class Receptionist extends Staff, Role.RECEPTIONIST {
     public Receptionist(String staffId1, String username, String password,String dob , int workingHours , Gender gender){
        super(staffId1,username,password,dob,Role.RECEPTIONIST,workingHours,gender);
    }
ublic Receptionist(String username, String password,String dob , int workingHours , Gender gender){
        super(username,password,dob,Role.RECEPTIONIST,workingHours,gender);
    }
    @Override
    public String getRoleDescription() {
        return "this is the Receptionist ";
    }
@Override
    public String toString() {
        return ("Receptionist" + super.toString());
    }
}
