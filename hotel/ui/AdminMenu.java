package hotel.ui;

import hotel.models.*;
import hotel.services.AdminService;
import hotel.utils.DisplayUtils;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final AdminService adminService;
    private final Staff currentAdmin;

    public AdminMenu(Scanner scanner, AdminService adminService, Staff admin) {
        this.scanner = scanner;
        this.adminService = adminService;
        this.currentAdmin = admin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            DisplayUtils.printHeader("Admin Panel — " + currentAdmin.getUsername());
            System.out.println("1. View All Rooms");
            System.out.println("2. Add Room");
            System.out.println("3. Update Room");
            System.out.println("4. Delete Room");
            System.out.println("5. View All Room Types");
            System.out.println("6. Add Room Type");
            System.out.println("7. Update Room Type");
            System.out.println("8. Delete Room Type");
            System.out.println("9. View All Amenities");
            System.out.println("10. Add Amenity");
            System.out.println("11. Update Amenity");
            System.out.println("12. Delete Amenity");
            System.out.println("13. View All Guests");
            System.out.println("14. View All Reservations");
            System.out.println("15. View All Invoices");
            System.out.println("16. View Revenue Summary");
            System.out.println("0. Logout");
            System.out.print("\nChoice: ");

            int Choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println();

            if (Choice == 1) {
                viewRooms();
            }
            else if (Choice == 2) {
                addRoom();
            }
            else if (Choice == 3) {
                updateRoom();
            }
            else if (Choice == 4) {
                deleteRoom();
            }
            else if (Choice == 5) {
                viewRoomTypes();
            }
            else if (Choice == 6) {
                addRoomType();
            }
            else if (Choice == 7) {
                updateRoomType();
            }
            else if (Choice == 8) {
                deleteRoomType();
            }
            else if (Choice == 9) {
                viewAmenities();
            }
            else if (Choice == 10) {
                addAmenity();
            }
            else if (Choice == 11) {
                updateAmenity();
            }
            else if (Choice == 12) {
                deleteAmenity();
            }
            else if (Choice == 13) {
                viewGuests();
            }
            else if (Choice == 14) {
                viewReservations();
            }
            else if (Choice == 15) {
                viewInvoices();
            }
            else if (Choice == 16) {
                viewRevenue();
            }
            else if (Choice == 0) {
                running = false;
            }
            else {
                DisplayUtils.printError("Invalid option.");
            }
        }
        DisplayUtils.printInfo("Admin logged out.");
    }

    private void viewRooms() {
        DisplayUtils.printHeader("All Rooms");
        DisplayUtils.printRooms(adminService.getAllRooms());
    }

    private void addRoom() {
        DisplayUtils.printHeader("Add Room");

        System.out.print("Room ID: ");
        String roomId = scanner.nextLine();

        System.out.print("Floor number: ");
        int floor;
        try {
            floor = Integer.parseInt(scanner.nextLine());
        }
        catch (Exception e) {
            DisplayUtils.printError("Invalid floor.");
            return;
        }

        System.out.println("Available Room Types:");
        List<RoomType> types = adminService.getAllRoomTypes();
        for (int i = 0; i < types.size(); i++) {
            RoomType rt = types.get(i);
            System.out.println(rt.getTypeId() + " - " + rt.getTypeName());
        }

        System.out.print("Enter Room Type ID: ");
        String typeId = scanner.nextLine();
        RoomType rt = adminService.getRoomType(typeId);

        if (rt == null) {
            DisplayUtils.printError("Room type not found.");
            return;
        }

        try {
            adminService.addRoom(new Room(roomId, floor, rt));
            DisplayUtils.printSuccess("Room '" + roomId + "' added successfully.");
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void deleteRoom() {
        DisplayUtils.printHeader("Delete Room");

        System.out.print("Enter Room ID to delete: ");
        String roomId = scanner.nextLine();

        if (adminService.deleteRoom(roomId)) {
            DisplayUtils.printSuccess("Room '" + roomId + "' deleted.");
        }
        else {
            DisplayUtils.printError("Room not found.");
        }
    }

    private void updateRoom() {
        DisplayUtils.printHeader("Update Room");
        viewRooms();

        System.out.print("\nEnter Room ID to update: ");
        String roomId = scanner.nextLine();

        Room room = adminService.getRoom(roomId);
        if (room == null) {
            DisplayUtils.printError("Room not found.");
            return;
        }

        System.out.printf("New floor (leave blank to keep %d): ", room.getFloorNumber());
        String floorInput = scanner.nextLine();
        if (!floorInput.isEmpty()) {
            try {
                room.setFloorNumber(Integer.parseInt(floorInput));
            }
            catch (NumberFormatException e) {
                DisplayUtils.printError("Invalid floor.");
                return;
            }
        }

        String currentAvailability;
        if (room.isAvailable()) {
            currentAvailability = "available";
        } else {
            currentAvailability = "unavailable";
        }
        System.out.printf("New availability (y/n) (blank to keep %s): ", currentAvailability);
        String availabilityInput = scanner.nextLine().toLowerCase();

        if ("y".equals(availabilityInput)) {
            room.setAvailable(true);
        } else if ("n".equals(availabilityInput)) {
            room.setAvailable(false);
        } else if (!availabilityInput.isEmpty()) {
            DisplayUtils.printError("Availability must be y, n, or blank.");
            return;
        }

        System.out.println("Available Room Types:");
        List<RoomType> roomTypes = adminService.getAllRoomTypes();
        for (int i = 0; i < roomTypes.size(); i++) {
            RoomType rt = roomTypes.get(i);
            System.out.println(rt.getTypeId() + " — " + rt.getTypeName() + " (" + rt.getBasePricePerNight() + "/night)");
        }
        System.out.printf("New Room Type ID (blank to keep %s): ", room.getRoomType().getTypeName());
        String roomTypeId = scanner.nextLine().trim();
        if (!roomTypeId.isEmpty()) {
            RoomType roomType = adminService.getRoomType(roomTypeId);
            if (roomType == null) {
                DisplayUtils.printError("Room type not found.");
                return;
            }
            room.setRoomType(roomType);
        }

        System.out.println("Available Amenities:");
        List<Amenity> amenities = adminService.getAllAmenities();
        for (int i = 0; i < amenities.size(); i++) {
            Amenity amenity = amenities.get(i);
            System.out.println(amenity.getAmenityId() + " — " + amenity.getName());
        }
        System.out.print("Amenity IDs (comma-separated, blank to keep current list): ");
        String amenityIds = scanner.nextLine();
        if (!amenityIds.isEmpty()) {
            try {
                room.setAmenities(resolveAmenities(amenityIds));
            }
            catch (IllegalArgumentException e) {
                DisplayUtils.printError(e.getMessage());
                return;
            }
        }

        if (adminService.updateRoom(room)) {
            DisplayUtils.printSuccess("Room updated successfully.");
        }
        else {
            DisplayUtils.printError("Room update failed.");
        }
    }

    private void viewRoomTypes() {
        DisplayUtils.printHeader("Room Types");
        List<RoomType> types = adminService.getAllRoomTypes();
        if (types.isEmpty()) { System.out.println("  No room types found."); return; }
        System.out.println("ID    Name    Price/Night    MaxOcc");
        for (int i = 0; i < types.size(); i++) {
            RoomType rt = types.get(i);
            System.out.println(rt.getTypeId() + "    " + rt.getTypeName() + "    " + rt.getBasePricePerNight() + "    " + rt.getMaxOccupancy());
        }
    }

    private void addRoomType() {
        DisplayUtils.printHeader("Add Room Type");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Description: ");
        String desc = scanner.nextLine();

        System.out.print("Price per night: ");
        double price;
        try {
            price = Double.parseDouble(scanner.nextLine());
        }
        catch (NumberFormatException e) {
            DisplayUtils.printError("Invalid price.");
            return;
        }

        System.out.print("Max occupancy: ");
        int max;
        try {
            max = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            DisplayUtils.printError("Invalid occupancy.");
            return;
        }

        try {
            RoomType rt = new RoomType(name, desc, price, max);
            adminService.addRoomType(rt);
            DisplayUtils.printSuccess("Room type added.");
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void deleteRoomType() {
        DisplayUtils.printHeader("Delete Room Type");
        viewRoomTypes();
        System.out.println("\nEnter Room Type ID to delete it: ");
        String id = scanner.nextLine();

        if (adminService.deleteRoomType(id)) {
            DisplayUtils.printSuccess("Room Type Deleted.");
        } else {
            DisplayUtils.printError("Room Type Not Found.");
        }
    }

    private void updateRoomType() {
        DisplayUtils.printHeader("Update Room Type");
        viewRoomTypes();
        System.out.print("\nEnter Room Type ID to update it: ");
        String id = scanner.nextLine();

        RoomType roomType = adminService.getRoomType(id);
        if (roomType == null) {
            DisplayUtils.printError("Room Type Not found.");
            return;
        }

        System.out.print("New name: ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) {
            roomType.setTypeName(name);
        }

        System.out.println("New description (blank to keep current): ");
        String description = scanner.nextLine();
        if (!description.isEmpty()) {
            roomType.setDescription(description);
        }

        System.out.printf("New base price (blank to keep %.2f): ", roomType.getBasePricePerNight());
        String priceInput = scanner.nextLine().trim();
        if (!priceInput.isEmpty()) {
            try {
                roomType.setBasePricePerNight(Double.parseDouble(priceInput));
            }
            catch (NumberFormatException e) {
                DisplayUtils.printError("Invalid base price.");
                return;
            }
        }

        System.out.printf("New max occupancy (blank to keep %d): ", roomType.getMaxOccupancy());
        String occupancyInput = scanner.nextLine();
        if (!occupancyInput.isEmpty()) {
            try {
                roomType.setMaxOccupancy(Integer.parseInt(occupancyInput));
            }
            catch (NumberFormatException e) {
                DisplayUtils.printError("Invalid max occupancy.");
                return;
            }
        }

        if (adminService.updateRoomType(roomType)) {
            DisplayUtils.printSuccess("Room type updated successfully.");
        }
        else {
            DisplayUtils.printError("Room type update failed.");
        }
    }

    private void viewAmenities() {
        DisplayUtils.printHeader("All Amenities");
        List<Amenity> ams = adminService.getAllAmenities();
        if (ams.isEmpty()) {
            System.out.println("No amenities found.");
            return;
        }
        System.out.println("ID    Name    Description    Cost/Night");
        for (int i = 0; i < ams.size(); i++) {
            Amenity a = ams.get(i);
            System.out.println(a.getAmenityId() + "    " + a.getName() + "    " + a.getDescription() + "    " + a.getExtraCostPerNight());
        }
    }

    private void addAmenity() {
        DisplayUtils.printHeader("Add Amenity");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Description: ");
        String desc = scanner.nextLine();

        System.out.print("Extra Cost per night: ");
        double cost;
        try {
            cost = Double.parseDouble(scanner.nextLine());
        }
        catch (NumberFormatException e) {
            DisplayUtils.printError("Invalid cost.");
            return;
        }

        try {
            Amenity amenity = new Amenity(name, desc, cost);
            adminService.addAmenity(amenity);
            DisplayUtils.printSuccess("Amenity '" + name + "' added. ID: " + amenity.getAmenityId());
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void deleteAmenity() {
        DisplayUtils.printHeader("Delete Amenity");
        viewAmenities();
        System.out.print("Enter ID: ");
        String id = scanner.nextLine();

        if (adminService.deleteAmenity(id)) {
            DisplayUtils.printSuccess("Amenity deleted.");
        } else {
            DisplayUtils.printError("Amenity Not found.");
        }
    }

    private void updateAmenity() {
        DisplayUtils.printHeader("Update Amenity");
        viewAmenities();
        System.out.print("Enter ID: ");
        String id = scanner.nextLine();

        Amenity amenity = adminService.getAmenity(id);
        if (amenity == null) {
            DisplayUtils.printError("Amenity Not found.");
            return;
        }

        System.out.printf("  New name (blank to keep %s): ", amenity.getName());
        String name = scanner.nextLine();
        if (!name.isEmpty()) {
            amenity.setName(name);
        }

        System.out.print("  New description (blank to keep current): ");
        String description = scanner.nextLine();
        if (!description.isEmpty()) {
            amenity.setDescription(description);
        }

        System.out.printf("  New extra cost (blank to keep %.2f): ", amenity.getExtraCostPerNight());
        String costInput = scanner.nextLine();
        if (!costInput.isEmpty()) {
            try {
                amenity.setExtraCostPerNight(Double.parseDouble(costInput));
            }
            catch (NumberFormatException e) {
                DisplayUtils.printError("Invalid extra cost.");
                return;
            }
        }

        if (adminService.updateAmenity(amenity)) {
            DisplayUtils.printSuccess("Amenity updated successfully.");
        } else {
            DisplayUtils.printError("Amenity update failed.");
        }
    }

    private void viewGuests() {
        DisplayUtils.printHeader("All Registered Guests");
        DisplayUtils.printGuests(adminService.getAllGuests());
    }

    private void viewReservations() {
        DisplayUtils.printHeader("All Reservations");
        DisplayUtils.printReservations(adminService.getAllReservations());
    }

    private void viewInvoices() {
        DisplayUtils.printHeader("Invoices");
        List<Invoice> invoices = adminService.getAllInvoices();

        if (invoices.isEmpty()) {
            System.out.println("No invoices.");
            return;
        }

        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            System.out.println(inv.getFormattedInvoice());
            System.out.println();
        }
    }

    private void viewRevenue() {
        DisplayUtils.printHeader("Revenue Summary");

        System.out.printf("Total Revenue: EGP %.2f%n", adminService.getTotalRevenue());
        System.out.println("Occupied Rooms: " + adminService.getOccupiedRoomCount());
        System.out.println("Available Rooms: " + adminService.getAvailableRoomCount());
        System.out.println("Guests: " + adminService.getAllGuests().size());
        System.out.println("Reservations: " + adminService.getAllReservations().size());
    }

    private List<Amenity> resolveAmenities(String amenityIds) {
        java.util.ArrayList<Amenity> selectedAmenities = new java.util.ArrayList<>();
        String[] ids = amenityIds.split(",");
        for (int i = 0; i < ids.length; i++) {
            String amenityId = ids[i];
            if (amenityId.isEmpty()) {
                continue;
            }
            Amenity amenity = adminService.getAmenity(amenityId);
            if (amenity == null) {
                throw new IllegalArgumentException("Amenity not found: " + amenityId);
            }
            selectedAmenities.add(amenity);
        }
        return selectedAmenities;
    }
}
