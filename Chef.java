public class Chef {

    public boolean prepareOrder(Order order) {
        if (order == null) {
            System.out.println("No order selected.");
            return false;
        }

        if (!order.canPrepare()) {
            System.out.println("This order cannot be prepared.");
            return false;
        }

        System.out.println("Chef is preparing " + order.getItem().getName() + ".");

        order.prepareOneUnit();

        return true;
    }
}
