package hotel.gui.controllers;

import hotel.database.HotelDatabase;
import hotel.enums.PaymentMethod;
import hotel.enums.ReservationStatus;
import hotel.gui.utils.AlertHelper;
import hotel.gui.utils.SceneManager;
import hotel.models.*;
import hotel.services.GuestService;
import hotel.services.InvoiceService;
import hotel.services.ReservationService;
import hotel.network.ChatClient;
import hotel.database.DatabaseSyncService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuestDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnRooms, btnReserve, btnMyRes, btnPay, btnInvoices, btnDeposit, btnProfile, btnChat;

    private Guest currentGuest;
    private GuestService guestService;
    private InvoiceService invoiceService;
    private ReservationService reservationService;

    private ScheduledExecutorService roomRefreshScheduler;

    private String currentRoomTypeFilter = null;
    private Double currentBudgetFilter   = null;

    private TableView<Room> liveRoomTable         = null;
    private Label           roomRefreshStatusLabel = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SceneManager sm = SceneManager.getInstance();
        currentGuest       = sm.getCurrentGuest();
        guestService       = sm.guestService;
        invoiceService     = sm.invoiceService;
        reservationService = sm.reservationService;

        welcomeLabel.setText("Welcome, " + currentGuest.getUsername());

        showRooms();
        startRoomRefreshTask();
        
        DatabaseSyncService.startSyncTask();
    }

    private void startRoomRefreshTask() {
        roomRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "room-availability-refresh");
            t.setDaemon(true);
            return t;
        });

        roomRefreshScheduler.scheduleAtFixedRate(() -> {

            final List<Room> freshRooms = fetchRoomsForCurrentFilter();
            final String timestamp = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

            Platform.runLater(() -> {
                Node current = contentArea.getChildren().isEmpty()
                        ? null : contentArea.getChildren().get(0);
                if (current != null && "roomsPane".equals(current.getId())
                        && liveRoomTable != null) {
                    liveRoomTable.setItems(
                            FXCollections.observableArrayList(freshRooms));
                    if (roomRefreshStatusLabel != null) {
                        roomRefreshStatusLabel.setText(
                                "Last auto-refreshed: " + timestamp
                                + "  |  " + freshRooms.size() + " room(s) shown");
                    }
                }
            });

        }, 10, 10, TimeUnit.SECONDS);
    }

    private List<Room> fetchRoomsForCurrentFilter() {
        if (currentBudgetFilter != null)
            return guestService.viewAvailableRoomsWithinBudget(currentBudgetFilter);
        if (currentRoomTypeFilter != null)
            return guestService.viewAvailableRoomsByType(currentRoomTypeFilter);
        return guestService.viewAvailableRooms();
    }

    public void stopRefresh() {
        if (roomRefreshScheduler != null && !roomRefreshScheduler.isShutdown())
            roomRefreshScheduler.shutdown();
    }


    @FXML private void showRooms()           { setActive(btnRooms);    buildRoomsPane(null, null); }
    @FXML private void showMakeReservation() { setActive(btnReserve);  buildMakeReservationPane(); }
    @FXML private void showMyReservations()  { setActive(btnMyRes);    buildMyReservationsPaneAsync(); }
    @FXML private void showPayInvoice()      { setActive(btnPay);      buildPayInvoicePane(); }
    @FXML private void showInvoices()        { setActive(btnInvoices); buildInvoicesPaneAsync(); }
    @FXML private void showDeposit()         { setActive(btnDeposit);  buildDepositPane(); }
    @FXML private void showProfile()         { setActive(btnProfile);  buildProfilePane(); }

    @FXML private void showChat() {
        setActive(btnChat);
        try {
            ChatClient.getInstance().connect(currentGuest.getUsername());
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/hotel/gui/fxml/ChatWindow.fxml"));
            VBox chatPane = loader.load();
            hotel.gui.controllers.ChatController controller = loader.getController();
            controller.initializeChat(false, currentGuest.getUsername());
            setContent(chatPane);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            setContent(new Label("Failed to load chat: " + e.getMessage()));
        }
    }

    @FXML
    private void handleLogout() {
        stopRefresh();
        ChatClient.getInstance().disconnect();
        SceneManager.getInstance().showLogin();
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnRooms, btnReserve, btnMyRes, btnPay,
                btnInvoices, btnDeposit, btnProfile, btnChat})
            b.getStyleClass().remove("active");
        if (active != null) active.getStyleClass().add("active");
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

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

    private void buildRoomsPane(String filterType, Double filterBudget) {
        currentRoomTypeFilter = filterType;
        currentBudgetFilter   = filterBudget;

        VBox pane = new VBox(16);
        pane.setId("roomsPane");
        pane.setPadding(new Insets(0));

        Label title = new Label("Browse Available Rooms");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("Filter by:");
        filterLabel.setStyle("-fx-font-weight:bold;");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");
        for (RoomType rt : SceneManager.getInstance().roomService.getAllRoomTypes())
            typeFilter.getItems().add(rt.getTypeName());
        typeFilter.setValue(filterType != null ? filterType : "All Types");

        TextField budgetField = new TextField(filterBudget != null ? String.valueOf(filterBudget) : "");
        budgetField.setPromptText("Max price/night");
        budgetField.setPrefWidth(130);

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("btn-primary");
        applyBtn.setStyle("-fx-padding:6 16 6 16;");
        applyBtn.setOnAction(e -> {
            String sel = typeFilter.getValue();
            Double budget = null;
            if (!budgetField.getText().trim().isEmpty()) {
                try { budget = Double.parseDouble(budgetField.getText().trim()); }
                catch (NumberFormatException ex) { AlertHelper.showError("Invalid Input", "Budget must be a number."); return; }
            }
            buildRoomsPane("All Types".equals(sel) ? null : sel, budget);
        });

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setStyle("-fx-padding:6 12 6 12;");
        clearBtn.setOnAction(e -> buildRoomsPane(null, null));

        filters.getChildren().addAll(filterLabel, typeFilter, budgetField, applyBtn, clearBtn);

        roomRefreshStatusLabel = new Label("Auto-refreshes every 10 seconds");
        roomRefreshStatusLabel.setStyle(
                "-fx-text-fill:#94A3B8; -fx-font-size:11px; -fx-font-style:italic;");

        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        List<Room> rooms = fetchRoomsForCurrentFilter();
        if (rooms.isEmpty()) {
            Label empty = new Label("No rooms available with the current filter.");
            empty.setStyle("-fx-text-fill:#94A3B8; -fx-font-size:13px;");
            card.getChildren().add(empty);
            liveRoomTable = null;
        } else {
            liveRoomTable = buildRoomTable(rooms);
            card.getChildren().add(liveRoomTable);
        }

        pane.getChildren().addAll(title, filters, roomRefreshStatusLabel, card);
        setContent(pane);
    }

    private TableView<Room> buildRoomTable(List<Room> rooms) {
        TableView<Room> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);

        TableColumn<Room, String> colId    = col("Room ID",    r -> r.getRoomId());
        TableColumn<Room, String> colFloor = col("Floor",      r -> String.valueOf(r.getFloorNumber()));
        TableColumn<Room, String> colType  = col("Type",       r -> r.getRoomType().getTypeName());
        TableColumn<Room, String> colAvail = col("Available",  r -> r.isAvailable() ? "Yes" : "No");
        TableColumn<Room, String> colPrice = col("Price/Night",r -> String.format("EGP %.0f", r.getTotalPricePerNight()));
        TableColumn<Room, String> colAmen  = col("Amenities",  r -> {
            if (r.getAmenities().isEmpty()) return "—";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < r.getAmenities().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(r.getAmenities().get(i).getName());
            }
            return sb.toString();
        });

        colId.setPrefWidth(80); colFloor.setPrefWidth(60); colType.setPrefWidth(100);
        colAvail.setPrefWidth(80); colPrice.setPrefWidth(110); colAmen.setPrefWidth(300);
        table.getColumns().addAll(colId, colFloor, colType, colAvail, colPrice, colAmen);
        table.setItems(FXCollections.observableArrayList(rooms));
        return table;
    }

    private void buildMakeReservationPane() {
        VBox pane = new VBox(16);
        Label title = new Label("Make a Reservation");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        HBox dateRow = new HBox(20);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        VBox checkInBox  = new VBox(5, styledLabel("Check-In Date"));
        VBox checkOutBox = new VBox(5, styledLabel("Check-Out Date"));
        DatePicker checkInPicker  = new DatePicker();
        DatePicker checkOutPicker = new DatePicker();
        checkInBox.getChildren().add(checkInPicker);
        checkOutBox.getChildren().add(checkOutPicker);
        dateRow.getChildren().addAll(checkInBox, checkOutBox);

        Label msgLabel = new Label("");
        msgLabel.setWrapText(true);

        VBox roomsSection = new VBox(8);
        Label roomsLabel = new Label("Available Rooms for Selected Dates");
        roomsLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#1D4ED8;");
        TableView<Room> roomTable = new TableView<>();
        roomTable.getStyleClass().add("table-view");
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomTable.setPrefHeight(200);
        setupRoomColumns(roomTable);
        roomsSection.getChildren().addAll(roomsLabel, roomTable);
        roomsSection.setVisible(false);

        Button searchBtn = new Button("Search Available Rooms");
        searchBtn.getStyleClass().add("btn-primary");
        searchBtn.setOnAction(e -> {
            msgLabel.setText("");
            LocalDate ci = checkInPicker.getValue(), co = checkOutPicker.getValue();
            if (ci == null || co == null) { msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText("Please select both dates."); return; }
            if (!ci.isBefore(co)) { msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText("Check-out must be after check-in."); return; }
            if (ci.isBefore(LocalDate.now())) { msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText("Check-in cannot be in the past."); return; }

            List<Room> available = guestService.viewAvailableRooms();
            ObservableList<Room> filtered = FXCollections.observableArrayList();
            for (Room r : available) {
                boolean overlap = false;
                for (Reservation res : HotelDatabase.reservations) {
                    if (res.getRoomId().equals(r.getRoomId())
                            && res.getStatus() != ReservationStatus.CANCELLED
                            && res.getStatus() != ReservationStatus.CHECKED_OUT
                            && ci.isBefore(res.getCheckOutDate()) && co.isAfter(res.getCheckInDate())) {
                        overlap = true; break;
                    }
                }
                if (!overlap) filtered.add(r);
            }
            roomTable.setItems(filtered);
            roomsSection.setVisible(true);
            if (filtered.isEmpty()) { msgLabel.setStyle("-fx-text-fill:#B45309;"); msgLabel.setText("No rooms available for these dates."); }
        });

        TextField roomIdField = new TextField();
        roomIdField.setPromptText("Select a room from the table above");
        roomIdField.setPrefWidth(220);
        roomIdField.setEditable(false);

        VBox amenitiesSection = new VBox(10);
        amenitiesSection.setStyle("-fx-background-color:#F0F7FF; -fx-background-radius:8; -fx-padding:12;");
        amenitiesSection.setVisible(false);
        amenitiesSection.setManaged(false);

        Label amenitiesSectionTitle = new Label("Extra Amenities (Optional)");
        amenitiesSectionTitle.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:#1D4ED8;");

        Label fixedAmenitiesInfo = new Label("");
        fixedAmenitiesInfo.setWrapText(true);
        fixedAmenitiesInfo.setStyle("-fx-text-fill:#475569; -fx-font-size:12px;");

        Label extraAmenitiesLabel = new Label("Add extra amenities to your stay:");
        extraAmenitiesLabel.setStyle("-fx-font-weight:bold; -fx-font-size:12px;");

        VBox dropdownBox = new VBox(6);

        Label costPreviewLabel = new Label("");
        costPreviewLabel.setStyle("-fx-text-fill:#0369A1; -fx-font-weight:bold; -fx-font-size:13px;");

        amenitiesSection.getChildren().addAll(
                amenitiesSectionTitle,
                fixedAmenitiesInfo,
                new Separator(),
                extraAmenitiesLabel,
                dropdownBox,
                costPreviewLabel
        );

        java.util.List<CheckBox> amenityCheckboxes = new java.util.ArrayList<>();

        roomTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selectedRoom) -> {
            if (selectedRoom == null) return;
            roomIdField.setText(selectedRoom.getRoomId());

            LocalDate ci = checkInPicker.getValue(), co = checkOutPicker.getValue();
            long nights = (ci != null && co != null && ci.isBefore(co))
                    ? java.time.temporal.ChronoUnit.DAYS.between(ci, co) : 1;

            List<Amenity> fixed = selectedRoom.getAmenities();
            if (!fixed.isEmpty()) {
                StringBuilder sb = new StringBuilder("🏨 Included with this room: ");
                for (int i = 0; i < fixed.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(fixed.get(i).getName());
                }
                fixedAmenitiesInfo.setText(sb.toString());
            } else {
                fixedAmenitiesInfo.setText("This room has no fixed amenities included.");
            }

            dropdownBox.getChildren().clear();
            amenityCheckboxes.clear();

            java.util.Set<String> fixedIds = new java.util.HashSet<>();
            for (Amenity a : fixed) fixedIds.add(a.getAmenityId());

            for (Amenity a : HotelDatabase.amenities) {
                if (fixedIds.contains(a.getAmenityId())) continue; // already included — skip

                String labelText = a.getName()
                        + " — " + a.getDescription()
                        + (a.getExtraCostPerNight() > 0
                        ? "  (+EGP " + String.format("%.0f", a.getExtraCostPerNight()) + "/night)"
                        : "  (Free)");
                CheckBox cb = new CheckBox(labelText);
                cb.setUserData(a);
                cb.setStyle("-fx-font-size:12.5px;");

                final long nightsFinal = nights;
                final double baseTotal = selectedRoom.getTotalPricePerNight() * nights;
                cb.setOnAction(ev -> {
                    double extra = 0;
                    for (CheckBox c : amenityCheckboxes) {
                        if (c.isSelected()) {
                            extra += ((Amenity) c.getUserData()).getExtraCostPerNight() * nightsFinal;
                        }
                    }
                    costPreviewLabel.setText(extra > 0
                            ? "💰 Estimated total: EGP " + String.format("%.2f", baseTotal + extra)
                            + "  (base EGP " + String.format("%.2f", baseTotal)
                            + " + extras EGP " + String.format("%.2f", extra) + ")"
                            : "💰 Estimated total: EGP " + String.format("%.2f", baseTotal) + "  (no extras selected)");
                });

                amenityCheckboxes.add(cb);
                dropdownBox.getChildren().add(cb);
            }

            if (amenityCheckboxes.isEmpty()) {
                dropdownBox.getChildren().add(new Label("All amenities are already included in this room."));
            }

            costPreviewLabel.setText("💰 Estimated total: EGP "
                    + String.format("%.2f", selectedRoom.getTotalPricePerNight() * nights)
                    + "  (no extras selected)");

            amenitiesSection.setVisible(true);
            amenitiesSection.setManaged(true);
        });

        Button confirmBtn = new Button("Confirm Reservation");
        confirmBtn.getStyleClass().add("btn-success");

        HBox confirmRow = new HBox(12);
        confirmRow.setAlignment(Pos.CENTER_LEFT);
        confirmRow.getChildren().addAll(new Label("Selected Room:"), roomIdField, confirmBtn);

        confirmBtn.setOnAction(e -> {
            String roomId = roomIdField.getText().trim();
            LocalDate ci = checkInPicker.getValue(), co = checkOutPicker.getValue();
            if (roomId.isEmpty() || ci == null || co == null) {
                msgLabel.setStyle("-fx-text-fill:#DC2626;");
                msgLabel.setText("Please select dates and a room from the table.");
                return;
            }
            try {
                Reservation res = guestService.makeReservation(currentGuest, roomId, ci, co);

                Room selectedRoom = HotelDatabase.findRoomById(roomId);
                int addedCount = 0;
                for (CheckBox cb : amenityCheckboxes) {
                    if (cb.isSelected()) {
                        Amenity a = (Amenity) cb.getUserData();
                        res.addExtraAmenity(a, selectedRoom != null ? selectedRoom.getAmenities() : new java.util.ArrayList<>(), res.getNumberOfNights());
                        invoiceService.updateInvoiceAmount(res.getReservationId(), res.getTotalCost());
                        addedCount++;
                    }
                }

                String extraInfo = addedCount > 0 ? "  |  Extra amenities: " + addedCount : "";
                msgLabel.setStyle("-fx-text-fill:#15803D;");
                msgLabel.setText("✅ Reservation confirmed!  ID: " + res.getReservationId()
                        + "  |  Total: EGP " + String.format("%.2f", res.getTotalCost())
                        + "  |  Nights: " + res.getNumberOfNights()
                        + extraInfo);

                checkInPicker.setValue(null);
                checkOutPicker.setValue(null);
                roomIdField.clear();
                roomTable.getItems().clear();
                roomsSection.setVisible(false);
                amenitiesSection.setVisible(false);
                amenitiesSection.setManaged(false);
                dropdownBox.getChildren().clear();
                amenityCheckboxes.clear();
                costPreviewLabel.setText("");
            } catch (Exception ex) {
                msgLabel.setStyle("-fx-text-fill:#DC2626;");
                msgLabel.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(dateRow, searchBtn, msgLabel, roomsSection, confirmRow, amenitiesSection);
        pane.getChildren().addAll(title, card);
        setContent(pane);
    }

    private void buildMyReservationsPaneAsync() {
        VBox pane = new VBox(16);
        Label title = new Label("My Reservations");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        pane.getChildren().addAll(title, makeLoadingPane("Loading your reservations…"));
        setContent(pane);   // show spinner immediately

        Task<List<Reservation>> loadTask = new Task<>() {
            @Override
            protected List<Reservation> call() {
                return guestService.viewMyReservations(currentGuest);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Reservation> reservations = loadTask.getValue();
            VBox card = new VBox(12);
            card.getStyleClass().add("card");

            if (reservations.isEmpty()) {
                Label empty = new Label("You have no reservations yet.");
                empty.setStyle("-fx-text-fill:#94A3B8;");
                card.getChildren().add(empty);
            } else {
                TableView<Reservation> table = buildReservationTable(reservations);
                Label actionLabel = new Label("");
                Button cancelBtn = new Button("Cancel Selected Reservation");
                cancelBtn.getStyleClass().add("btn-danger");
                cancelBtn.setOnAction(e -> {
                    Reservation sel = table.getSelectionModel().getSelectedItem();
                    if (sel == null) { actionLabel.setStyle("-fx-text-fill:#DC2626;"); actionLabel.setText("Select a reservation first."); return; }
                    if (!AlertHelper.showConfirm("Cancel Reservation", "Cancel reservation " + sel.getReservationId() + "?")) return;
                    try {
                        guestService.cancelReservation(currentGuest, sel.getReservationId());
                        actionLabel.setStyle("-fx-text-fill:#15803D;");
                        actionLabel.setText("Reservation cancelled successfully.");
                        buildMyReservationsPaneAsync(); // re-trigger async load
                    } catch (Exception ex) {
                        actionLabel.setStyle("-fx-text-fill:#DC2626;"); actionLabel.setText(ex.getMessage());
                    }
                });
                card.getChildren().addAll(table, actionLabel, cancelBtn);
            }

            pane.getChildren().setAll(title, card); // swap spinner for real content
        });

        loadTask.setOnFailed(event -> {
            Label err = new Label("Failed to load reservations: " + loadTask.getException().getMessage());
            err.setStyle("-fx-text-fill:#DC2626;");
            pane.getChildren().setAll(title, err);
        });

        Thread t = new Thread(loadTask, "reservations-load-thread");
        t.setDaemon(true);
        t.start();
    }

    private void buildPayInvoicePane() {
        VBox pane = new VBox(16);
        Label title = new Label("Pay Invoice");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        List<Invoice> myInvoices = guestService.viewMyInvoices(currentGuest);
        ObservableList<Invoice> unpaid = FXCollections.observableArrayList();
        for (Invoice inv : myInvoices) if (!inv.isPaid()) unpaid.add(inv);

        if (unpaid.isEmpty()) {
            card.getChildren().add(new Label("No unpaid invoices."));
            pane.getChildren().addAll(title, card);
            setContent(pane);
            return;
        }

        TableView<Invoice> invoiceTable = buildInvoiceTable(unpaid);

        Label selMsg = new Label("Select an invoice above, then choose payment method:");
        selMsg.setStyle("-fx-font-weight:bold; -fx-padding:10 0 4 0;");

        ToggleGroup tg = new ToggleGroup();
        RadioButton rbBalance = new RadioButton("My Balance  (EGP " + String.format("%.2f", currentGuest.getBalance()) + ")");
        RadioButton rbCash    = new RadioButton("Cash");
        RadioButton rbCard    = new RadioButton("Credit Card");
        RadioButton rbDebit   = new RadioButton("Debit Card");
        rbBalance.setToggleGroup(tg); rbBalance.setSelected(true);
        rbCash.setToggleGroup(tg); rbCard.setToggleGroup(tg); rbDebit.setToggleGroup(tg);

        HBox radioRow = new HBox(20, rbBalance, rbCash, rbCard, rbDebit);
        radioRow.setAlignment(Pos.CENTER_LEFT);

        Label resultLabel = new Label("");
        resultLabel.setWrapText(true);

        Button payBtn = new Button("Pay Now");
        payBtn.getStyleClass().add("btn-success");
        payBtn.setOnAction(e -> {
            Invoice sel = invoiceTable.getSelectionModel().getSelectedItem();
            if (sel == null) { resultLabel.setStyle("-fx-text-fill:#DC2626;"); resultLabel.setText("Please select an invoice to pay."); return; }
            try {
                Invoice paid;
                if (rbBalance.isSelected()) {
                    paid = guestService.checkoutWithBalance(currentGuest, sel.getReservationId());
                } else {
                    PaymentMethod pm = rbCash.isSelected() ? PaymentMethod.CASH
                            : rbCard.isSelected() ? PaymentMethod.CREDIT_CARD : PaymentMethod.DEBIT_CARD;
                    paid = guestService.checkoutWithExternalPayment(sel.getReservationId(), sel.getTotalAmountDue(), pm);
                }
                resultLabel.setStyle("-fx-text-fill:#15803D;");
                resultLabel.setText("Payment successful!\n" + paid.getFormattedInvoice());
                buildPayInvoicePane();
            } catch (Exception ex) {
                resultLabel.setStyle("-fx-text-fill:#DC2626;"); resultLabel.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(invoiceTable, selMsg, radioRow, payBtn, resultLabel);
        pane.getChildren().addAll(title, card);
        setContent(pane);
    }

    private void buildInvoicesPaneAsync() {
        VBox pane = new VBox(16);
        Label title = new Label("My Invoices");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        pane.getChildren().addAll(title, makeLoadingPane("Loading your invoices…"));
        setContent(pane);

        Task<List<Invoice>> loadTask = new Task<>() {
            @Override
            protected List<Invoice> call() {
                return guestService.viewMyInvoices(currentGuest);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Invoice> invoices = loadTask.getValue();
            VBox card = new VBox(12);
            card.getStyleClass().add("card");

            if (invoices.isEmpty()) {
                card.getChildren().add(new Label("No invoices found."));
            } else {
                card.getChildren().add(
                        buildInvoiceTable(FXCollections.observableArrayList(invoices)));
            }

            pane.getChildren().setAll(title, card);
        });

        loadTask.setOnFailed(event -> {
            Label err = new Label("Failed to load invoices: " + loadTask.getException().getMessage());
            err.setStyle("-fx-text-fill:#DC2626;");
            pane.getChildren().setAll(title, err);
        });

        Thread t = new Thread(loadTask, "invoices-load-thread");
        t.setDaemon(true);
        t.start();
    }

    private void buildDepositPane() {
        VBox pane = new VBox(16);
        Label title = new Label("Deposit Balance");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setMaxWidth(400);

        Label balLabel = new Label("Current Balance: EGP " + String.format("%.2f", currentGuest.getBalance()));
        balLabel.getStyleClass().add("balance-label");

        TextField amtField = new TextField();
        amtField.setPromptText("e.g. 500");
        Label msgLabel = new Label("");

        Button depositBtn = new Button("Deposit");
        depositBtn.getStyleClass().add("btn-success");
        depositBtn.setOnAction(e -> {
            msgLabel.setText("");
            try {
                double amount = Double.parseDouble(amtField.getText().trim());
                guestService.depositBalance(currentGuest, amount);
                balLabel.setText("Current Balance: EGP " + String.format("%.2f", currentGuest.getBalance()));
                msgLabel.setStyle("-fx-text-fill:#15803D;");
                msgLabel.setText("EGP " + String.format("%.2f", amount) + " deposited successfully.");
                amtField.clear();
            } catch (NumberFormatException ex) {
                msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText("Please enter a valid amount.");
            } catch (Exception ex) {
                msgLabel.setStyle("-fx-text-fill:#DC2626;"); msgLabel.setText(ex.getMessage());
            }
        });

        card.getChildren().addAll(balLabel, styledLabel("Amount to Deposit (EGP):"), amtField, depositBtn, msgLabel);
        pane.getChildren().addAll(title, card);
        setContent(pane);
    }

    private void buildProfilePane() {
        VBox pane = new VBox(16);
        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F2044;");

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setMaxWidth(480);
        card.getChildren().addAll(
                profileRow("Guest ID",      currentGuest.getGuestId()),
                profileRow("Username",      currentGuest.getUsername()),
                profileRow("Gender",        currentGuest.getGender().toString()),
                profileRow("Date of Birth", currentGuest.getDateOfBirth().toString()),
                profileRow("Address",       currentGuest.getAddress()),
                profileRow("Balance",       "EGP " + String.format("%.2f", currentGuest.getBalance()))
        );

        Label prefTitle = new Label("Update Room Preferences");
        prefTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1D4ED8; -fx-padding:10 0 0 0;");

        VBox prefCard = new VBox(10);
        prefCard.getStyleClass().add("card");
        prefCard.setMaxWidth(480);

        HBox prefRow1 = new HBox(20);
        prefRow1.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().add("No Preference");
        for (RoomType rt : SceneManager.getInstance().roomService.getAllRoomTypes())
            typeCombo.getItems().add(rt.getTypeName());
        typeCombo.setValue("No Preference");

        TextField floorField = new TextField();
        floorField.setPromptText("Preferred Floor (blank = none)");
        floorField.setPrefWidth(180);
        prefRow1.getChildren().addAll(new Label("Type:"), typeCombo, new Label("Floor:"), floorField);

        HBox prefRow2 = new HBox(20);
        prefRow2.setAlignment(Pos.CENTER_LEFT);
        CheckBox smokingCb = new CheckBox("Smoking Room");
        CheckBox accessCb  = new CheckBox("Accessibility Required");
        prefRow2.getChildren().addAll(smokingCb, accessCb);

        Label prefMsg = new Label("");
        Button savePrefBtn = new Button("Save Preferences");
        savePrefBtn.getStyleClass().add("btn-primary");
        savePrefBtn.setOnAction(e -> {
            String type = "No Preference".equals(typeCombo.getValue()) ? null : typeCombo.getValue();
            Integer floor = null;
            if (!floorField.getText().trim().isEmpty()) {
                try { floor = Integer.parseInt(floorField.getText().trim()); }
                catch (NumberFormatException ex) { prefMsg.setStyle("-fx-text-fill:#DC2626;"); prefMsg.setText("Floor must be a number."); return; }
            }
            RoomPreference pref = new RoomPreference(type, floor != null ? floor : 0, smokingCb.isSelected(), accessCb.isSelected());
            guestService.updateRoomPreferences(currentGuest, pref);
            prefMsg.setStyle("-fx-text-fill:#15803D;"); prefMsg.setText("Preferences saved.");
        });

        prefCard.getChildren().addAll(prefRow1, prefRow2, savePrefBtn, prefMsg);
        pane.getChildren().addAll(title, card, prefTitle, prefCard);
        setContent(pane);
    }

    private HBox profileRow(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":"); lbl.setStyle("-fx-font-weight:bold; -fx-min-width:120px;");
        row.getChildren().addAll(lbl, new Label(value));
        return row;
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight:bold;");
        return l;
    }

    private TableView<Reservation> buildReservationTable(List<Reservation> reservations) {
        TableView<Reservation> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(280);
        table.getColumns().addAll(
                col("Res ID",      r -> r.getReservationId()),
                col("Room",        r -> r.getRoomId()),
                col("Check-In",    r -> r.getCheckInDate().toString()),
                col("Check-Out",   r -> r.getCheckOutDate().toString()),
                col("Nights",      r -> String.valueOf(r.getNumberOfNights())),
                col("Cost (EGP)",  r -> String.format("%.2f", r.getTotalCost())),
                col("Status",      r -> r.getStatus().toString()),
                col("Extra Amenities", r -> {
                    if (r.getExtraAmenities().isEmpty()) return "—";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < r.getExtraAmenities().size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(r.getExtraAmenities().get(i).getName());
                    }
                    return sb.toString();
                })
        );
        table.setItems(FXCollections.observableArrayList(reservations));
        return table;
    }

    private TableView<Invoice> buildInvoiceTable(ObservableList<Invoice> invoices) {
        TableView<Invoice> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(220);
        table.getColumns().addAll(
                col("Invoice ID", i -> i.getInvoiceId()),
                col("Res ID",     i -> i.getReservationId()),
                col("Amount Due", i -> String.format("EGP %.2f", i.getTotalAmountDue())),
                col("Status",     i -> i.isPaid() ? "PAID" : "UNPAID"),
                col("Method",     i -> i.getPaymentMethod() != null ? i.getPaymentMethod().toString() : "—"),
                col("Issued",     i -> i.getIssuedDate().toString())
        );
        table.setItems(invoices);
        return table;
    }

    private void setupRoomColumns(TableView<Room> table) {
        table.getColumns().addAll(
                col("Room ID",    r -> r.getRoomId()),
                col("Floor",      r -> String.valueOf(r.getFloorNumber())),
                col("Type",       r -> r.getRoomType().getTypeName()),
                col("Price/Night",r -> String.format("EGP %.0f", r.getTotalPricePerNight())),
                col("Amenities",  r -> {
                    if (r.getAmenities().isEmpty()) return "—";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < r.getAmenities().size(); i++) { if (i > 0) sb.append(", "); sb.append(r.getAmenities().get(i).getName()); }
                    return sb.toString();
                })
        );
    }

    private <T> TableColumn<T, String> col(String header, java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> c = new TableColumn<>(header);
        c.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return c;
    }
}
