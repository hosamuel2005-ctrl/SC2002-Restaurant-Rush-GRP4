public class Order {

    private String itemName;
    private int price;
    private int preparationUnits;
    private int preparedUnits;
    private String status;

    public Order(Menu menu, int index) {
        this.item = menu.getName(index);
        this.price = menu.getPrice(index);
        this.preparationUnits = menu.getPreparationUnits(index);
        this.preparedUnits = 0;
        this.status = "WAITING_FOR_FOOD";
    }

    public String getItemName() {
        return itemName;
    }

    public int getPrice() {
        return price;
    }

    public int getPreparedUnits() {
        return preparedUnits;
    }

    public String getStatus() {
        return status;
    }

    public boolean isReady() {
        return status.equals("READY");
    }

    public void prepareOneUnit() {
        if (isReady()) {
            System.out.println("This order is already ready.");
            return;
        }

        preparedUnits++;

        if (preparedUnits >= item.getPreparationUnits()) {
            status = "READY";
            System.out.println(itemName + " is READY.");
        } else {
            System.out.println(itemName + " preparation progress: " + preparedUnits + "/" + item.getPreparationUnits());
        }
    }

    public void displayOrderInfo() {
        System.out.println("Item: " + item.getName());
        System.out.println("Progress: " + preparedUnits + "/"
                + item.getPreparationUnits());
        System.out.println("Status: " + status);
    }
}
