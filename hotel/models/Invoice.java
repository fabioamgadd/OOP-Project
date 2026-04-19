package hotel.models;

import hotel.enums.PaymentMethod;
import hotel.interfaces.Payable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;


public class Invoice implements Payable {

    private String invoiceId;
    private String reservationId;
    private String guestId;
    private double amountDue;
    private double amountPaid;
    private PaymentMethod paymentMethod;
    private boolean paid;
    private LocalDate issuedDate;
    private LocalDate paidDate;

 

    public Invoice(String reservationId, String guestId, double amountDue) {
        this.invoiceId = UUID.randomUUID().toString();
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.amountDue = amountDue;
        this.amountPaid = 0.0;
        this.paid = false;
        this.issuedDate = LocalDate.now();
    }

    
    public Invoice(String invoiceId, String reservationId, String guestId,
                   double amountDue, double amountPaid, PaymentMethod paymentMethod,
                   boolean paid, LocalDate issuedDate, LocalDate paidDate) {
        this.invoiceId = invoiceId;
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.issuedDate = issuedDate;
        this.paidDate = paidDate;
    }

   
    @Override
    public boolean pay(double amount, PaymentMethod paymentMethod) {
        if (paid) return false;
        if (amount < amountDue) return false;

        this.amountPaid = amount;
        this.paymentMethod = paymentMethod;
        this.paid = true;
        this.paidDate = LocalDate.now();
        return true;
    }

    @Override
    public double getAmountDue() {
        if (paid) {
            return 0.0;
        }
        else {
            return amountDue;
        }
    }

  

    public String getInvoiceId() { return invoiceId; }
    public String getReservationId() { return reservationId; }
    public String getGuestId() { return guestId; }
    public double getTotalAmountDue() { return amountDue; }
    public double getAmountPaid() { return amountPaid; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public boolean isPaid() { return paid; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public LocalDate getPaidDate() { return paidDate; }

  
    public String getFormattedInvoice() {
      String statusStr;
        if (paid) {
            statusStr = "PAID";
        }
        else {
            statusStr = "UNPAID";
        }

        String paidDateStr;
        if (paidDate != null) {
            paidDateStr = paidDate.toString();
        }
        else {
            paidDateStr = "N/A";
        }

        String paymentMethodStr;
        if (paymentMethod != null) {
            paymentMethodStr = paymentMethod.toString();
        }
        else {
            paymentMethodStr = "N/A";
        }
        return String.format(
                "=== Invoice ===\n" +
                "Invoice ID    : %s\n" +
                "Reservation ID: %s\n" +
                "Guest ID      : %s\n" +
                "Amount Due    : EGP %.2f\n" +
                "Amount Paid   : EGP %.2f\n" +
                "Status        : %s\n" +
                "Issued Date   : %s\n" +
                "Paid Date     : %s\n" +
                "Payment Method: %s\n" +
                invoiceId, reservationId, guestId,
                amountDue, amountPaid,statusStr,
                issuedDate,
                paidDateStr,
                paymentMethodStr
        );
    }

    @Override
    public String toString() {
        return String.format("Invoice{id='%s', reservationId='%s', amountDue=%.2f, paid=%b}",
                invoiceId, reservationId, amountDue, paid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(invoiceId, invoice.invoiceId);
    }

  
}
