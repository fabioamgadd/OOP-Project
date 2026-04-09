package Hotel_Project.ui;

import hotel.enums.PaymentMethod;
import hotel.models.*;
import hotel.services.GuestService;
import hotel.utils.DisplayUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class GuestMenu {
    private static final DateTimeFormatter theDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Scanner scanner = new Scanner(System.in);

    private final GuestService guestService;
    private       Guest        currentGuest;

    public GuestMenu(Scanner scanner, GuestService guestService, Guest guest) {
        this.scanner      = scanner;
        this.guestService = guestService;
        this.currentGuest = guest;
    }

    public void show() {
        boolean running = true;
        while (running) {
            DisplayUtils.printHeader("Guest Dashboard — Welcome, " + currentGuest.getUsername());
            System.out.println("1. Browse Available Rooms");
            System.out.println("2. Make a Reservation");
            System.out.println("3. View My Reservations");
            System.out.println("4. Cancel a Reservation");
            System.out.println("5. Pay Invoice (Checkout)");
            System.out.println("6. View My Invoices");
            System.out.println("7. Deposit Balance");
            System.out.println("8. View My Profile");
            System.out.println("9. Update Room Preferences");
            System.out.println("0. Logout");
            System.out.println("\nChoice: ");

            int Choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println();

            if (Choice == 1) {
                browseRooms();
            }
            else if (Choice == 2) {
                makeReservation();
            }
            else if (Choice == 3) {
                viewReservations();
            }
            else if (Choice == 4) {
                cancelReservation();
            }
            else if (Choice == 5) {
                payInvoice();
            }
            else if (Choice == 6) {
                viewInvoices();
            }
            else if (Choice == 7) {
                depositBalance();
            }
            else if (Choice == 8) {
                viewProfile();
            }
            else if (Choice == 9) {
                updatePreferences();
            }
            else {
                DisplayUtils.printError("Invalid input, please try again.");
            }
        }
        DisplayUtils.printInfo("Logged out. Goodbye, " + currentGuest.getUsername() + "!");
    }

    private void browseRooms() {
        DisplayUtils.printHeader("Available Rooms");
        System.out.println("\nFilter by:");
        System.out.println("1. All");
        System.out.println("2. Room Type");
        System.out.println("3. Max Budget");
        System.out.println("\nChoice: ");
        int roomChoice = scanner.nextInt();
        scanner.nextLine();

        List <Room> rooms;

        if (roomChoice == 2) {
            System.out.println("\nEnter room type (Single - Double - Suite - Deluxe - Family): ");
            String typeChoice = scanner.nextLine();
            rooms = guestService.viewAvailableRoomsByType(typeChoice);        }
        else if (roomChoice == 3) {
            System.out.println("\nEnter max price per night (EGP): ");
            try {
                double budget = Double.parseDouble(scanner.nextLine());
                rooms = guestService.viewAvailableRoomsWithinBudget(budget);
            }
            catch (NumberFormatException e) {
                DisplayUtils.printError("Invalid budget amount.");
                return;
            }
        }
        else {
            rooms = guestService.viewAvailableRooms();
        }

        DisplayUtils.printRooms(rooms);

    }

    private void makeReservation() {
        DisplayUtils.printHeader("Make a Reservation");

        List<Room> availableRooms = guestService.viewAvailableRooms();

        if (availableRooms.isEmpty()) {
            DisplayUtils.printError("No rooms available at this time.");
            return;
        }
        DisplayUtils.printRooms(availableRooms);

        System.out.print("\n  Enter Room ID from the list above: ");
        String roomId = scanner.nextLine().trim();

        Room selectedRoom = null;
        for (int i = 0; i < availableRooms.size(); i++) {
            if (availableRooms.get(i).getRoomId().equals(roomId)) {
                selectedRoom = availableRooms.get(i);
                break;
            }
        }
        if (selectedRoom == null) {
            DisplayUtils.printError("Invalid Room ID. Please select from the available rooms.");
            return;
        }

        LocalDate checkIn  = promptDate("  Check-In Date  (yyyy-MM-dd): ");
        LocalDate checkOut = promptDate("  Check-Out Date (yyyy-MM-dd): ");
        if (checkIn == null || checkOut == null) return;

        try {
            Reservation res = guestService.makeReservation(currentGuest, roomId, checkIn, checkOut);
            DisplayUtils.printSuccess("Reservation created! ID: " + res.getReservationId());
            System.out.printf("  Total cost: EGP %.2f for %d night(s)%n",
                    res.getTotalCost(), res.getNumberOfNights());
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void viewReservations() {
        DisplayUtils.printHeader("My Reservations");
        List<Reservation> reservations = guestService.viewMyReservations(currentGuest);
        DisplayUtils.printReservations(reservations);
    }

    private void cancelReservation() {
        DisplayUtils.printHeader("Cancel Reservation");
        viewReservations();
        System.out.print("\n  Enter Reservation ID to cancel: ");
        String reservationId = scanner.nextLine();
        try {
            guestService.cancelReservation(currentGuest, reservationId);
            DisplayUtils.printSuccess("Reservation cancelled successfully.");
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void payInvoice() {
        DisplayUtils.printHeader("Pay Invoice");
        System.out.print("Enter Reservation ID: ");
        String reservationId = scanner.nextLine();

        System.out.println("Payment method");
        System.out.println("\n1. My Balance");
        System.out.println("2. Cash");
        System.out.println("3. Credit Card");
        System.out.println("4. Debit Card");
        System.out.print("\nChoice: ");
        int paymentMethod = scanner.nextInt();
        scanner.nextLine();

        try {
            Invoice invoice;

            if (paymentMethod == 1) {
                invoice = guestService.checkoutWithBalance(currentGuest, reservationId);
            }
            else if (paymentMethod == 2) {
                invoice = guestService.checkoutWithExternalPayment(reservationId, invoice_amountDue(reservationId), PaymentMethod.CASH);
            }
            else if (paymentMethod == 3) {
                invoice = guestService.checkoutWithExternalPayment(reservationId, invoice_amountDue(reservationId), PaymentMethod.CREDIT_CARD);
            }
            else if (paymentMethod == 4) {
                invoice = guestService.checkoutWithExternalPayment(reservationId, invoice_amountDue(reservationId), PaymentMethod.DEBIT_CARD);
            }
            else {
                DisplayUtils.printError("Invalid payment method.");
                return;
            }

            DisplayUtils.printSuccess("Payment successful!");
            System.out.println(invoice.getFormattedInvoice());
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private double invoice_amountDue(String reservationId) {
        // Helper — get the amount due for a reservation (so external payments know how much to charge)
        List<Invoice> myInvoices = guestService.viewMyInvoices(currentGuest);
        for (Invoice inv : myInvoices) {
            if (inv.getReservationId().equals(reservationId) && !inv.isPaid()) {
                return inv.getTotalAmountDue();
            }
        }
        throw new IllegalArgumentException("No unpaid invoice found for reservation: " + reservationId);
    }

    private void viewInvoices() {
        DisplayUtils.printHeader("My Invoices");
        List<Invoice> invoices = guestService.viewMyInvoices(currentGuest);
        if (invoices.isEmpty()) {
            System.out.println("No invoices found.");
            return;
        }
        for (Invoice inv : invoices) {
            System.out.println(inv.getFormattedInvoice());
            System.out.println();
        }
    }

    private void depositBalance() {
        DisplayUtils.printHeader("Deposit Balance");
        System.out.printf("Current Balance: EGP %.2f%n", currentGuest.getBalance());
        System.out.print("Amount to deposit (EGP): ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            guestService.depositBalance(currentGuest, amount);
            DisplayUtils.printSuccess(String.format("Deposited EGP %.2f. New balance: EGP %.2f", amount, currentGuest.getBalance()));
        }
        catch (NumberFormatException e) {
            DisplayUtils.printError("Invalid amount.");
        }
        catch (Exception e) {
            DisplayUtils.printError(e.getMessage());
        }
    }

    private void viewProfile() {
        DisplayUtils.printHeader("My Profile");
        System.out.println("Username: " + currentGuest.getUsername());
        System.out.println("Gender: " + currentGuest.getGender());
        System.out.println("Date of Birth: " + currentGuest.getDateOfBirth());
        System.out.println("Address: " + currentGuest.getAddress());
        System.out.printf ("Balance: EGP %.2f%n", currentGuest.getBalance());
        System.out.println("Preferences: " + currentGuest.getRoomPreferences());
    }

    private void updatePreferences() {
        DisplayUtils.printHeader("Update Room Preferences");
        System.out.print("Preferred room type (Single - Double - Suite - Deluxe - Family, leave blank for none): ");
        String preferredType = scanner.nextLine();
        System.out.print("Preferred floor (number, leave blank for none): ");
        String floorStr = scanner.nextLine();
        System.out.print("Smoking room? (y/n): ");
        boolean smoking = scanner.nextLine().equalsIgnoreCase("y");
        System.out.print("Accessibility required? (y/n): ");
        boolean access = scanner.nextLine().equalsIgnoreCase("y");

        Integer floor = null;
        if (!floorStr.isEmpty()) {
            try {
                floor = Integer.parseInt(floorStr);
            }
            catch (NumberFormatException ignored) {
            }
        }

        String type = null;
        if (!preferredType.isEmpty()) {
            type = preferredType;
        }

        RoomPreference pref = new RoomPreference(type, floor, smoking, access);
        guestService.updateRoomPreferences(currentGuest, pref);
        DisplayUtils.printSuccess("Preferences updated.");
    }

    private LocalDate promptDate(String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim(), theDate);
        }
        catch (DateTimeParseException e) {
            DisplayUtils.printError("Invalid date format. Use yyyy-MM-dd.");
            return null;
        }
    }
}
