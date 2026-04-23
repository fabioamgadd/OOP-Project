package hotel.utils;
import hotel.models.*;
import java.util.List;

public class DisplayUtils
{
    public static void printHeader(String title)
    {
        System.out.println("");
        System.out.println("⭐ " + title + " ⭐");
    }

    public static void printRooms(List<Room> rooms)
    {
        if(rooms.isEmpty())
        {
            System.out.println("No rooms found");
        }
        System.out.printf("%-12s %-8s %-12s %-12s %-15s %s%n","Room Id:","Floor","Type","Available","Price/Night","Amenities");
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
            System.out.printf("%-12s %-8d %-12s %-12s %-15.2f %s%n","r.getRoomId(),r.getFloorNumber(),r.getRoomType().getTypeName(),r.isAvailable()?"Yes":"No",
                    r.getTotalPricePerNight(),amenities);
        }
    }
    public static void printReservations(List<Reservation> reservations)
    {
        if(reservations.isEmpty())
        {
            System.out.println("No reservations found");
        }
        System.out.printf("%-18s %-10s %-15s %-15s %-10s %s%n","Reservation Id:","Room","Check in","Check out","Cost","Status");
        for(int i=0;i<reservations.size();i++)
        {
            Reservation res = reservations.get(i);
            System.out.printf("%-18s %-10s %-15s %-15s %-10.2f %s%n",res.getReservationId(), res.getRoomId(), res.CheckInDate(), res.CheckOutDate(), res.getTotalCost(),
                    res.getStatus());
        }

    }
    public static void printGuests(List<Guest> guests)
    {
        if(guests.isEmpty())
        {
            System.out.println("No guests found");
        }
        System.out.printf("%-12s %-15s %-10s %s%n","Guest Id:","Username","Gender","Balance");
        for(int i=0;i<guests.size();i++)
        {
            Guest g = guests.get(i);
            System.out.printf("%-12s %-15s %-10s %.2f%n",g.getGuestId(), g.getUsename(), g.getGender(), g.getBalance());
        }

    }
    public static void printSuccess(String message)
    {
        System.out.println("\nDone successfully: "+message);
        System.out.println("");
    }
    public static void printError(String message)
    {
        System.out.println("\nError: "+message);
        System.out.println("");
    }
    public static void printInfo(String message)
    {
        System.out.println("Info: "+message);
        System.out.println("");
    }


}
