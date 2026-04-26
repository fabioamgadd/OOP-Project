package hotel.database;

import hotel.enums.Gender;
import hotel.enums.PaymentMethod;
import hotel.enums.ReservationStatus;
import hotel.models.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class HotelDatabase {

    public static ArrayList<Guest> guests= new ArrayList<>();
    public static ArrayList<Staff> staffMembers = new ArrayList<>();
    public static ArrayList<RoomType> roomTypes = new ArrayList<>();
    public static ArrayList<Amenity> amenities = new ArrayList<>();
    public static ArrayList<Room> rooms = new ArrayList<>();
    public static ArrayList<Reservation> reservations = new ArrayList<>();
    public static ArrayList<Invoice> invoices = new ArrayList<>();

    static {
        seedAmenities();
        seedRoomTypes();
        seedRooms();
        seedStaff();
        seedGuests();
        seedReservationsAndInvoices();
    }

    private HotelDatabase(){
        //private to prevent making object from it
    }

    private static void seedAmenities() {
        amenities.add(new Amenity("a001", "WiFi",         "High-speed wireless internet",      10.0));
        amenities.add(new Amenity("a002", "TV",           "55\" Smart TV with cable channels",  5.0));
        amenities.add(new Amenity("a003", "Mini-bar",     "Stocked mini-bar (pay-per-use)",    20.0));
        amenities.add(new Amenity("a004", "Air Conditioning", "Climate-controlled room",        0.0));
        amenities.add(new Amenity("a005", "Jacuzzi",      "Private in-room jacuzzi",           50.0));
        amenities.add(new Amenity("a006", "Breakfast",    "Daily continental breakfast",       30.0));
        amenities.add(new Amenity("a007", "Safe",         "In-room electronic safe",            0.0));
        amenities.add(new Amenity("a008", "Gym Access",   "Full access to the hotel gym",      15.0));
    }

    private static void seedRoomTypes() {
        roomTypes.add(new RoomType("rt001", "Single",  "Cozy room for one guest",              500.0,  1));
        roomTypes.add(new RoomType("rt002", "Double",  "Comfortable room for two guests",      800.0,  2));
        roomTypes.add(new RoomType("rt003", "Suite",   "Luxurious suite with panoramic view", 2000.0,  4));
        roomTypes.add(new RoomType("rt004", "Deluxe",  "Upscale room with premium finishes",  1200.0,  2));
        roomTypes.add(new RoomType("rt005", "Family",  "Spacious room for families",          1500.0,  6));
    }

    private static void seedRooms() {
        RoomType single  = findRoomTypeById("rt001");
        RoomType doubleT = findRoomTypeById("rt002");
        RoomType suite   = findRoomTypeById("rt003");
        RoomType deluxe  = findRoomTypeById("rt004");
        RoomType family  = findRoomTypeById("rt005");

        Amenity wifi   = findAmenityById("a001");
        Amenity tv     = findAmenityById("a002");
        Amenity ac     = findAmenityById("a004");
        Amenity minibar= findAmenityById("a003");
        Amenity jacuzzi= findAmenityById("a005");
        Amenity brkfst = findAmenityById("a006");
        Amenity safe   = findAmenityById("a007");
        Amenity gym    = findAmenityById("a008");

        //Floor 1 for single rooms
        Room r101 = new Room("101", 1, single);  r101.addAmenity(wifi); r101.addAmenity(tv); r101.addAmenity(ac);
        Room r102 = new Room("102", 1, single);  r102.addAmenity(wifi); r102.addAmenity(ac);
        Room r103 = new Room("103", 1, single);  r103.addAmenity(wifi); r103.addAmenity(tv);

        // Floor 2 for double rooms
        Room r201 = new Room("201", 2, doubleT); r201.addAmenity(wifi); r201.addAmenity(tv); r201.addAmenity(ac); r201.addAmenity(safe);
        Room r202 = new Room("202", 2, doubleT); r202.addAmenity(wifi); r202.addAmenity(tv); r202.addAmenity(brkfst);
        Room r203 = new Room("203", 2, doubleT); r203.addAmenity(wifi); r203.addAmenity(ac);

        // Floor 3 for deluxe rooms
        Room r301 = new Room("301", 3, deluxe);  r301.addAmenity(wifi); r301.addAmenity(tv); r301.addAmenity(minibar); r301.addAmenity(safe);
        Room r302 = new Room("302", 3, deluxe);  r302.addAmenity(wifi); r302.addAmenity(brkfst); r302.addAmenity(gym);

        // Floor 4 for family rooms
        Room r401 = new Room("401", 4, family);  r401.addAmenity(wifi); r401.addAmenity(tv); r401.addAmenity(ac); r401.addAmenity(brkfst);
        Room r402 = new Room("402", 4, family);  r402.addAmenity(wifi); r402.addAmenity(ac);

        // Floor 5 for suites
        Room r501 = new Room("501", 5, suite);   r501.addAmenity(wifi); r501.addAmenity(tv); r501.addAmenity(minibar); r501.addAmenity(jacuzzi); r501.addAmenity(brkfst); r501.addAmenity(gym);
        Room r502 = new Room("502", 5, suite);   r502.addAmenity(wifi); r502.addAmenity(tv); r502.addAmenity(jacuzzi); r502.addAmenity(safe);

        rooms.add(r101); rooms.add(r102); rooms.add(r103);
        rooms.add(r201); rooms.add(r202); rooms.add(r203);
        rooms.add(r301); rooms.add(r302);
        rooms.add(r401); rooms.add(r402);
        rooms.add(r501); rooms.add(r502);
    }

    private static void seedStaff() {
        // Admin
        staffMembers.add(new Admin(
                "staff-admin-001", "admin", "Admin@123",
                LocalDate.of(1985, 3, 10), 40, Gender.MALE));

        // Receptionist 1
        staffMembers.add(new Receptionist(
                "staff-recep-001", "pierre","Recep@123",
                LocalDate.of(2007, 6, 15), 40, Gender.MALE));

        // Receptionist 2
        staffMembers.add(new Receptionist(
                "staff-recep-002", "fabio", "Recep@456",
                LocalDate.of(2006, 6, 11), 40, Gender.MALE));
    }

    private static void seedGuests() {
        // Guest 1
        Guest g1 = new Guest(
                "guest-001", "habiba","Guest@123",
                LocalDate.of(2007, 2, 7), 3000.0,
                "15 Nile St, Cairo", Gender.FEMALE,
                new RoomPreference("Double", 2, false, false));
        guests.add(g1);

        // Guest 2
        Guest g2 = new Guest(
                "guest-002", "mennat-allah", "Guest@456",
                LocalDate.of(2006, 10, 28), 7500.0,
                "88 Zamalek Ave, Cairo", Gender.FEMALE,
                new RoomPreference("Suite", 5, false, false));
        guests.add(g2);

        // Guest 3
        Guest g3 = new Guest(
                "guest-003", "selena","Guest@789",
                LocalDate.of(2008, 4, 12), 1200.0,
                "3 Maadi St, Cairo", Gender.FEMALE,
                new RoomPreference("Single", 1, false, false));
        guests.add(g3);
    }

    private static void seedReservationsAndInvoices() {
        Room room201 = findRoomById("201");
        if (room201 == null) return;

        LocalDate checkIn  = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(5);
        double cost = Reservation.calculateTotalCost(room201, checkIn, checkOut);

        Reservation res = new Reservation(
                "res-seed-001", "guest-001", "201",
                checkIn, checkOut,
                ReservationStatus.CONFIRMED, cost, LocalDate.now());
        reservations.add(res);
        room201.setAvailable(false);

        Invoice inv = new Invoice("inv-seed-001", "res-seed-001", "guest-001",
                cost, 0.0, null, false, LocalDate.now(), null);
        invoices.add(inv);

        // One already paid invoice for guest-002
        Reservation res2 = new Reservation(
                "res-seed-002", "guest-002", "501",
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(7),
                ReservationStatus.CHECKED_OUT, 8100.0, LocalDate.now().minusDays(10));
        reservations.add(res2);

        Invoice inv2 = new Invoice("inv-seed-002", "res-seed-002", "guest-002",
                8100.0, 8100.0, PaymentMethod.CREDIT_CARD, true,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(7));
        invoices.add(inv2);
    }


    public static RoomType findRoomTypeById(String id) {
        for (int i = 0; i < roomTypes.size(); i++) {
            RoomType rt = roomTypes.get(i);
            if (rt.getTypeId().equals(id)) {
                return rt;
            }
        }
        return null;
    }
    public static Amenity findAmenityById(String id) {
        for (int i = 0; i < amenities.size(); i++) {
            Amenity a = amenities.get(i);
            if (a.getAmenityId().equals(id)) {
                return a;
            }
        }
        return null;
    }
    public static Room findRoomById(String id) {
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            if (r.getRoomId().equals(id)) {
                return r;
            }
        }
        return null;
    }
}
