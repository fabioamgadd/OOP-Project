package hotel.models;
import java.util.Random;
public class Amenity {
    private String amenityId;
    private String name;
    private String description;
    private double extraCostPerNight;

    private static final Random random = new Random();

    public Amenity(){

    }

    public Amenity(String amenityId, String name, String description, double extraCostPerNight) {
        this.amenityId = amenityId;
        this.name = name;
        this.description = description;
        this.extraCostPerNight = extraCostPerNight;
    }

    public Amenity(String name, String description, double extraCostPerNight) {
        this.amenityId = generateId();
        this.name = name;
        this.description = description;
        this.extraCostPerNight = extraCostPerNight;
    }


    public static String generateId() {
        int number = 100 + random.nextInt(900);
        return "a" + number;
    }


    public String getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(String amenityId) {
        this.amenityId = amenityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getExtraCostPerNight() {
        return extraCostPerNight;
    }

    public void setExtraCostPerNight(double extraCostPerNight) {
        if (extraCostPerNight < 0) throw new IllegalArgumentException("Cost cannot be negative.");
        this.extraCostPerNight = extraCostPerNight;
    }

    @Override
    public String toString() {
        return "Amenity[id="+amenityId +" name="+name+" cost="+ extraCostPerNight +"/night]";

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Amenity) {
            Amenity amenity = (Amenity) obj;
            if (this.amenityId == amenity.amenityId) {
                return true;
            }
        }
        return false;


    }
}
