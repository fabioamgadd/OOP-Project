package hotel.interfaces;

public interface Authenticatable {
    public boolean authenticate(String plainPassword ,String comparable);
    public  String getUsername();
}
