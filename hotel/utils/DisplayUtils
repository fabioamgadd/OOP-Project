package hotel.utils;
import hotel.models.*;
import java.util.List;

public class DisplayUtils
{
    public static void printHeader(String title)
    {
        System.out.println("\n");
        System.out.println("⭐ " + title + " ⭐");
        System.out.println("\n");
    }

    public static void printRooms(List<Room> rooms)
    {
        if(rooms.isEmpty())
        {
            System.out.println("No rooms found");
        }
        System.out.printf("Room Id:","Floor","Type","Available","Price/Night","Amenities");
        for(int i=0;i<rooms.size();i++)
        {
            Room r =rooms.get(i);
            String amenities;
            if(r.getAmenities().isEmpty())
            {
                amenities="None";
            }
            else
            {
                String result =" ";
                for(int j=0;j<r.getAmenities().size();j++)
                {
                    Amenity a =r.getAmenities.get(j);
                    result += a.getName();
                    if(j<r.getAmenities().size()-1)
                    {
                        result += ", ";
                    }

                }
                amenities=result;
            }
            System.out.printf(r.getRoomId(),r.getFloorNumber(),r.getRoomType(),r.getTypeName(),r.isAvailable()?"Yes":"No",
                    r.getTotalPricePerNight(),amenities);
        }
    }
    public static void printReservations(List<Reservation> reservations)
    {
        if(reservations.isEmpty())
        {
            System.out.println("No reservations found");
        }
        System.out.printf("Reservation Id:","Room","Check in","Check out","Cost","Status");
        for(int i=0;i<reservations.size();i++)
        {
            Reservation res = reservations.get(i);
            System.out.printf(res.getReservationId(), res.getRoomId(), res.CheckInDate(), res.CheckOutDate(), res.getTotalCost(),
                    res.getStatus());
        }

    }
    public static void printGuests(List<Guest> guests)
    {
        if(guests.isEmpty())
        {
            System.out.println("No guests found");
        }
        System.out.printf("Guest Id:","Username","Gender","Balance");
        for(int i=0;i<guests.size();i++)
        {
            Guest g = guests.get(i);
            System.out.printf(g.getGuestId(), g.getUsename(), g.getGender(), g.getBalance());
        }

    }
    public static void printSuccess(String message)
    {
        System.out.println("\nDone successfully: "+message);
    }
    public static void printError(String message)
    {
        System.out.println("\nError: "+message);
    }
    public static void printInfo(String message)
    {
        System.out.println("Info: "+message);
    }


}
