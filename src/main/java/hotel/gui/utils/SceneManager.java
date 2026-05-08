package hotel.gui.utils;

import hotel.models.Guest;
import hotel.models.Staff;
import hotel.services.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class SceneManager {

    private static SceneManager instance;

    public static SceneManager getInstance() {
        return instance;
    }

    public static void init(Stage primaryStage,
                            AuthService authService,
                            GuestService guestService,
                            AdminService adminService,
                            ReceptionistService receptionistService,
                            InvoiceService invoiceService,
                            RoomService roomService,
                            ReservationService reservationService) {
        instance = new SceneManager(primaryStage, authService, guestService,
                adminService, receptionistService, invoiceService,
                roomService, reservationService);
    }

    private final Stage stage;

    public final AuthService         authService;
    public final GuestService        guestService;
    public final AdminService        adminService;
    public final ReceptionistService receptionistService;
    public final InvoiceService      invoiceService;
    public final RoomService         roomService;
    public final ReservationService  reservationService;

    private Guest currentGuest;
    private Staff  currentStaff;

    private static final String CSS_PATH = "/hotel/gui/css/main.css";

    private SceneManager(Stage stage, AuthService auth, GuestService gs,
                         AdminService as, ReceptionistService rs,
                         InvoiceService is, RoomService roomS,
                         ReservationService resS) {
        this.stage               = stage;
        this.authService         = auth;
        this.guestService        = gs;
        this.adminService        = as;
        this.receptionistService = rs;
        this.invoiceService      = is;
        this.roomService         = roomS;
        this.reservationService  = resS;
    }


    public void showLogin() {
        currentGuest = null;
        currentStaff = null;
        loadScene("/hotel/gui/fxml/Login.fxml", "Hotel – Login");
    }

    public void showGuestDashboard(Guest guest) {
        this.currentGuest = guest;
        loadScene("/hotel/gui/fxml/GuestDashboard.fxml",
                "Hotel – Guest Dashboard");
    }

    public void showStaffDashboard(Staff staff) {
        this.currentStaff = staff;
        loadScene("/hotel/gui/fxml/StaffDashboard.fxml",
                "Hotel – Staff Dashboard");
    }


    private void loadScene(String fxmlPath, String title) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IllegalStateException("FXML not found: " + fxmlPath);
            }
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);

            URL css = getClass().getResource(CSS_PATH);
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
        }
    }


    public Guest getCurrentGuest() { return currentGuest; }
    public Staff  getCurrentStaff() { return currentStaff; }

    public Stage getStage() { return stage; }
}
