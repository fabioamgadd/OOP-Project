package hotel;

import hotel.database.HotelDatabase;
import hotel.services.*;
import hotel.ui.MainMenu;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("Importing database.");
        int guestCount = HotelDatabase.guests.size();
        int roomCount = HotelDatabase.rooms.size();
        System.out.printf("  Loaded %d guests, %d rooms, %d room types, %d amenities.%n",
                guestCount, roomCount,
                HotelDatabase.roomTypes.size(),
                HotelDatabase.amenities.size());

        InvoiceService invoiceService = new InvoiceService();
        RoomService roomService = new RoomService();
        ReservationService reservationService = new ReservationService(roomService, invoiceService);
        GuestService guestService = new GuestService(roomService, reservationService, invoiceService);
        AdminService adminService = new AdminService(roomService, guestService, invoiceService);
        ReceptionistService receptionistService = new ReceptionistService(reservationService, roomService,
                guestService);
        AuthService authService = new AuthService();

        Scanner scanner = new Scanner(System.in);
        MainMenu mainMenu = new MainMenu(scanner, authService, guestService, adminService, receptionistService);
        mainMenu.run();

        scanner.close();
    }
}

