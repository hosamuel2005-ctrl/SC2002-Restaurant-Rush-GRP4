public class Order {

    private MenuItem item;
    private int preparedUnits;
    private String status;

    // Create an order
    public Order(MenuItem item) {
        this.item = item;
        this.preparedUnits = 0;
        this.status = "WAITING_FOR_FOOD";
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
    public String getStatus() {
        return status;
    }

    // Check whether order is ready
    public boolean isReady() {
        return status.equals("READY");
    }

    // Check whether order can be prepared
    public boolean canPrepare() {
        return status.equals("WAITING_FOR_FOOD");
    }

    // Prepare one unit of the order
    public void prepareOneUnit() {
        if (!canPrepare()) {
            System.out.println("This order cannot be prepared");
            return;
        }

        preparedUnits++;

        if (preparedUnits >= item.getPreparationUnits()) {
            status = "READY";
            System.out.println(item.getName() + " is READY.");
        } else {
            System.out.println(item.getName() + " preparation progress: " + preparedUnits + "/" + item.getPreparationUnits());
        }
    }

    // Mark order as served
    public void markServed() {
        if (!isReady()) {
            throw new IllegalStateException("Order is not ready.");
        }
        status = "SERVED";
    }

    // Mark order as paid
    public void markPaid() {
        if (!status.equals("SERVED")) {
            throw new IllegalStateException("Order has not been served.");
        }
        status = "PAID";
    }

    // Cancel an unpaid order
    public void cancel() {
        if (!status.equals("PAID")) {
            status = "CANCELLED";
        }
    }

    

    // Display order information
    public void displayOrderInfo() {
        System.out.println("Item: " + item.getName());
        System.out.println("Progress: " + preparedUnits + "/"
                + item.getPreparationUnits());
        System.out.println("Status: " + status);
    }
}
