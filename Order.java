public class Order {

    private MenuItem item;
    private int preparedUnits;
    private String status;

    public Order(MenuItem item) {
        this.item = item;
        this.preparedUnits = 0;
        this.status = "WAITING_FOR_FOOD";
    }

    public MenuItem getItem() {
        return item;
    }

    public int getPreparedUnits() {
        return preparedUnits;
    }

    public String getStatus() {
        return status;
    }

    public void prepareOneUnit() {
        if (status.equals("READY")) {
            System.out.println("This order is already ready.");
            return;
        }

        preparedUnits++;

        if (preparedUnits >= item.getPreparationUnits()) {
            status = "READY";
            System.out.println(item.getName() + " is READY.");
        } else {
            System.out.println(item.getName() + " preparation progress: "
                    + preparedUnits + "/" + item.getPreparationUnits());
        }
    }

    public boolean isReady() {
        return status.equals("READY");
    }

    public void displayOrderInfo() {
        System.out.println("Item: " + item.getName());
        System.out.println("Progress: " + preparedUnits + "/"
                + item.getPreparationUnits());
        System.out.println("Status: " + status);
    }
}