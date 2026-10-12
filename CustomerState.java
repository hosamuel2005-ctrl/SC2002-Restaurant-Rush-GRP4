// Customer stages in Stage 1
public enum CustomerState {
    // Waiting for a table
    WAITING,
    // Sitting at a table
    SEATED,
    // Order placed
    ORDERED,
    // Food served
    SERVED,
    // Finished eating
    READY_TO_PAY,
    // Payment completed
    PAID,
    // Left the restaurant
    LEFT
}
