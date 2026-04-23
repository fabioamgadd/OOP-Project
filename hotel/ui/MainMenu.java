package hotel.ui;

import hotel.enums.Gender;
import hotel.interfaces.Authenticatable;
import hotel.models.*;
import hotel.services.*;
import hotel.utils.DisplayUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;


public class MainMenu {
    private static final DateTimeFormatter theDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Scanner scanner = new Scanner(System.in);

    private final AuthService         authService;
    private final GuestService        guestService;
    private final AdminService        adminService;
    private final ReceptionistService receptionistService;

    public MainMenu(Scanner scanner, AuthService authService, GuestService guestService, AdminService adminService, ReceptionistService receptionistService) {
        this.scanner = scanner;
        this.authService = authService;
        this.guestService = guestService;
        this.adminService = adminService;
        this.receptionistService = receptionistService;
    }

    public void run() {
        DisplayUtils.printHeader("Hotel Reservation System");
        System.out.println("\nMain Menu");

        boolean running = true;

        while(running) {
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("\nEnter Choice: ");
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
        DisplayUtils.printHeader("Login");

        System.out.print("\nUsername: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Authenticatable user = authService.login(username, password);

        if (user == null) {
            DisplayUtils.printError("Invalid credentials. Please try again.");
            return;
        }

        DisplayUtils.printSuccess("Login Successfully, Welcome Back " + user.getUsername() + "!");

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
            DisplayUtils.printError("Unknown Account Type!");
        }
    }

    private void handleRegister() {
        DisplayUtils.printHeader("Guest Register");

        System.out.print("\nEnter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Date of Birth (yyyy-mm-dd): ");
        LocalDate DOB;

        try {
            DOB = LocalDate.parse(scanner.nextLine(), theDate);
        }
        catch (DateTimeParseException e) {
            DisplayUtils.printError("Invalid date format.");
            return;
        }

        System.out.print("Address: ");
        String address = scanner.nextLine();
        System.out.print("Gender (M/F): ");
        String inputGender = scanner.nextLine().toUpperCase();
        Gender gender;
        if (inputGender.equals("M")) {
            gender = Gender.MALE;
        }
        else if (inputGender.equals("F")) {
            gender = Gender.FEMALE;
        }
        else {
            DisplayUtils.printError("Invalid Input, Enter (M or F");
            return;
        }

        try {
            Guest guest = authService.registerGuest(username, password, DOB, address, gender);
            DisplayUtils.printSuccess("Registered successfully! Your guest ID: " + guest.getGuestId());
            DisplayUtils.printInfo("You can now log in with your info.");
        }
        catch (IllegalArgumentException e) {
            DisplayUtils.printError(e.getMessage());
        }
    }
}
