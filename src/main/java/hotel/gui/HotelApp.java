package hotel.gui;

import hotel.gui.utils.SceneManager;
import hotel.services.*;
import hotel.database.DatabaseSyncService;
import javafx.application.Application;
import javafx.stage.Stage;

public class HotelApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        InvoiceService      invoiceService      = new InvoiceService();
        RoomService         roomService         = new RoomService();
        ReservationService  reservationService  = new ReservationService(roomService, invoiceService);
        GuestService        guestService        = new GuestService(roomService, reservationService, invoiceService);
        AdminService        adminService        = new AdminService(roomService, guestService, invoiceService);
        ReceptionistService receptionistService = new ReceptionistService(reservationService, roomService, guestService);
        AuthService         authService         = new AuthService();

        SceneManager.init(primaryStage, authService, guestService, adminService,
                receptionistService, invoiceService, roomService, reservationService);

        primaryStage.setTitle("Hotel Reservation System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        SceneManager.getInstance().showLogin();
        
        DatabaseSyncService.startSyncTask();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Application closing... Flushing data to Supabase...");
        DatabaseSyncService.forceSync();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
