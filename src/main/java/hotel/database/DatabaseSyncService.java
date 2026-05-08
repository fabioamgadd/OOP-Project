package hotel.database;

import hotel.enums.ReservationStatus;
import hotel.models.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;

public class DatabaseSyncService {

    private static final String DB_URL = "jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:5432/postgres?user=postgres.cedjgqcahstcktwfahgu&password=tutpes-2fofzy-Godbat";
    
    private static final Map<String, ReservationStatus> syncedState = new ConcurrentHashMap<>();
    private static ScheduledExecutorService scheduler;
    private static boolean isRunning = false;

    public static synchronized void startSyncTask() {
        if (isRunning) return;
        isRunning = true;
        
        new Thread(() -> {
            initializeDatabase();

            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "database-sync-thread");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(() -> {
                try (Connection conn = DriverManager.getConnection(DB_URL)) {
                    syncGuests(conn);
                    syncReservations(conn);
                } catch (Exception e) {
                    System.err.println("Database sync failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 0, 5, TimeUnit.SECONDS);
        }).start();
    }

    public static void forceSync() {
        if (!isRunning) return;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            syncGuests(conn);
            syncReservations(conn);
        } catch (Exception e) {
            System.err.println("Final DB sync failed: " + e.getMessage());
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS reservations_sync (" +
                    "reservation_id VARCHAR(50) PRIMARY KEY, " +
                    "guest_id VARCHAR(50), " +
                    "room_id VARCHAR(50), " +
                    "check_in_date DATE, " +
                    "check_out_date DATE, " +
                    "status VARCHAR(20), " +
                    "total_cost DOUBLE PRECISION, " +
                    "created_at DATE" +
                    ")";
            stmt.execute(createTableSQL);
            System.out.println("Supabase table 'reservations_sync' is ready.");
            
            String createGuestsTableSQL = "CREATE TABLE IF NOT EXISTS guests_sync (" +
                    "guest_id VARCHAR(50) PRIMARY KEY, " +
                    "username VARCHAR(100), " +
                    "password VARCHAR(255), " +
                    "date_of_birth DATE, " +
                    "balance DOUBLE PRECISION, " +
                    "address VARCHAR(255), " +
                    "gender VARCHAR(20), " +
                    "pref_type VARCHAR(50), " +
                    "pref_floor INT, " +
                    "pref_smoking BOOLEAN, " +
                    "pref_access BOOLEAN" +
                    ")";
            stmt.execute(createGuestsTableSQL);
            System.out.println("Supabase table 'guests_sync' is ready.");
        } catch (SQLException e) {
            System.err.println("Could not initialize Supabase table: " + e.getMessage());
        }
    }

    private static void syncReservations(Connection conn) throws SQLException {
        String selectSQL = "SELECT * FROM reservations_sync";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            
            while (rs.next()) {
                String resId = rs.getString("reservation_id");
                String guestId = rs.getString("guest_id");
                String roomId = rs.getString("room_id");
                LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out_date").toLocalDate();
                ReservationStatus status = ReservationStatus.valueOf(rs.getString("status"));
                double cost = rs.getDouble("total_cost");
                LocalDate createdAt = rs.getDate("created_at").toLocalDate();

                Reservation.updateIdCounter(resId);

                Reservation localRes = findLocal(resId);
                if (localRes == null) {
                    Reservation newRes = new Reservation(resId, guestId, roomId, checkIn, checkOut, status, cost, createdAt);
                    HotelDatabase.reservations.add(newRes);
                    syncedState.put(resId, status);
                } else {
                    ReservationStatus lastSyncedStatus = syncedState.get(resId);
                    if (lastSyncedStatus != null && lastSyncedStatus != status) {
                        localRes.setStatus(status);
                        syncedState.put(resId, status);
                    } else if (lastSyncedStatus == null) {
                       if (localRes.getStatus() != status) {
                           localRes.setStatus(status);
                       }
                       syncedState.put(resId, localRes.getStatus());
                    }
                }
            }
        }

        String upsertSQL = "INSERT INTO reservations_sync (reservation_id, guest_id, room_id, check_in_date, check_out_date, status, total_cost, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (reservation_id) DO UPDATE SET status = EXCLUDED.status";
        
        try (PreparedStatement pstmt = conn.prepareStatement(upsertSQL)) {
            for (Reservation local : HotelDatabase.reservations) {
                String id = local.getReservationId();
                ReservationStatus currentStatus = local.getStatus();
                ReservationStatus lastSynced = syncedState.get(id);

                if (lastSynced == null || lastSynced != currentStatus) {
                    pstmt.setString(1, id);
                    pstmt.setString(2, local.getGuestId());
                    pstmt.setString(3, local.getRoomId());
                    pstmt.setDate(4, Date.valueOf(local.getCheckInDate()));
                    pstmt.setDate(5, Date.valueOf(local.getCheckOutDate()));
                    pstmt.setString(6, currentStatus.name());
                    pstmt.setDouble(7, local.getTotalCost());
                    pstmt.setDate(8, Date.valueOf(LocalDate.now())); 
                    pstmt.executeUpdate();
                    
                    syncedState.put(id, currentStatus);
                }
            }
        }
    }

    private static Reservation findLocal(String id) {
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getReservationId().equals(id)) return r;
        }
        return null;
    }

    private static final Map<String, Double> syncedGuestBalances = new ConcurrentHashMap<>();

    private static void syncGuests(Connection conn) throws SQLException {
        String selectSQL = "SELECT * FROM guests_sync";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            
            while (rs.next()) {
                String guestId = rs.getString("guest_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                LocalDate dob = rs.getDate("date_of_birth").toLocalDate();
                double balance = rs.getDouble("balance");
                String address = rs.getString("address");
                hotel.enums.Gender gender = hotel.enums.Gender.valueOf(rs.getString("gender"));
                
                String prefType = rs.getString("pref_type");
                int prefFloor = rs.getInt("pref_floor");
                boolean prefSmoking = rs.getBoolean("pref_smoking");
                boolean prefAccess = rs.getBoolean("pref_access");
                hotel.models.RoomPreference prefs = new hotel.models.RoomPreference(prefType, prefFloor, prefSmoking, prefAccess);

                hotel.models.Guest.updateIdCounter(guestId);

                hotel.models.Guest localGuest = findLocalGuest(guestId);
                if (localGuest == null) {
                    hotel.models.Guest newGuest = new hotel.models.Guest(guestId, username, password, dob, balance, address, gender, prefs);
                    HotelDatabase.guests.add(newGuest);
                    syncedGuestBalances.put(guestId, balance);
                } else {
                    Double lastSyncedBalance = syncedGuestBalances.get(guestId);
                    if (lastSyncedBalance != null && lastSyncedBalance != balance) {
                        double diff = balance - localGuest.getBalance();
                        if (diff > 0) localGuest.deposit(diff);
                        else if (diff < 0) localGuest.deduct(-diff);
                        
                        syncedGuestBalances.put(guestId, balance);
                    } else if (lastSyncedBalance == null && localGuest.getBalance() != balance) {
                        double diff = balance - localGuest.getBalance();
                        if (diff > 0) localGuest.deposit(diff);
                        else if (diff < 0) localGuest.deduct(-diff);
                        
                        syncedGuestBalances.put(guestId, localGuest.getBalance());
                    }
                }
            }
        }

        String upsertSQL = "INSERT INTO guests_sync (guest_id, username, password, date_of_birth, balance, address, gender, pref_type, pref_floor, pref_smoking, pref_access) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (guest_id) DO UPDATE SET balance = EXCLUDED.balance, address = EXCLUDED.address, pref_type = EXCLUDED.pref_type, pref_floor = EXCLUDED.pref_floor, pref_smoking = EXCLUDED.pref_smoking, pref_access = EXCLUDED.pref_access";
        
        try (PreparedStatement pstmt = conn.prepareStatement(upsertSQL)) {
            for (hotel.models.Guest local : HotelDatabase.guests) {
                Double lastSynced = syncedGuestBalances.get(local.getGuestId());
                if (lastSynced == null || lastSynced != local.getBalance()) {
                    pstmt.setString(1, local.getGuestId());
                    pstmt.setString(2, local.getUsername());
                    pstmt.setString(3, local.getPassword());
                    pstmt.setDate(4, Date.valueOf(local.getDateOfBirth()));
                    pstmt.setDouble(5, local.getBalance());
                    pstmt.setString(6, local.getAddress());
                    pstmt.setString(7, local.getGender().name());
                    
                    hotel.models.RoomPreference prefs = local.getRoomPreferences();
                    if (prefs != null) {
                        pstmt.setString(8, prefs.getPreferredRoomTypeName());
                        pstmt.setInt(9, prefs.getPreferredFloor());
                        pstmt.setBoolean(10, prefs.isSmokingRoom());
                        pstmt.setBoolean(11, prefs.isAccessibilityRequired());
                    } else {
                        pstmt.setNull(8, Types.VARCHAR);
                        pstmt.setInt(9, 0);
                        pstmt.setBoolean(10, false);
                        pstmt.setBoolean(11, false);
                    }
                    pstmt.executeUpdate();
                    
                    syncedGuestBalances.put(local.getGuestId(), local.getBalance());
                }
            }
        }
    }

    private static hotel.models.Guest findLocalGuest(String id) {
        for (hotel.models.Guest g : HotelDatabase.guests) {
            if (g.getGuestId().equals(id)) return g;
        }
        return null;
    }
}
