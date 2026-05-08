package hotel.services;
import hotel.database.HotelDatabase;
import hotel.models.Guest;
import hotel.models.Reservation;
import hotel.models.Room;
import java.util.List;

public class ReceptionistService {
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final GuestService guestService;

    public ReceptionistService(ReservationService reservationService, RoomService roomService, GuestService guestService) {
        this.reservationService = reservationService;
        this.roomService = roomService;
        this.guestService = guestService;
    }

    public boolean checkIn(String reservationId)
    {
        boolean ok=reservationService.checkIn(reservationId);
        if(!ok)
        {
            throw new IllegalStateException("Check in failed");
        }
        return true;
    }
    public boolean checkOut(String reservationId)
    {
        boolean ok=reservationService.checkOut(reservationId);
        if(!ok)
        {
            throw new IllegalStateException("Check out failed");
        }
        return true;
    }

    public List<Guest> getAllGuests()
    {
        return guestService.getAllGuests();
    }
    public List<Room> getAllRooms()
    {
        return roomService.getAllRooms();
    }
    public List<Reservation> getAllReservations()
    {
        return reservationService.getAllReservations();
    }
    public Reservation findReservationById(String id)
    {
        return reservationService.findById(id);
    }
    public Guest findGuestById(String guestId)
    {
        return guestService.findGuestById(guestId);
    }
}
