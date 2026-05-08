package hotel.interfaces;

public interface Authenticatable {
    boolean authenticate(String password);
    String getUsername();
}
