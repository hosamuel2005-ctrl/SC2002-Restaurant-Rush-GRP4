public class Order {

    private final MenuItem item;
    private int preparedUnits;
    private OrderStatus status;

    // Create an order
    public Order(MenuItem item) {
        // Menu item cannot be null
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        this.item = item;
        this.preparedUnits = 0;
        this.status = OrderStatus.WAITING_FOR_FOOD;
    }

    // Get ordered menu item
    public MenuItem getItem() {
        return item;
    }

    // Get order price
    public int getPrice() {
        return item.getPrice();
    }

    // Get completed preparation units
    public int getPreparedUnits() {
        return preparedUnits;
    }

    // Get current order status
    public OrderStatus getStatus() {
        return status;
    }

    // Check whether order is ready
    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    // Check whether order can be prepared
    public boolean canPrepare() {
        return status == OrderStatus.WAITING_FOR_FOOD;
    }

    // Prepare one unit of the order
    public void prepareOneUnit() {
        if (!canPrepare()) {
            throw new IllegalStateException("Order cannot be prepared.");
        }

        preparedUnits++;

        if (preparedUnits >= item.getPreparationUnits()) {
            status = OrderStatus.READY;
        }
    }

    // Mark order as served
    public void markServed() {
        if (!isReady()) {
            throw new IllegalStateException("Order is not ready.");
        }
        status = OrderStatus.SERVED;
    }

    // Mark order as paid
    public void markPaid() {
        if (!status.equals(OrderStatus.SERVED)) {
            throw new IllegalStateException("Order has not been served.");
        }
        status = OrderStatus.PAID;
    }

    // Cancel an unpaid order
    public void cancel() {
        if (!status.equals(OrderStatus.PAID)) {
            status = OrderStatus.CANCELLED;
        }
    }
}
