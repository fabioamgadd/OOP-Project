package hotel.gui.controllers;

import hotel.database.HotelDatabase;
import hotel.gui.utils.AlertHelper;
import hotel.gui.utils.SceneManager;
import hotel.models.*;
import hotel.services.AdminService;
import hotel.services.ReceptionistService;
import hotel.services.RoomService;
import hotel.network.ChatServer;
import hotel.database.DatabaseSyncService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StaffDashboardController implements Initializable {

    @FXML private Label staffNameLabel, roleLabel;
    @FXML private StackPane contentArea;

    @FXML private Button btnGuests, btnRooms, btnAllRes, btnCheckIn, btnCheckOut;
    @FXML private Button btnRoomTypes, btnAmenities, btnInvoices, btnRevenue, btnChat;

    private Staff currentStaff;
    private boolean isAdmin;
    private AdminService adminService;
    private ReceptionistService receptionistService;
    private RoomService roomService;

    private ScheduledExecutorService roomRefreshScheduler;
    private TableView<Room>          liveStaffRoomTable         = null;
    private Label                    staffRoomRefreshStatusLabel = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SceneManager sm = SceneManager.getInstance();
        currentStaff        = sm.getCurrentStaff();
        adminService        = sm.adminService;
        receptionistService = sm.receptionistService;
        roomService         = sm.roomService;
        isAdmin = (currentStaff instanceof Admin);

        staffNameLabel.setText(currentStaff.getUsername());
        roleLabel.setText(isAdmin ? "ADMIN" : "RECEPTIONIST");

        if (!isAdmin) {
            btnRoomTypes.setVisible(false); btnRoomTypes.setManaged(false);
            btnAmenities.setVisible(false); btnAmenities.setManaged(false);
            btnInvoices.setVisible(false);  btnInvoices.setManaged(false);
            btnRevenue.setVisible(false);   btnRevenue.setManaged(false);
        }

        showGuests();
        startRoomStatusRefresh();
        
        DatabaseSyncService.startSyncTask();
        ChatServer.getInstance().start();
    }

    private void startRoomStatusRefresh() {
        roomRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "staff-room-refresh");
            t.setDaemon(true);
            return t;
        });

        roomRefreshScheduler.scheduleAtFixedRate(() -> {

            final List<Room> freshRooms = isAdmin
                    ? adminService.getAllRooms()
                    : receptionistService.getAllRooms();
            final String ts = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

            Platform.runLater(() -> {
                Node current = contentArea.getChildren().isEmpty()
                        ? null : contentArea.getChildren().get(0);
                if (current != null && "staffRoomsPane".equals(current.getId())
                        && liveStaffRoomTable != null) {
                    liveStaffRoomTable.setItems(FXCollections.observableArrayList(freshRooms));
                    if (staffRoomRefreshStatusLabel != null)
                        staffRoomRefreshStatusLabel.setText(
                                "Last refreshed: " + ts + "  |  " + freshRooms.size() + " room(s)");
                }
            });

        }, 15, 15, TimeUnit.SECONDS);
    }

    public void stopRefresh() {
        if (roomRefreshScheduler != null && !roomRefreshScheduler.isShutdown())
            roomRefreshScheduler.shutdown();
    }

    @FXML private void handleLogout() { stopRefresh(); ChatServer.getInstance().setOnMessageReceived(null);; SceneManager.getInstance().showLogin(); }


    @FXML private void showGuests()          { setActive(btnGuests);    buildGuestsPaneAsync(); }
    @FXML private void showRooms()           { setActive(btnRooms);     buildRoomsPane(); }
    @FXML private void showAllReservations() { setActive(btnAllRes);    buildReservationsPaneAsync(); }
    @FXML private void showCheckIn()         { setActive(btnCheckIn);   buildCheckInPane(); }
    @FXML private void showCheckOut()        { setActive(btnCheckOut);  buildCheckOutPane(); }
    @FXML private void showRoomTypes()       { if (isAdmin) { setActive(btnRoomTypes); buildRoomTypesPane(); } }
    @FXML private void showAmenities()       { if (isAdmin) { setActive(btnAmenities); buildAmenitiesPane(); } }
    @FXML private void showInvoices()        { if (isAdmin) { setActive(btnInvoices);  buildInvoicesPane(); } }
    @FXML private void showRevenue()         { if (isAdmin) { setActive(btnRevenue);   buildRevenuePane(); } }

    @FXML private void showChat() {
        setActive(btnChat);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/hotel/gui/fxml/ChatWindow.fxml"));
            VBox chatPane = loader.load();
            hotel.gui.controllers.ChatController controller = loader.getController();
            controller.initializeChat(true, currentStaff.getUsername());
            setContent(chatPane);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            setContent(new Label("Failed to load chat: " + e.getMessage()));
        }
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnGuests, btnRooms, btnAllRes, btnCheckIn, btnCheckOut,
                btnRoomTypes, btnAmenities, btnInvoices, btnRevenue, btnChat})
            if (b != null) b.getStyleClass().remove("active");
        if (active != null) active.getStyleClass().add("active");
    }

    private void setContent(Node node) { contentArea.getChildren().setAll(node); }

    private VBox makeLoadingPane(String message) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);
        Label label = new Label(message);
        label.setStyle("-fx-text-fill:#64748B; -fx-font-size:13px;");
        VBox box = new VBox(12, spinner, label);
        box.setAlignment(Pos.CENTER);
        box.setPrefHeight(200);
        return box;
    }

    private void buildGuestsPaneAsync() {
        VBox pane = sectionPane("All Registered Guests");
        pane.getChildren().add(makeLoadingPane("Loading guest list…"));
        setContent(pane);

        Task<List<Guest>> task = new Task<>() {
            @Override protected List<Guest> call() {
                return isAdmin ? adminService.getAllGuests() : receptionistService.getAllGuests();
            }
        };

        task.setOnSucceeded(e -> {
            VBox card = card();
            TableView<Guest> table = new TableView<>();
            table.getStyleClass().add("table-view");
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(400);
            table.getColumns().addAll(
                    col("Guest ID", g -> g.getGuestId()),
                    col("Username", g -> g.getUsername()),
                    col("Gender",   g -> g.getGender().toString()),
                    col("DOB",      g -> g.getDateOfBirth().toString()),
                    col("Address",  g -> g.getAddress()),
                    col("Balance",  g -> String.format("EGP %.2f", g.getBalance()))
            );
            table.setItems(FXCollections.observableArrayList(task.getValue()));
            card.getChildren().add(table);
            pane.getChildren().setAll(titleLabel("All Registered Guests"), card);
        });

        task.setOnFailed(e -> {
            Label err = new Label("Failed to load guests: " + task.getException().getMessage());
            err.setStyle("-fx-text-fill:#DC2626;");
            pane.getChildren().setAll(titleLabel("All Registered Guests"), err);
        });

        daemon(task, "guests-load-thread");
    }

    private void buildRoomsPane() {
        VBox pane = sectionPane("All Rooms");
        pane.setId("staffRoomsPane");

        staffRoomRefreshStatusLabel = new Label("Auto-refreshes every 15 seconds");
        staffRoomRefreshStatusLabel.setStyle(
                "-fx-text-fill:#94A3B8; -fx-font-size:11px; -fx-font-style:italic;");

        VBox card = card();
        List<Room> rooms = isAdmin ? adminService.getAllRooms() : receptionistService.getAllRooms();

        liveStaffRoomTable = new TableView<>();
        liveStaffRoomTable.getStyleClass().add("table-view");
        liveStaffRoomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        liveStaffRoomTable.setPrefHeight(350);
        liveStaffRoomTable.getColumns().addAll(
                col("Room ID",    r -> r.getRoomId()),
                col("Floor",      r -> String.valueOf(r.getFloorNumber())),
                col("Type",       r -> r.getRoomType().getTypeName()),
                col("Available",  r -> r.isAvailable() ? "Yes" : "No"),
                col("Price/Night",r -> String.format("EGP %.0f", r.getTotalPricePerNight())),
                col("Amenities",  r -> {
                    if (r.getAmenities().isEmpty()) return "—";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < r.getAmenities().size(); i++) { if (i > 0) sb.append(", "); sb.append(r.getAmenities().get(i).getName()); }
                    return sb.toString();
                })
        );
        liveStaffRoomTable.setItems(FXCollections.observableArrayList(rooms));

        if (isAdmin) card.getChildren().add(buildAdminRoomPanel(liveStaffRoomTable));
        card.getChildren().add(liveStaffRoomTable);

        pane.getChildren().addAll(staffRoomRefreshStatusLabel, card);
        setContent(pane);
    }

    private VBox buildAdminRoomPanel(TableView<Room> table) {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-padding:0 0 12 0;");
        Label panelTitle = new Label("Add New Room");
        panelTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:#1D4ED8;");

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        TextField floorField  = new TextField(); floorField.setPromptText("Floor"); floorField.setPrefWidth(70);
        TextField roomIdField = new TextField(); roomIdField.setPromptText("Room ID (auto)"); roomIdField.setPrefWidth(110);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setPromptText("Room Type");
        for (RoomType rt : adminService.getAllRoomTypes()) typeCombo.getItems().add(rt.getTypeName());

        Label addMsg = new Label("");
        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setStyle("-fx-padding:6 14 6 14;");

        floorField.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused && !floorField.getText().trim().isEmpty()) {
                try {
                    int floor = Integer.parseInt(floorField.getText().trim());
                    int maxId = floor * 100;
                    for (Room r : adminService.getAllRooms()) {
                        if (r.getFloorNumber() == floor) {
                            try { int id = Integer.parseInt(r.getRoomId()); if (id > maxId) maxId = id; } catch (NumberFormatException ignored) {}
                        }
                    }
                    roomIdField.setText(String.valueOf(maxId + 1));
                } catch (NumberFormatException ignored) {}
            }
        });

        addBtn.setOnAction(e -> {
            addMsg.setText("");
            try {
                int floor = Integer.parseInt(floorField.getText().trim());
                RoomType rt = null;
                for (RoomType r : adminService.getAllRoomTypes())
                    if (r.getTypeName().equals(typeCombo.getValue())) { rt = r; break; }
                if (rt == null) { addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText("Select a valid room type."); return; }
                adminService.addRoom(new Room(roomIdField.getText().trim(), floor, rt));
                addMsg.setStyle("-fx-text-fill:#15803D;"); addMsg.setText("Room " + roomIdField.getText().trim() + " added.");
                floorField.clear(); roomIdField.clear(); typeCombo.setValue(null);
                buildRoomsPane();
            } catch (NumberFormatException ex) {
                addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText("Invalid floor number.");
            } catch (Exception ex) {
                addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText(ex.getMessage());
            }
        });

        Button delBtn = new Button("Delete Selected");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setStyle("-fx-padding:6 14 6 14;");
        Label delMsg = new Label("");
        delBtn.setOnAction(e -> {
            Room sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { delMsg.setStyle("-fx-text-fill:#DC2626;"); delMsg.setText("Select a room first."); return; }
            if (!AlertHelper.showConfirm("Delete Room", "Delete room " + sel.getRoomId() + "?")) return;
            adminService.deleteRoom(sel.getRoomId());
            delMsg.setStyle("-fx-text-fill:#15803D;"); delMsg.setText("Room deleted.");
            buildRoomsPane();
        });

        row.getChildren().addAll(new Label("Floor:"), floorField, new Label("Room ID:"), roomIdField,
                new Label("Type:"), typeCombo, addBtn, delBtn);
        panel.getChildren().addAll(panelTitle, row, addMsg, delMsg);
        return panel;
    }

    private void buildReservationsPaneAsync() {
        VBox pane = sectionPane("All Reservations");
        pane.getChildren().add(makeLoadingPane("Loading reservations…"));
        setContent(pane);

        Task<List<Reservation>> task = new Task<>() {
            @Override protected List<Reservation> call() {
                return isAdmin ? adminService.getAllReservations() : receptionistService.getAllReservations();
            }
        };

        task.setOnSucceeded(e -> {
            VBox card = card();
            TableView<Reservation> table = new TableView<>();
            table.getStyleClass().add("table-view");
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(420);
            table.getColumns().addAll(
                    col("Res ID",    r -> r.getReservationId()),
                    col("Guest ID",  r -> r.getGuestId()),
                    col("Room",      r -> r.getRoomId()),
                    col("Check-In",  r -> r.getCheckInDate().toString()),
                    col("Check-Out", r -> r.getCheckOutDate().toString()),
                    col("Nights",    r -> String.valueOf(r.getNumberOfNights())),
                    col("Cost",      r -> String.format("EGP %.2f", r.getTotalCost())),
                    col("Status",    r -> r.getStatus().toString())
            );
            table.setItems(FXCollections.observableArrayList(task.getValue()));
            card.getChildren().add(table);
            pane.getChildren().setAll(titleLabel("All Reservations"), card);
        });

        task.setOnFailed(e -> {
            Label err = new Label("Failed to load reservations: " + task.getException().getMessage());
            err.setStyle("-fx-text-fill:#DC2626;");
            pane.getChildren().setAll(titleLabel("All Reservations"), err);
        });

        daemon(task, "reservations-load-thread");
    }

    private void buildCheckInPane() {
        VBox pane = sectionPane("Check In Guest");
        VBox card = card();
        card.setMaxWidth(500);

        Label infoLabel = new Label("Enter a Reservation ID to check in the guest.\nReservation must be in CONFIRMED status.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-text-fill:#334155;");

        TextField resIdField = new TextField();
        resIdField.setPromptText("Reservation ID (e.g. R001)");
        Label msgLabel = new Label("");
        msgLabel.setWrapText(true);

        Button checkInBtn = new Button("Check In");
        checkInBtn.getStyleClass().add("btn-success");
        checkInBtn.setOnAction(e -> {
            msgLabel.setText("");
            String resId = resIdField.getText().trim();
            if (resId.isEmpty()) { msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText("Please enter a reservation ID."); return; }
            try {
                receptionistService.checkIn(resId);
                Reservation res = receptionistService.findReservationById(resId);
                msgLabel.setStyle("-fx-text-fill:#15803D;");
                msgLabel.setText("Check-in successful!\nReservation " + resId + " is now: " + res.getStatus());
                resIdField.clear();
            } catch (Exception ex) {
                msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText(ex.getMessage());
            }
        });

        Label arriving = new Label("Today's Arrivals (Check-In Date = Today):");
        arriving.setStyle("-fx-font-weight:bold; -fx-padding:16 0 4 0; -fx-text-fill:#1D4ED8;");
        TableView<Reservation> todayTable = new TableView<>();
        todayTable.getStyleClass().add("table-view");
        todayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        todayTable.setPrefHeight(180);
        todayTable.getColumns().addAll(
                col("Res ID", r -> r.getReservationId()),
                col("Guest",  r -> r.getGuestId()),
                col("Room",   r -> r.getRoomId()),
                col("Status", r -> r.getStatus().toString())
        );
        java.time.LocalDate today = java.time.LocalDate.now();
        javafx.collections.ObservableList<Reservation> todayArrivals = FXCollections.observableArrayList();
        for (Reservation r : receptionistService.getAllReservations())
            if (r.getCheckInDate().equals(today) && r.getStatus() == hotel.enums.ReservationStatus.CONFIRMED)
                todayArrivals.add(r);
        todayTable.setItems(todayArrivals);
        todayTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) resIdField.setText(sel.getReservationId());
        });

        card.getChildren().addAll(infoLabel, resIdField, checkInBtn, msgLabel, arriving, todayTable);
        pane.getChildren().add(card);
        setContent(pane);
    }

    private void buildCheckOutPane() {
        VBox pane = sectionPane("Check Out Guest");
        VBox card = card();
        card.setMaxWidth(500);

        Label infoLabel = new Label("Enter a Reservation ID to check out the guest.\nReservation must be in CHECKED_IN status.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-text-fill:#334155;");

        TextField resIdField = new TextField();
        resIdField.setPromptText("Reservation ID (e.g. R001)");
        Label msgLabel = new Label("");
        msgLabel.setWrapText(true);

        Button checkOutBtn = new Button("Check Out");
        checkOutBtn.getStyleClass().add("btn-danger");
        checkOutBtn.setOnAction(e -> {
            msgLabel.setText("");
            String resId = resIdField.getText().trim();
            if (resId.isEmpty()) { msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText("Please enter a reservation ID."); return; }
            try {
                receptionistService.checkOut(resId);
                Reservation res = receptionistService.findReservationById(resId);
                msgLabel.setStyle("-fx-text-fill:#15803D;");
                msgLabel.setText("Check-out successful!\nReservation " + resId + " is now: " + res.getStatus());
                resIdField.clear();
            } catch (Exception ex) {
                msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText(ex.getMessage());
            }
        });

        Label checkedInLabel = new Label("Currently Checked-In:");
        checkedInLabel.setStyle("-fx-font-weight:bold; -fx-padding:16 0 4 0; -fx-text-fill:#1D4ED8;");
        TableView<Reservation> checkedInTable = new TableView<>();
        checkedInTable.getStyleClass().add("table-view");
        checkedInTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        checkedInTable.setPrefHeight(200);
        checkedInTable.getColumns().addAll(
                col("Res ID",    r -> r.getReservationId()),
                col("Guest",     r -> r.getGuestId()),
                col("Room",      r -> r.getRoomId()),
                col("Check-Out", r -> r.getCheckOutDate().toString()),
                col("Status",    r -> r.getStatus().toString())
        );
        javafx.collections.ObservableList<Reservation> checkedIn = FXCollections.observableArrayList();
        for (Reservation r : receptionistService.getAllReservations())
            if (r.getStatus() == hotel.enums.ReservationStatus.CHECKED_IN) checkedIn.add(r);
        checkedInTable.setItems(checkedIn);
        checkedInTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) resIdField.setText(sel.getReservationId());
        });

        card.getChildren().addAll(infoLabel, resIdField, checkOutBtn, msgLabel, checkedInLabel, checkedInTable);
        pane.getChildren().add(card);
        setContent(pane);
    }


    private void buildRoomTypesPane() {
        VBox pane = sectionPane("Room Types");
        VBox card = card();

        TableView<RoomType> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(260);
        table.getColumns().addAll(
                col("ID",          rt -> rt.getTypeId()),
                col("Name",        rt -> rt.getTypeName()),
                col("Description", rt -> rt.getDescription()),
                col("Base Price",  rt -> String.format("EGP %.2f", rt.getBasePricePerNight())),
                col("Max Occ.",    rt -> String.valueOf(rt.getMaxOccupancy()))
        );
        table.setItems(FXCollections.observableArrayList(adminService.getAllRoomTypes()));

        Label addTitle = new Label("Add Room Type");
        addTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:#1D4ED8; -fx-padding:12 0 4 0;");
        HBox addRow = new HBox(10);
        addRow.setAlignment(Pos.CENTER_LEFT);
        TextField nameF  = field("Name",       100);
        TextField descF  = field("Description",160);
        TextField priceF = field("Base Price",  90);
        TextField occF   = field("Max Occ.",    80);
        Label addMsg = new Label("");
        Button addBtn = btn("Add", "btn-primary");
        addBtn.setOnAction(e -> {
            try {
                RoomType rt = new RoomType(nameF.getText().trim(), descF.getText().trim(),
                        Double.parseDouble(priceF.getText().trim()), Integer.parseInt(occF.getText().trim()));
                adminService.addRoomType(rt);
                addMsg.setStyle("-fx-text-fill:#15803D;"); addMsg.setText("Room type added.");
                nameF.clear(); descF.clear(); priceF.clear(); occF.clear();
                buildRoomTypesPane();
            } catch (Exception ex) { addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText(ex.getMessage()); }
        });
        Button delBtn = btn("Delete Selected", "btn-danger");
        delBtn.setOnAction(e -> {
            RoomType sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText("Select a type first."); return; }
            if (AlertHelper.showConfirm("Delete", "Delete room type: " + sel.getTypeName() + "?")) {
                adminService.deleteRoomType(sel.getTypeId()); buildRoomTypesPane();
            }
        });
        addRow.getChildren().addAll(new Label("Name:"), nameF, new Label("Desc:"), descF,
                new Label("Price:"), priceF, new Label("Occ:"), occF, addBtn, delBtn);
        card.getChildren().addAll(table, addTitle, addRow, addMsg);
        pane.getChildren().add(card);
        setContent(pane);
    }


    private void buildAmenitiesPane() {
        VBox pane = sectionPane("Amenities");
        VBox card = card();

        TableView<Amenity> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(260);
        table.getColumns().addAll(
                col("ID",                  a -> a.getAmenityId()),
                col("Name",                a -> a.getName()),
                col("Description",         a -> a.getDescription()),
                col("Extra Cost/Night",    a -> String.format("EGP %.2f", a.getExtraCostPerNight()))
        );
        table.setItems(FXCollections.observableArrayList(adminService.getAllAmenities()));

        Label addTitle = new Label("Add Amenity");
        addTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:#1D4ED8; -fx-padding:12 0 4 0;");
        HBox addRow = new HBox(10);
        addRow.setAlignment(Pos.CENTER_LEFT);
        TextField nameF = field("Name",       120);
        TextField descF = field("Description",180);
        TextField costF = field("Extra Cost",  90);
        Label addMsg = new Label("");
        Button addBtn = btn("Add", "btn-primary");
        addBtn.setOnAction(e -> {
            try {
                Amenity amenity = new Amenity(nameF.getText().trim(), descF.getText().trim(),
                        Double.parseDouble(costF.getText().trim()));
                adminService.addAmenity(amenity);
                addMsg.setStyle("-fx-text-fill:#15803D;"); addMsg.setText("Amenity added: " + amenity.getAmenityId());
                nameF.clear(); descF.clear(); costF.clear();
                buildAmenitiesPane();
            } catch (Exception ex) { addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText(ex.getMessage()); }
        });
        Button delBtn = btn("Delete Selected", "btn-danger");
        delBtn.setOnAction(e -> {
            Amenity sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { addMsg.setStyle("-fx-text-fill:#DC2626;"); addMsg.setText("Select an amenity first."); return; }
            if (AlertHelper.showConfirm("Delete", "Delete amenity: " + sel.getName() + "?")) {
                adminService.deleteAmenity(sel.getAmenityId()); buildAmenitiesPane();
            }
        });
        addRow.getChildren().addAll(new Label("Name:"), nameF, new Label("Desc:"), descF,
                new Label("Cost:"), costF, addBtn, delBtn);
        card.getChildren().addAll(table, addTitle, addRow, addMsg);
        pane.getChildren().add(card);
        setContent(pane);
    }


    private void buildInvoicesPane() {
        VBox pane = sectionPane("All Invoices");
        VBox card = card();

        TableView<Invoice> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);
        table.getColumns().addAll(
                col("Invoice ID",  i -> i.getInvoiceId()),
                col("Res ID",      i -> i.getReservationId()),
                col("Guest ID",    i -> i.getGuestId()),
                col("Amount Due",  i -> String.format("EGP %.2f", i.getTotalAmountDue())),
                col("Amount Paid", i -> String.format("EGP %.2f", i.getAmountPaid())),
                col("Status",      i -> i.isPaid() ? "PAID" : "UNPAID"),
                col("Method",      i -> i.getPaymentMethod() != null ? i.getPaymentMethod().toString() : "—"),
                col("Issued",      i -> i.getIssuedDate().toString())
        );
        table.setItems(FXCollections.observableArrayList(adminService.getAllInvoices()));
        card.getChildren().add(table);
        pane.getChildren().add(card);
        setContent(pane);
    }


    private void buildRevenuePane() {
        VBox pane = sectionPane("Revenue Summary");

        HBox kpiRow = new HBox(16);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        kpiRow.getChildren().addAll(
                kpiCard("Total Revenue",       String.format("EGP %.0f", adminService.getTotalRevenue())),
                kpiCard("Occupied Rooms",       String.valueOf(adminService.getOccupiedRoomCount())),
                kpiCard("Available Rooms",      String.valueOf(adminService.getAvailableRoomCount())),
                kpiCard("Total Guests",         String.valueOf(adminService.getAllGuests().size())),
                kpiCard("Total Reservations",   String.valueOf(adminService.getAllReservations().size()))
        );
        pane.getChildren().add(kpiRow);

        VBox card = card();
        Label breakdownTitle = new Label("Invoice Breakdown");
        breakdownTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1D4ED8; -fx-padding:0 0 6 0;");
        long paidCount   = adminService.getAllInvoices().stream().filter(Invoice::isPaid).count();
        long unpaidCount = adminService.getAllInvoices().stream().filter(i -> !i.isPaid()).count();
        card.getChildren().addAll(breakdownTitle,
                infoRow("Paid Invoices",   String.valueOf(paidCount)),
                infoRow("Unpaid Invoices", String.valueOf(unpaidCount)),
                infoRow("Total Invoices",  String.valueOf(adminService.getAllInvoices().size())));
        pane.getChildren().add(card);
        setContent(pane);
    }


    private VBox sectionPane(String titleText) {
        VBox pane = new VBox(16);
        pane.getChildren().add(titleLabel(titleText));
        return pane;
    }

    private Label titleLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");
        return l;
    }

    private VBox card() { VBox c = new VBox(10); c.getStyleClass().add("card"); return c; }

    private VBox kpiCard(String label, String value) {
        VBox card = new VBox(4);
        card.getStyleClass().add("kpi-card");
        card.setPrefWidth(140);
        card.setAlignment(Pos.CENTER);
        Label val = new Label(value); val.getStyleClass().add("kpi-value");
        Label lbl = new Label(label); lbl.getStyleClass().add("kpi-title");
        card.getChildren().addAll(val, lbl);
        return card;
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":"); lbl.setStyle("-fx-font-weight:bold; -fx-min-width:160px;");
        row.getChildren().addAll(lbl, new Label(value));
        return row;
    }

    private TextField field(String prompt, double width) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(width); return tf;
    }

    private Button btn(String text, String styleClass) {
        Button b = new Button(text); b.getStyleClass().add(styleClass);
        b.setStyle("-fx-padding:6 12 6 12;"); return b;
    }

    private void daemon(Task<?> task, String name) {
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        t.start();
    }

    private <T> TableColumn<T, String> col(String header, java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> c = new TableColumn<>(header);
        c.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return c;
    }
}
