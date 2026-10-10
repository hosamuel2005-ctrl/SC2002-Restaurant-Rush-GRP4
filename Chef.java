public class Chef {

    private String name;

    public Chef(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void prepareOrder(Order order) {
        if (order == null) {
            System.out.println("No order selected.");
            return;
        }

        if (order.isReady()) {
            System.out.println("This order is already ready.");
            return;
        }

        System.out.println(name + " is preparing "
                + order.getItem().getName() + ".");

        order.prepareOneUnit();
    }
}