package hotel.models;

public class RoomType {
    private String typeId;
    private String typeName;
    private String description;
    private double basePricePerNight;
    private int maxOccupancy;
    private static int counterId=5;


    public RoomType(String typeId, String typeName, String description, double basePricePerNight, int maxOccupancy) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.description = description;
        this.basePricePerNight = basePricePerNight;
        this.maxOccupancy = maxOccupancy;
    }

    public RoomType(String typeName, String description, double basePricePerNight, int maxOccupancy) {
        this.typeId =  "rt" + String.format("%03d",counterId++);
        this.typeName = typeName;
        this.description = description;
        this.basePricePerNight = basePricePerNight;
        this.maxOccupancy = maxOccupancy;
    }
    

    public String getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBasePricePerNight() {
        return basePricePerNight;
    }

    public void setBasePricePerNight(double basePricePerNight) {
        if (basePricePerNight < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.basePricePerNight = basePricePerNight;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        if (maxOccupancy < 1) throw new IllegalArgumentException("Max occupancy must be at least 1.");
        this.maxOccupancy = maxOccupancy;
    }

    @Override
    public String toString() {
        return "RoomType(id="+typeId+" name="+ typeName+" price="+basePricePerNight +" maxOccupancy="+ maxOccupancy+"]";

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RoomType) {
            RoomType roomType = (RoomType) obj;
            if (this.typeId == roomType.typeId) {
                return true;
            }
        }
        return false;


    }

}
