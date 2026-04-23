package hotel.models;

import hotel.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Reservation {

    private static int idCounter = 1;

    private String reservationId;
    private String guestId;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;
    private double totalCost;

    public Reservation(String guestId, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        validateDates(checkInDate, checkOutDate);

        this.reservationId = "R" + String.format("%03d", idCounter++);
        this.guestId = guestId;
        this.roomId = room.getRoomId();
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.CONFIRMED;
        this.totalCost = calculateTotalCost(room, checkInDate, checkOutDate);
    }

    public Reservation(String reservationId, String guestId, String roomId,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       ReservationStatus status, double totalCost) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalCost = totalCost;
    }

    public static double calculateTotalCost(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new IllegalArgumentException("Check-out must be after check-in.");
        return room.getTotalPricePerNight() * nights;
    }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public boolean cancel() {
        if (status == ReservationStatus.CONFIRMED || status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.CANCELLED;
            return true;
        }
        return false;
    }

    public boolean checkIn() {
        if (status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CHECKED_IN;
            return true;
        }
        return false;
    }

    public boolean checkOut() {
        if (status == ReservationStatus.CHECKED_IN) {
            this.status = ReservationStatus.CHECKED_OUT;
            return true;
        }
        return false;
    }

    private static void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null.");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public double getTotalCost() {
        return totalCost;
    }


    @Override
    public String toString() {
        return String.format(
                "Reservation{id='%s', guestId='%s', roomId='%s', checkIn=%s, checkOut=%s, nights=%d, cost=%.2f, status=%s}",
                reservationId, guestId, roomId, checkInDate, checkOutDate,
                getNumberOfNights(), totalCost, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}
