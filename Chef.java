public class Chef {

    public void prepareOrder(Order order) {
        if (order == null) {
            System.out.println("No order selected.");
            return;
        }

        if (order.isReady()) {
            System.out.println("This order is already ready.");
            return;
        }

        System.out.println(name + " is preparing " + order.getItem().getName() + ".");

        order.prepareOneUnit();
    }
}
