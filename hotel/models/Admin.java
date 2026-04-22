package hotel.models;

import hotel.enums.Role;

public class Admin extends Staff, Role.ADMIN {
    Admin (){
        super();
    }
    @Override
    public String getRoleDescription() {
        return "this is the admin";
    }
}
