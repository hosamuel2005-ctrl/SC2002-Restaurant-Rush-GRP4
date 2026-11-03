public class Order {

    private final MenuItem item;
    private int preparedUnits;
    private OrderStatus status;
    private final int price;

    // Create an order without Happy Hour
    public Order(MenuItem item, Customer customer) {
        this(item, customer, new HappyHour(), 1);
    }

    // Create an order and lock the final price
    public Order(MenuItem item, Customer customer,
                 HappyHour happyHour, int turn) {

        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (happyHour == null) {
            throw new IllegalArgumentException("Happy Hour cannot be null.");
        }

        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }

        this.item = item;

        // Customer discount first, then Happy Hour
        this.price = happyHour.applyDiscount(
                customer.calculatePrice(item.getPrice()), turn);

        this.preparedUnits = 0;
        this.status = OrderStatus.WAITING_FOR_FOOD;
    }

    // Get ordered menu item
    public MenuItem getItem() {
        return item;
    }

    // Get locked order price
    public int getPrice() {
        return price;
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
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Order has not been served.");
        }

        status = OrderStatus.PAID;
    }

    // Cancel an unpaid order
    public void cancel() {
        if (status != OrderStatus.PAID) {
            status = OrderStatus.CANCELLED;
        }
    }
}
