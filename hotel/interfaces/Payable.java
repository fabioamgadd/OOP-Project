package hotel.interfaces;

import hotel.enums.PaymentMethod;

public interface Payable {
    boolean pay(double amount, PaymentMethod method);
    double getAmountDue();
}
