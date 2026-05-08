package hotel.services;

import hotel.database.HotelDatabase;
import hotel.enums.PaymentMethod;
import hotel.models.Guest;
import hotel.models.Invoice;

import java.util.List;



public class InvoiceService {

    
    public Invoice createInvoice(String reservationId, String guestId, double amountDue) {
        Invoice invoice = new Invoice(reservationId, guestId, amountDue);
        HotelDatabase.invoices.add(invoice);
        return invoice;
    }

   
    public boolean payWithBalance(String invoiceId, Guest guest) {
        Invoice invoice = findById(invoiceId);
        validateInvoiceForPayment(invoice, invoiceId);

        double amount = invoice.getTotalAmountDue();
        if (!guest.deduct(amount)) {
            throw new IllegalStateException(
                    String.format("Insufficient balance. Required: %.2f EGP, Available: %.2f EGP",
                            amount, guest.getBalance()));
        }
        invoice.pay(amount, PaymentMethod.BALANCE);
        return true;
    }

    public double payWithExternal(String invoiceId, double amountTendered, PaymentMethod paymentMethod) {
        Invoice invoice = findById(invoiceId);
        validateInvoiceForPayment(invoice, invoiceId);
        double amountDue = invoice.getTotalAmountDue();

        if (amountTendered < amountDue) {
            throw new IllegalArgumentException(
                    String.format("Amount tendered (%.2f) is less than amount due (%.2f).",
                            amountTendered, amountDue));
        }
        invoice.pay(amountTendered, paymentMethod);
        return amountTendered - amountDue;
    }

  
    public void voidInvoiceForReservation(String reservationId) {
        for (int i = HotelDatabase.invoices.size() - 1; i >= 0; i--) {
            Invoice invoice = HotelDatabase.invoices.get(i);
            if (invoice.getReservationId().equals(reservationId) && !invoice.isPaid()) {
                HotelDatabase.invoices.remove(i);
            }
        }
    }

  

    public Invoice findById(String invoiceId) {
        for (Invoice invoice : HotelDatabase.invoices) {
            if (invoice.getInvoiceId().equals(invoiceId)) {
                return invoice;
            }
        }
        return null;
    }

    public Invoice findByReservationId(String reservationId) {
        for (Invoice invoice : HotelDatabase.invoices) {
            if (invoice.getReservationId().equals(reservationId)) {
                return invoice;
            }
        }
        return null;
    }

    public List<Invoice> getInvoicesByGuest(String guestId) {
        List<Invoice> result = new java.util.ArrayList<>();
        for (Invoice invoice : HotelDatabase.invoices) {
            if (invoice.getGuestId().equals(guestId)) {
                result.add(invoice);
            }
        }
        return result;
    }

    public List<Invoice> getAllInvoices() {
        return HotelDatabase.invoices;
    }

  
    private void validateInvoiceForPayment(Invoice invoice, String invoiceId) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice '" + invoiceId + "' not found.");
        }
        if (invoice.isPaid()) {
            throw new IllegalArgumentException("Invoice '" + invoiceId + "' has already been paid.");
        }
    }
}
