package hotel.interfaces;

public interface Authenticatable {
    public boolean authenticate(String plainPassword);
    public  String getUsername();
}
