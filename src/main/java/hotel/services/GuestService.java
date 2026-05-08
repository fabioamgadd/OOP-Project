package hotel.services;
import hotel.database.HotelDatabase;
import hotel.enums.PaymentMethod;
import hotel.models.*;

import java.time.LocalDate;
import java.util.List;

public class GuestService {
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final InvoiceService invoiceService;

    public GuestService(RoomService roomService, ReservationService reservationService, InvoiceService invoiceService)
    {
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.invoiceService = invoiceService;
    }
    public List<Room>viewAvailableRooms()
    {
        return roomService.getAvailableRooms();
    }
    public List<Room>viewAvailableRoomsByType(String typeName)
    {
        return roomService.getAvailableRoomsByType(typeName);
    }
    public List<Room>viewAvailableRoomsWithinBudget(double maxBudget)
    {
        return roomService.getAvailableRoomsWithinBudget(maxBudget);
    }
    public Reservation makeReservation(Guest guest,String roomId,LocalDate checkIn,LocalDate checkOut)
    {
        return reservationService.makeReservation(guest.getGuestId(),roomId,checkIn,checkOut);
    }
    public List<Reservation>viewMyReservations(Guest guest)
    {
        return reservationService.getReservationsByGuest(guest.getGuestId());
    }
    public boolean cancelReservation(Guest guest,String reservationId)
    {
        return reservationService.cancelReservation(reservationId,guest.getGuestId());
    }
    public Invoice checkoutWithBalance(Guest guest,String reservationId)
    {
        Invoice invoice=invoiceService.findByReservationId(reservationId);
        if(invoice==null)
        {
            throw new IllegalArgumentException("No invoice found for reservation:"+reservationId);
        }
        invoiceService.payWithBalance(invoice.getInvoiceId(),guest);
        return invoice;
    }
    public Invoice checkoutWithExternalPayment(String reservationId,double amountTendered,PaymentMethod paymentMethod)
    {
        Invoice invoice=invoiceService.findByReservationId(reservationId);
        if(invoice==null)
        {
            throw new IllegalArgumentException("No invoice found for reservation:"+reservationId);
        }
        invoiceService.payWithExternal(invoice.getInvoiceId(),amountTendered,paymentMethod);
        return invoice;
    }
    public List<Invoice>viewMyInvoices(Guest guest)
    {
        return invoiceService.getInvoicesByGuest(guest.getGuestId());
    }
    public void updateRoomPreferences(Guest guest, RoomPreference preferences)
    {
        guest.setRoomPreferences(preferences);
    }
    public void depositBalance(Guest guest, double amount)
    {
        guest.deposit(amount);
    }
    public Guest findGuestById(String guestId)
    {

        for (int i=0;i<HotelDatabase.guests.size();i++)
        {
            Guest g =HotelDatabase.guests.get(i);
            if(g.getGuestId().equals(guestId))
            {
                return g;
            }
        }
        return null;
    }
    public List<Guest> getAllGuests()
    {
        return HotelDatabase.guests;
    }
    
}
