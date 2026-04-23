package hotel.models;

import hotel.enums.Gender;
import hotel.enums.Role;
import hotel.utils.PasswordUtils;

import java.util.Objects;

abstract class Staff {
    private String staffId;
    private String username;
    private String password;
    private String dob;
    private int workingHours;
    Role role;
    Gender gender;
    private static int idCounter=0;
     protected Staff( String staffId1, String username, String password,String dob, Role role, int workingHours , Gender gender){
         this.staffId = staffId1;
         this. username=username;
         this. password=password;
         this.role=role;
         this.workingHours=workingHours;
         this.gender=gender;
         this.dob=dob;
     }
    protected Staff(  String username, String password,String dob, Role role, int workingHours , Gender gender){
         this.staffId = "S"+ String.format("%03d",idCounter++);
         this. username=username;
         this. password=password;
         this.role=role;
         this.workingHours=workingHours;
         this.gender=gender;
        this.dob=dob;
     }


    public String getStaffId() {
        return staffId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDob() {
        return dob;
    }

    public int getWorkingHours() {

         return workingHours;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWorkingHours(int workingHours) {
       if(workingHours>=0&&workingHours<=168)
        this.workingHours = workingHours;
       else
           System.out.println("invalid number");
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public abstract String getRoleDescription();
    @Override
    public String toString() {
        return ("Staffid = "+staffId+"Username= "+username+"role= "+role+"working hours = "+workingHours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff staff = (Staff) o;
        return Objects.equals(staffId, staff.staffId);
    }
    public boolean authenticate(String username, String password) {
       if ( this.username.equals(username) && PasswordUtils.matches(password, this.password))
           return true;
    }

}
