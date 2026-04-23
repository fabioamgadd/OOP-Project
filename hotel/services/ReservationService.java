package hotel.services;

import hotel.database.HotelDatabase;
import hotel.models.Reservation;
import hotel.models.Room;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private final RoomService    roomService;
    private final InvoiceService invoiceService;

    public ReservationService(RoomService roomService, InvoiceService invoiceService) {
        this.roomService    = roomService;
        this.invoiceService = invoiceService;
    }

    public Reservation makeReservation(String guestId, String roomId,
                                       LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomService.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room '" + roomId + "' does not exist.");
        }
        if (!room.isAvailable()) {
            throw new IllegalStateException("Room '" + roomId + "' is currently not available.");
        }

        for (Reservation existing : HotelDatabase.reservations) {
            if (existing.getRoomId().equals(roomId) &&
                existing.getStatus() != hotel.enums.ReservationStatus.CANCELLED &&
                existing.getStatus() != hotel.enums.ReservationStatus.CHECKED_OUT) {
                
                if (checkInDate.isBefore(existing.getCheckOutDate()) && checkOutDate.isAfter(existing.getCheckInDate())) {
                    throw new IllegalStateException("Room '" + roomId + "' is already booked for these dates.");
                }
            }
        }

        Reservation res = new Reservation(guestId, room, checkInDate, checkOutDate);
        HotelDatabase.reservations.add(res);

        invoiceService.createInvoice(res.getReservationId(), guestId, res.getTotalCost());

        return res;
    }

    public List<Reservation> getReservationsByGuest(String guestId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getGuestId().equals(guestId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> getActiveReservationsByGuest(String guestId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : HotelDatabase.reservations) {
            if (!r.getGuestId().equals(guestId)) {
                continue;
            }
            if (r.getStatus() == hotel.enums.ReservationStatus.CONFIRMED || r.getStatus() == hotel.enums.ReservationStatus.PENDING || r.getStatus() == hotel.enums.ReservationStatus.CHECKED_IN) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reservation> getAllReservations() {
        return HotelDatabase.reservations;
    }

    public Reservation findById(String reservationId) {
        for (Reservation r : HotelDatabase.reservations) {
            if (r.getReservationId().equals(reservationId)) {
                return r;
            }
        }
        return null;
    }


    public boolean cancelReservation(String reservationId, String guestId) {
        Reservation res = findById(reservationId);
        if (res == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }
        if (!res.getGuestId().equals(guestId)) {
            throw new SecurityException("You can only cancel your own reservations.");
        }
        if (!res.cancel()) {
            throw new IllegalStateException("Reservation cannot be cancelled in its current status: " + res.getStatus());
        }

        invoiceService.voidInvoiceForReservation(reservationId);

        return true;
    }

    public boolean checkIn(String reservationId) {
        Reservation res = findById(reservationId);
        if (res == null) throw new IllegalArgumentException("Reservation not found.");
        if (LocalDate.now().isBefore(res.getCheckInDate())) {
            throw new IllegalStateException("Cannot check in before the reservation start date (" + res.getCheckInDate() + ").");
        }
        boolean ok = res.checkIn();
        return ok;
    }


    public boolean checkOut(String reservationId) {
        Reservation res = findById(reservationId);
        if (res == null) throw new IllegalArgumentException("Reservation not found.");
        boolean ok = res.checkOut();
        if (ok) {
        }
        return ok;
    }

}
