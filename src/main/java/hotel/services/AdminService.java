package hotel.services;
import hotel.database.HotelDatabase;
import hotel.models.*;

import java.util.List;
public class AdminService {
    private final RoomService roomService;
    private final GuestService guestService;
    private final InvoiceService invoiceService;

    public AdminService(RoomService roomService, GuestService guestService, InvoiceService invoiceService) {
        this.roomService = roomService;
        this.guestService = guestService;
        this.invoiceService = invoiceService;
    }
    public void addRoom(Room room)
    {
        roomService.add(room);
    }
    public boolean updateRoom(Room room)
    {
        return roomService.update(room);
    }
    public boolean deleteRoom(String roomId)
    {
        return roomService.delete(roomId);
    }
    public Room getRoom(String roomId)
    {
        return roomService.findById(roomId);
    }
    public List <Room>getAllRooms()
    {
        return roomService.getAllRooms();
    }


    public void addRoomType(RoomType rt)
    {
        roomService.addRoomType(rt);
    }
    public boolean updateRoomType(RoomType rt)
    {
        return roomService.updateRoomType(rt);
    }
    public boolean deleteRoomType(String typeId)
    {
        return roomService.deleteRoomType(typeId);
    }
    public RoomType getRoomType(String typeId)
    {
        return roomService.findRoomTypeById(typeId);
    }
    public List <RoomType>getAllRoomTypes()
    {
        return roomService.getAllRoomTypes();
    }


    public void addAmenity(Amenity a)
    {
        roomService.addAmenity(a);
    }
    public boolean updateAmenity(Amenity a)
    {
        return roomService.updateAmenity(a);
    }
    public boolean deleteAmenity(String amenityId)
    {
        return roomService.deleteAmenity(amenityId);
    }
    public Amenity getAmenity(String amenityId)
    {
        return roomService.findAmenityById(amenityId);
    }
    public List <Amenity>getAllAmenities()
    {
        return roomService.getAllAmenities();
    }

    public List <Guest>getAllGuests()
    {
        return guestService.getAllGuests();
    }
    public Guest getGuest(String guestId)
    {
        return guestService.findGuestById(guestId);
    }
    

    public List <Staff>getAllStaff()
    {
        return HotelDatabase.staffMembers;
    }

    public List<Reservation>getAllReservations()
    {
        return HotelDatabase.reservations;
    }


    public List<Invoice>getAllInvoices()
    {
        return invoiceService.getAllInvoices();
    }
    public double getTotalRevenue()
    {
        double total=0;
        for(int i=0;i<HotelDatabase.invoices.size();i++)
        {
            Invoice invoice =HotelDatabase.invoices.get(i);
            if(invoice.isPaid())
            {
                total+=invoice.getAmountPaid();
            }
        }
        return total;
    }
    public long getOccupiedRoomCount()
    {
        long count=0;
        for(int i=0;i<HotelDatabase.rooms.size();i++)
        {
            Room room =HotelDatabase.rooms.get(i);
            if(!room.isAvailable())
            {
                count++;
            }
        }
        return count;
    }
    public long getAvailableRoomCount()
    {
        long count=0;
        for(int i=0;i<HotelDatabase.rooms.size();i++)
        {
            Room room =HotelDatabase.rooms.get(i);
            if(room.isAvailable())
            {
                count++;
            }
        }
        return count;
    }
}
