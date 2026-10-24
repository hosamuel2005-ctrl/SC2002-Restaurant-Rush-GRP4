public class Chef {

    // Prepare one unit of an order
    public ActionResult prepareOrder(Order order) {
        if (order == null) {
            return ActionResult.failure("No order selected.");
        }
        if (!order.canPrepare()) {
            return ActionResult.failure("This order cannot be prepared.");
        }

        order.prepareOneUnit();

        if (order.isReady()) {
            return ActionResult.success(order.getItem().getName() + " is READY.");
        } else {
            return ActionResult.success(order.getItem().getName() + " preparation progress: " + order.getPreparedUnits() + "/" + order.getItem().getPreparationUnits());
        }
    }
}
