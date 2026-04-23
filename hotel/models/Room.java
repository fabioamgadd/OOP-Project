package hotel.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId;
    private int floorNumber;
    private boolean available;
    private RoomType roomType;
    private List<Amenity> amenities;

    public Room(String roomId, int floorNumber, RoomType roomType) {
        this.roomId = roomId;
        this.floorNumber = floorNumber;
        this.roomType = roomType;
        this.available = true;
        this.amenities = new ArrayList<>();
    }

    public Room(String roomId, int floorNumber, RoomType roomType, List<Amenity> amenities) {
        this.roomId = roomId;
        this.floorNumber = floorNumber;
        this.roomType = roomType;
        this.available = true;
        if(amenities != null)
            this.amenities = amenities;
        else
            this.amenities = new ArrayList<>();
    }

    public double getTotalPricePerNight() {
        double total = roomType.getBasePricePerNight();

        for (int i = 0; i < amenities.size(); i++) {
            Amenity a = amenities.get(i);
            total = total + a.getExtraCostPerNight();
        }
        return total;
    }


    public void addAmenity(Amenity amenity) {
        if (!amenities.contains(amenity)) {
            amenities.add(amenity);
        }
    }

    public boolean removeAmenity(String amenityId) {
        for (int i = 0; i < amenities.size(); i++) {
            Amenity a = amenities.get(i);

            if (a.getAmenityId().equals(amenityId)) {
                amenities.remove(i);
                return true;
            }
        }
        return false;
    }


    public String getRoomId() {
        return roomId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public List<Amenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<Amenity> amenities) {
        this.amenities = amenities;
    }

    @Override
    public String toString() {
        return "Room[id="+roomId+", floor="+floorNumber+", type="+roomType.getTypeName()+", available="+available+", price="+getTotalPricePerNight()+"/night]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Room) {
            Room room = (Room) obj;
            if (this.roomId == room.roomId) {
                return true;
            }
        }
        return false;


    }

}
