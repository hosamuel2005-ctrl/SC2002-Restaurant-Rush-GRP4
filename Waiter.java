package restaurant.staff;

import restaurant.customer.Customer;
import restaurant.customer.CustomerState;
import restaurant.customer.Table;
import restaurant.customer.WaitingQueue;
import restaurant.kitchen.Chef;
import restaurant.kitchen.Menu;
import restaurant.kitchen.MenuItem;
import restaurant.kitchen.Order;
import restaurant.kitchen.OrderStatus;
import restaurant.util.Money;

import java.util.ArrayList;
import java.util.List;

/**
 * The Waiter. Stage 1 base setting: exactly one Waiter, who completes at most
 * ONE task per turn (Section 2.1 / 3.4). A task is one of:
 * <ul>
 *   <li>{@link #seatCustomer} - seat one queued customer at a free table</li>
 *   <li>{@link #takeOrder}    - take one seated customer's order</li>
 *   <li>{@link #serveOrder}   - serve one READY order</li>
 *   <li>{@link #doNothing}    - Wait</li>
 * </ul>
 *
 * Design notes
 * <ul>
 *   <li>All validation lives here (not in the CLI): the CLI only passes the
 *       manager's choice in and prints the returned message.</li>
 *   <li>A task that fails validation does NOT use up the Waiter's turn; only a
 *       successful action (or an explicit Wait) does.</li>
 *   <li>The Waiter takes the order, so the Waiter is the object that locks the
 *       bill at order time (price is "locked when the order is taken").</li>
 * </ul>
 */
public class Waiter {

    private final Menu menu;
    private final Chef chef;

    /** 0 means startTurn() has not been called yet (turns are numbered from 1). */
    private int currentTurn = 0;
    private boolean taskUsed = false;

    public Waiter(Menu menu, Chef chef) {
        if (menu == null || chef == null) {
            throw new IllegalArgumentException("Waiter needs a Menu and a Chef");
        }
        this.menu = menu;
        this.chef = chef;
    }

    // ------------------------------------------------------------------
    // Turn lifecycle
    // ------------------------------------------------------------------

    /** Must be called by the controller once at the start of every turn. */
    public void startTurn(int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turns are numbered from 1");
        }
        this.currentTurn = turn;
        this.taskUsed = false;
    }

    /** True once a task (or Wait) has been completed this turn. */
    public boolean hasActedThisTurn() {
        return taskUsed;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    // ------------------------------------------------------------------
    // The four tasks
    // ------------------------------------------------------------------

    /** Task 1: seat one queued customer at a free table. */
    public ActionResult seatCustomer(Customer customer, Table table, WaitingQueue queue) {
        requireTurnStarted();
        if (taskUsed) {
            return alreadyActed();
        }
        if (customer == null) {
            return ActionResult.failure("No customer selected to seat.");
        }
        if (table == null) {
            return ActionResult.failure("No table selected.");
        }
        if (!queue.asList().contains(customer)) {
            return ActionResult.failure("Customer #" + customer.getId()
                    + " is not in the waiting queue, so cannot be seated.");
        }
        if (table.isOccupied()) {
            return ActionResult.failure("Table " + table.getId()
                    + " is already occupied by customer #" + table.getCustomer().getId() + ".");
        }

        queue.remove(customer);
        table.seat(customer);
        taskUsed = true;
        return ActionResult.success("Waiter seated customer #" + customer.getId()
                + " at table " + table.getId() + ".");
    }

    /**
     * Task 2: take one seated customer's order. The customer chooses their own
     * food (the manager never does); the choice is shown in the result message.
     */
    public ActionResult takeOrder(Customer customer) {
        requireTurnStarted();
        if (taskUsed) {
            return alreadyActed();
        }
        String blocker = orderBlocker(customer);
        if (blocker != null) {
            return ActionResult.failure(blocker);
        }

        MenuItem item = customer.chooseMenuItem(menu);
        int billCents = lockBill(customer, item);
        Order order = new Order(customer, item, billCents, currentTurn);

        customer.placeOrder(order);
        chef.receiveOrder(order);
        taskUsed = true;
        return ActionResult.success("Customer #" + customer.getId() + " chose "
                + item.getName() + " (" + Money.format(billCents) + ", "
                + item.getPrepUnits() + " prep unit(s)). Order #" + order.getId()
                + " sent to the kitchen.");
    }

    /**
     * Task 3: serve one READY order. A dish can only be served if it was
     * already READY when the Waiter acts, so a dish that became READY this turn
     * waits until next turn (Section 3.2, step 3).
     */
    public ActionResult serveOrder(Order order) {
        requireTurnStarted();
        if (taskUsed) {
            return alreadyActed();
        }
        String blocker = serveBlocker(order);
        if (blocker != null) {
            return ActionResult.failure(blocker);
        }

        Customer customer = order.getCustomer();
        order.markServed();
        customer.markServed(currentTurn);
        taskUsed = true;
        return ActionResult.success("Waiter served " + order.getItem().getName()
                + " (order #" + order.getId() + ") to customer #" + customer.getId() + ".");
    }

    /** Task 4: Wait. Uses the turn without doing anything. */
    public ActionResult doNothing() {
        requireTurnStarted();
        if (taskUsed) {
            return alreadyActed();
        }
        taskUsed = true;
        return ActionResult.success("Waiter waits this turn.");
    }

    // ------------------------------------------------------------------
    // Queries for the start-of-turn display and the CLI menus.
    // These never change any state.
    // ------------------------------------------------------------------

    /** Free tables, in the order the tables list gives them. */
    public List<Table> getFreeTables(List<Table> tables) {
        List<Table> free = new ArrayList<>();
        for (Table t : tables) {
            if (!t.isOccupied()) {
                free.add(t);
            }
        }
        return free;
    }

    /** Queued customers who could be seated right now (empty if no table is free). */
    public List<Customer> getSeatableCustomers(WaitingQueue queue, List<Table> tables) {
        if (getFreeTables(tables).isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(queue.asList());
    }

    /** Seated customers whose order can be taken now. */
    public List<Customer> getCustomersAwaitingOrder(List<Table> tables) {
        List<Customer> result = new ArrayList<>();
        for (Table t : tables) {
            if (t.isOccupied() && orderBlocker(t.getCustomer()) == null) {
                result.add(t.getCustomer());
            }
        }
        return result;
    }

    /** READY orders that may be served this turn. */
    public List<Order> getServableOrders(List<Table> tables) {
        List<Order> result = new ArrayList<>();
        for (Table t : tables) {
            if (t.isOccupied()) {
                Order order = t.getCustomer().getOrder();
                if (order != null && serveBlocker(order) == null) {
                    result.add(order);
                }
            }
        }
        return result;
    }

    /** Human-readable "staff tasks available" lines for the start-of-turn display. */
    public List<String> describeAvailableTasks(WaitingQueue queue, List<Table> tables) {
        List<String> lines = new ArrayList<>();
        if (taskUsed) {
            lines.add("Waiter has already acted this turn.");
            return lines;
        }

        List<Customer> seatable = getSeatableCustomers(queue, tables);
        if (!seatable.isEmpty()) {
            StringBuilder sb = new StringBuilder("Seat a queued customer: ");
            for (int i = 0; i < seatable.size(); i++) {
                sb.append(i > 0 ? ", " : "").append('#').append(seatable.get(i).getId());
            }
            sb.append(" -> free table(s): ");
            List<Table> free = getFreeTables(tables);
            for (int i = 0; i < free.size(); i++) {
                sb.append(i > 0 ? ", " : "").append(free.get(i).getId());
            }
            lines.add(sb.toString());
        }

        for (Customer c : getCustomersAwaitingOrder(tables)) {
            lines.add("Take order: customer #" + c.getId());
        }
        for (Order o : getServableOrders(tables)) {
            lines.add("Serve READY order #" + o.getId() + " (" + o.getItem().getName()
                    + ") to customer #" + o.getCustomer().getId());
        }
        lines.add("Wait (do nothing)");
        return lines;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * The bill is fixed when the order is taken. Every customer pays the menu
     * price. Kept as one method so the pricing rule lives in a single place.
     */
    private int lockBill(Customer customer, MenuItem item) {
        return item.getPriceCents();
    }

    /** Returns null if the customer's order can be taken now, otherwise the reason. */
    private String orderBlocker(Customer customer) {
        if (customer == null) {
            return "No customer selected to take an order from.";
        }
        if (customer.getOrder() != null) {
            return "Customer #" + customer.getId() + " has already ordered.";
        }
        if (customer.getState() != CustomerState.SEATED) {
            return "Customer #" + customer.getId() + " cannot order now (state: "
                    + customer.getState() + "). Only seated customers can order.";
        }
        return null;
    }

    /** Returns null if the order can be served now, otherwise the reason. */
    private String serveBlocker(Order order) {
        if (order == null) {
            return "No order selected to serve.";
        }
        if (order.getStatus() != OrderStatus.READY) {
            return "Order #" + order.getId() + " (" + order.getItem().getName()
                    + ") is not READY (status: " + order.getStatus() + ").";
        }
        if (order.getReadyTurn() >= currentTurn) {
            return "Order #" + order.getId() + " (" + order.getItem().getName()
                    + ") only became READY this turn. It can be served from next turn.";
        }
        return null;
    }

    private ActionResult alreadyActed() {
        return ActionResult.failure("The Waiter has already used this turn's task.");
    }

    private void requireTurnStarted() {
        if (currentTurn == 0) {
            throw new IllegalStateException("Waiter.startTurn(turn) must be called before the Waiter acts");
        }
    }
}
