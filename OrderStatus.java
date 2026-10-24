// Enum representing the different statuses of an order
public enum OrderStatus {
    // Order is waiting to be prepared
    WAITING_FOR_FOOD,
    // Order is ready to be served
    READY,
    // Order has been served to the customer
    SERVED,
    // Order has been paid for by the customer
    PAID,
    // Order has been cancelled
    CANCELLED
}