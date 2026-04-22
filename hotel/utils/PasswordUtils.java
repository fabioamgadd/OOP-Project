package hotel.utils;

public class PasswordUtils {
    public static boolean matches(String input, String stored) {
        return input.equals(stored);
    }
}
