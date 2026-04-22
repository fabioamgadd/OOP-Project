package hotel.ui;

import hotel.models.Reservation;
import hotel.models.Staff;
import hotel.services.ReceptionistService;
import hotel.utils.DisplayUtils;

import java.util.Scanner;

public class ReceptionistMenu {
    private final Scanner scanner;
    private final ReceptionistService receptionistService;
    private final Staff currentStaff;

    public ReceptionistMenu(Scanner scanner, ReceptionistService receptionistService, Staff staff) {
        this.scanner = scanner;
        this.receptionistService = receptionistService;
        this.currentStaff = staff;
    }

    public void show() {
        boolean running = true;
        while (running) {
            DisplayUtils.printHeader("Receptionist Dashboard — " + currentStaff.getUsername());
            System.out.println("1. View All Guests");
            System.out.println("2. View All Rooms");
            System.out.println("3. View All Reservations");
            System.out.println("4. Check In Guest");
            System.out.println("5. Check Out Guest");
            System.out.println("0. Logout");
            System.out.print("\n  Choice: ");

            int Choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println();

            if (Choice == 1) {
                viewGuests();
            }
            else if (Choice == 2) {
                viewRooms();
            }
            else if (Choice == 3) {
                viewReservations();
            }
            else if (Choice == 4) {
                checkIn();
            }
            else if (Choice == 5) {
                checkOut();
            }
            else if (Choice == 0) {
                running = false;
            }
            else {
                DisplayUtils.printError("Invalid option.");
            }
        }
        DisplayUtils.printInfo("Receptionist logged out.");
    }

    private void viewGuests() {
        DisplayUtils.printHeader("All Guests");
        DisplayUtils.printGuests(receptionistService.getAllGuests());
    }

    private void viewRooms() {
        DisplayUtils.printHeader("All Rooms");
        DisplayUtils.printRooms(receptionistService.getAllRooms());
    }

    private void viewReservations() {
        DisplayUtils.printHeader("All Reservations");
        DisplayUtils.printReservations(receptionistService.getAllReservations());
    }

    private void checkIn() {
        DisplayUtils.printHeader("Check In Guest");
        viewReservations();
        System.out.print("\nEnter Reservation ID: ");
        String reservationId = scanner.nextLine();
        try {
            receptionistService.checkIn(reservationId);
            Reservation reservation = receptionistService.findReservationById(reservationId);
            DisplayUtils.printSuccess("Guest checked in. Reservation status: " + reservation.getStatus());
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void checkOut() {
        DisplayUtils.printHeader("Check Out Guest");
        viewReservations();
        System.out.print("\nEnter Reservation ID: ");
        String reservationId = scanner.nextLine();
        try {
            receptionistService.checkOut(reservationId);
            Reservation reservation = receptionistService.findReservationById(reservationId);
            DisplayUtils.printSuccess("Guest checked out. Reservation status: " + reservation.getStatus());
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }
}
