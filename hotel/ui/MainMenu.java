package hotel.ui;

import hotel.enums.Gender;
import hotel.interfaces.Authenticatable;
import hotel.models.*;
import hotel.services.*;
import hotel.ui.AdminMenu;
import hotel.ui.GuestMenu;
import hotel.ui.ReceptionistMenu;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;


public class MainMenu {
    private static final DateTimeFormatter theDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Scanner scanner = new Scanner(System.in);

    private final AuthService authService;
    private final GuestService guestService;
    private final AdminService adminService;
    private final ReceptionistService receptionistService;

    public MainMenu(Scanner scanner, AuthService authService, GuestService guestService, AdminService adminService, ReceptionistService receptionistService) {
        this.scanner = scanner;
        this.authService = authService;
        this.guestService = guestService;
        this.adminService = adminService;
        this.receptionistService = receptionistService;
    }

    public void run() {
        System.out.println("Hotel Reservation System");
        System.out.println("\nMain Menu");

        boolean running = true;

        while(running) {
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.println("\nEnter Choice:" + "\n");
            int Choice = scanner.nextInt();
            scanner.nextLine();

            if (Choice == 1) {
                handleLogin();
            }
            else if (Choice == 2) {
                handleRegister();
            }
            else if (Choice == 3) {
                running = false;
            }
            else {
                System.out.println("Invalid input, please try again.");
            }
        }
    }

    private void handleLogin() {
        System.out.println("\n\n\n\n\n\n\n\n\nLogin");

        System.out.println("\nUsername: ");
        String username = scanner.nextLine();
        System.out.print("  Password: ");
        String password = scanner.nextLine();

        Authenticatable user = authService.login(username, password);

        if (user == null) {
            System.out.println("Invalid credentials. Please try again.");
            return;
        }

        System.out.println("Login Successfully, Welcome Back " + user.getUsername() + "!");

        if (user instanceof Guest) {
            GuestMenu guestmenu = new GuestMenu(scanner, guestService, (Guest) user);
            guestmenu.show();
        }
        else if (user instanceof Admin) {
            AdminMenu adminmenu = new AdminMenu(scanner, adminService, (Staff) user);
            adminmenu.show();
        }
        else if(user instanceof Receptionist) {
            ReceptionistMenu receptionistmenu = new ReceptionistMenu(scanner, receptionistService, (Staff) user);
            receptionistmenu.show();
        }
        else {
            System.out.println("Unknown Account Type!");
        }
    }

    private void handleRegister() {
        System.out.println("\n\n\n\n\n\n\n\n\nGuest Register");

        System.out.println("\nEnter Username: ");
        String username = scanner.nextLine();
        System.out.println("Enter Password: ");
        String password = scanner.nextLine();
        System.out.println("Enter Date of Birth (yyyy-mm-dd): ");
        LocalDate DOB;

        try {
            DOB = LocalDate.parse(scanner.nextLine(), theDate);
        }
        catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            return;
        }

        System.out.println("Address: ");
        String address = scanner.nextLine();
        System.out.println("Gender (M/F): ");
        String inputGender = scanner.nextLine().toUpperCase();
        Gender gender;
        if (inputGender.equals("M")) {
            gender = Gender.MALE;
        }
        else if (inputGender.equals("F")) {
            gender = Gender.FEMALE;
        }
        else {
            System.out.println("Invalid Input, Enter (M or F");
            return;
        }

        try {
            Guest guest = authService.registerGuest(username, password, DOB, address, gender);
            System.out.println("Registered successfully! Your guest ID: " + guest.getGuestId());
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
