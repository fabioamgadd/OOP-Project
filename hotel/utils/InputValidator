package hotel.utils;
import java.time.LocalDate;
public class InputValidator {
    private InputValidator()
    {}

    public static boolean isNullOrEmpty(String N)
    {
        return N==null||N.trim().isEmpty();

    }

    public static void requireNonEmpty(String value,String fieldName)
    {
        if (isNullOrEmpty(value))
        {
            throw new IllegalArgumentException(fieldName+" cannot be empty");
        }

    }
    public static void requirePositive(double d,String fieldName)
    {
        if(d<=0)
        {
            throw new IllegalArgumentException(fieldName+" must be a positive integer");
        }

    }

    public static void requireFutureDate(LocalDate Date,String fieldName)
    {
        if(Date==null||!Date.isAfter(LocalDate.now().minusDays(1)))
        {
           throw new IllegalArgumentException(fieldName + " must be today or future date");
        }
    }
    public static void requireDateBefore(LocalDate earlier, LocalDate later, String field1, String field2)
    {

        if (!earlier.isBefore(later))
        {
            throw new IllegalArgumentException(field1 + " must be before"+ field2);
        }

    }

}
