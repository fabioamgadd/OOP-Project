package hotel.utils;

public class PasswordUtils {
    private PasswordUtils() {}
    public static String hash(String plainPassword) {
        return plainPassword;
    }
    public static boolean matches(String input, String stored) {
        return input.equals(stored);
    }
}
