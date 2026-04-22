package hotel.models;

import hotel.enums.Role;

public class Receptionist extends Staff, Role.RECEPTIONIST {
    Receptionist (){
        super();
    }
    @Override
    public String getRoleDescription() {
        return "this is the Receptionist ";
    }
}
