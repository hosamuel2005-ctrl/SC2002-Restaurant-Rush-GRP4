public class Waiter {
  private Menu menu;

  public Waiter(Menu menu) {
    this.menu = menu;
  }

  public ActionResult seatCustomer(Customer customer, Table table, Queue queue) {
    if (customer == null) {
      return ActionResult.failure("Customer not found.");
    }

    if (table == null) {
      return ActionResult.failure("Table not found.");
    }

    if (table.isFree() == false) {
      return ActionResult.failure("Table is already occupied.");
    }

    queue.seatCustomer(customer.getId(), table);

    return ActionResult.success("Customer " + customer.getId() + " seated at table " + table.getId() + "." );
  }

  public ActionResult takeOrder(Customer customer) {
    if (customer == null) {
      return ActionResult.failure("Customer not found.");
    }

    if (customer.getState() != CustomerState.SEATED) {
      return ActionResult.failure("Customer is not ready to order.");
    }

    int index = (customer.getId() - 1) % menu.getMenuSize();
    MenuItem item = menu.getItem(index);

    Order order = new Order(item);
    customer.placeOrder(order);

    return ActionResult.success("Customer " + customer.getId() + " ordered " + item.getName() + ".");   
  }

  public ActionResult serveOrder(Customer customer, int currentTurn) {
    if (customer == null) {
      return ActionResult.failure("Customer not found.");
    }

    if (customer.getState() != CustomerState.ORDERED) {
      return ActionResult.failure("Customer is not waiting for food.");
    }

    Order order = customer.getOrder();

    if (order == null || !order.isReady()) {
      return ActionResult.failure("Order is not ready.");
    }

    order.markServed();
    customer.markServed(currentTurn);

    return ActionResult.success("Customer " + customer.getId() + " had been served " + order.getItem().getName() + ".");
  }

  public ActionResult doNothing() {
    return ActionResult.success("Waiter waits this turn.");
  }
}
