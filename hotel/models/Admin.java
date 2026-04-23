package hotel.models;

import hotel.enums.Role;

public class Admin extends Staff, Role.ADMIN {
   public Admin(String staffId1, String username, String password,String dob , int workingHours , Gender gender){
        super(staffId1,username,password,dob,Role.ADMIN,workingHours,gender);
    }
 public Admin( String username, String password,String dob , int workingHours , Gender gender){
        super(username,password,dob,Role.ADMIN,workingHours,gender);
    }
    @Override
    public String getRoleDescription() {
        return "this is the admin";
    }
@Override
    public String toString() {
        return ("Admin"+ super.toString());
    }
}
