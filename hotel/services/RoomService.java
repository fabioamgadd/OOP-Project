package hotel.services;

import hotel.database.HotelDatabase;
import hotel.interfaces.Manageable;
import hotel.models.Amenity;
import hotel.models.Room;
import hotel.models.RoomType;

import java.util.ArrayList;
import java.util.List;

public class RoomService implements Manageable<Room> {

    @Override
    public void add(Room r){
        if (findById(r.getRoomId())!=null){
            throw new IllegalArgumentException("Room with ID: " + r.getRoomId() + " ,already exists");
        }
        HotelDatabase.rooms.add(r);
    }
    @Override
    public boolean update(Room updatedr) {
        for (int i = 0; i < HotelDatabase.rooms.size(); i++) {
            if (HotelDatabase.rooms.get(i).getRoomId().equals(updatedr.getRoomId())) {
                HotelDatabase.rooms.set(i, updatedr);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean delete(String roomId) {
        for (int i = 0; i < HotelDatabase.rooms.size(); i++) {
            if (HotelDatabase.rooms.get(i).getRoomId().equals(roomId)) {
                HotelDatabase.rooms.remove(i);
                return true;
            }
        }
        return false;
    }
    @Override
    public Room findById(String roomId) {
        // U
        for (int i = 0; i < HotelDatabase.rooms.size(); i++) {
            Room r=HotelDatabase.rooms.get(i);
            if (r.getRoomId().equals(roomId)) {
                return r;
            }
        }
        return null;
    }
    public List<Room> getAllRooms() {
        return new ArrayList<>(HotelDatabase.rooms);
    }

    public List<Room> getAvailableRooms(){
        List<Room> availableRooms=new ArrayList<>();//created new list
        for (int i=0;i<HotelDatabase.rooms.size();i++){
            Room r=HotelDatabase.rooms.get(i);
            if (r.isAvailable()){
                availableRooms.add(r);
            }
        }
        return availableRooms;
    }
    public List<Room> getAvailableRoomsByType(String typeName){
        List<Room> availableTypeRooms=new ArrayList<>();//created new list
        for (int i=0;i<HotelDatabase.rooms.size();i++){
            Room r=HotelDatabase.rooms.get(i);
            if (r.isAvailable() && r.getRoomType().getTypeName().equals(typeName)){
                availableTypeRooms.add(r);
            }
        }
        return availableTypeRooms;
    }
    public List<Room> getAvailableRoomsByFloor(int floor){
        List<Room> availableFloorRooms=new ArrayList<>();//created new list
        for (int i=0;i<HotelDatabase.rooms.size();i++){
            Room r=HotelDatabase.rooms.get(i);
            if (r.isAvailable() && r.getFloorNumber()==floor){
                availableFloorRooms.add(r);
            }
        }
        return availableFloorRooms;
    }

    public List<Room> getAvailableRoomsWithinBudget(double maxPricePerNight){
        List<Room> availableBudgetRooms=new ArrayList<>();//created new list
        for (int i=0;i<HotelDatabase.rooms.size();i++){
            Room r=HotelDatabase.rooms.get(i);
            if (r.isAvailable() && r.getTotalPricePerNight()<=maxPricePerNight){
                availableBudgetRooms.add(r);
            }
        }
        return availableBudgetRooms;
    }

    public List<Room> getAvailableRoomsByAmenities(List<String> requiredAmenityNames){
        List<Room> availableAmenitiesRooms=new ArrayList<>();//created new list
        for (int i=0;i<HotelDatabase.rooms.size();i++){
            Room r=HotelDatabase.rooms.get(i);
            if (r.isAvailable() && hasAllAmenities(r, requiredAmenityNames) ){
                availableAmenitiesRooms.add(r);
            }
        }
        return availableAmenitiesRooms;
    }

    public boolean markUnavailable(String roomID){
        Room r=findById(roomID);
        if(r==null) return false;
        r.setAvailable(false);
        return true;

    }
    public boolean markAvailable(String roomID){
        Room r=findById(roomID);
        if(r==null) return false;
        r.setAvailable(true);
        return true;

    }

    public void addRoomType(RoomType rt){
        boolean exists=false;
        for (int i=0;i<HotelDatabase.roomTypes.size();i++){
            RoomType r=HotelDatabase.roomTypes.get(i);
            if (r.getTypeId().equals(rt.getTypeId())) {
                exists = true;
                break;
            }
        }
        if (exists) throw new IllegalArgumentException("RoomType with that ID already exists.");
        HotelDatabase.roomTypes.add(rt);

    }
    public boolean updatedRoomType(RoomType updated){
        for (int i = 0; i < HotelDatabase.roomTypes.size(); i++) {
            if (HotelDatabase.roomTypes.get(i).getTypeId().equals(updated.getTypeId())) {
                HotelDatabase.roomTypes.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteRoomType(String typeID){
        for (int i=0;i<HotelDatabase.roomTypes.size();i++){
            RoomType r=HotelDatabase.roomTypes.get(i);
            if (r.getTypeId().equals(typeID)) {
                HotelDatabase.roomTypes.remove(r);
                return true;
            }
        }
        return false;
    }
    public RoomType findRoomTypeById(String typeID){
        for (int i=0;i<HotelDatabase.roomTypes.size();i++){
            RoomType r=HotelDatabase.roomTypes.get(i);
            if (r.getTypeId().equals(typeID)) {
                return r;
            }
        }
        return null;
    }

    public List<RoomType> getAllRoomTypes() {
        return new ArrayList<>(HotelDatabase.roomTypes);
    }

    public void addAmenity(Amenity amenity) {
        for (int i = 0; i < HotelDatabase.amenities.size(); i++) {
            Amenity a = HotelDatabase.amenities.get(i);
            if (a.getAmenityId().equals(amenity.getAmenityId())) {
                throw new IllegalArgumentException("Amenity with that ID already exists.");
            }
        }
        HotelDatabase.amenities.add(amenity);
    }

    public boolean updateAmenity(Amenity updated) {
        for (int i = 0; i < HotelDatabase.amenities.size(); i++) {
            Amenity existing = HotelDatabase.amenities.get(i);
            if (existing.getAmenityId().equals(updated.getAmenityId())) {
                HotelDatabase.amenities.set(i,updated);
                return true;
            }
        }
        return false;
    }
    public boolean deleteAmenity(String amenityId) {
        for (int i = 0; i < HotelDatabase.amenities.size(); i++) {
            Amenity a = HotelDatabase.amenities.get(i);
            if (a.getAmenityId().equals(amenityId)) {
                HotelDatabase.amenities.remove(i);
                return true;
            }
        }
        return false;
    }

    public Amenity findAmenityById(String amenityId) {
        for (int i = 0; i < HotelDatabase.amenities.size(); i++) {
            Amenity a = HotelDatabase.amenities.get(i);
            if (a.getAmenityId().equals(amenityId)) {
                return a;
            }
        }
        return null;
    }











    public List<Amenity> getAllAmenities() {
        return new ArrayList<>(HotelDatabase.amenities);
    }

    //helper function for getAvailableRoomsByAmenities
    private boolean hasAllAmenities(Room room, List<String> requiredNames) {
        List<Amenity> roomAmenities = room.getAmenities();

        for (int i = 0; i < requiredNames.size(); i++) {
            String req = requiredNames.get(i);
            boolean foundMatch = false;

            for (int j = 0; j < roomAmenities.size(); j++) {
                Amenity a = roomAmenities.get(j);
                if (a.getName().equalsIgnoreCase(req)) {
                    foundMatch = true;
                    break; //it is found so break to move to the next requirment
                }
            }
            if (foundMatch == false) {
                return false;
            }
        }
        return true;
    }



}
