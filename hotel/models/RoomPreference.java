package hotel.models;

public class RoomPreference {
    private String preferredRoomTypeName;
    private int preferredFloor;
    private boolean smokingRoom;
    private boolean  accessibilityRequired;

    public RoomPreference(){

    }

    public RoomPreference(String preferredRoomTypeName, int preferredFloor, boolean smokingRoom, boolean accessibilityRequired) {
        this.preferredRoomTypeName = preferredRoomTypeName;
        this.preferredFloor = preferredFloor;
        this.smokingRoom = smokingRoom;
        this.accessibilityRequired = accessibilityRequired;
    }

    public String getPreferredRoomTypeName() {
        return preferredRoomTypeName;
    }

    public void setPreferredRoomTypeName(String preferredRoomTypeName) {
        this.preferredRoomTypeName = preferredRoomTypeName;
    }

    public int getPreferredFloor() {
        return preferredFloor;
    }

    public void setPreferredFloor(int preferredFloor) {
        this.preferredFloor = preferredFloor;
    }

    public boolean isSmokingRoom() {
        return smokingRoom;
    }

    public void setSmokingRoom(boolean smokingRoom) {
        this.smokingRoom = smokingRoom;
    }

    public boolean isAccessibilityRequired() {
        return accessibilityRequired;
    }

    public void setAccessibilityRequired(boolean accessibilityRequired) {
        this.accessibilityRequired = accessibilityRequired;
    }

    @Override
    public String toString() {
        return "RoomPreference[type="+ preferredRoomTypeName+ " floor="+preferredFloor+ " smoking=" +smokingRoom+" accessibility="+accessibilityRequired;

    }
}
