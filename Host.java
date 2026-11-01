/**
 * Stage 3 Host: seats one waiting customer per turn independently of Waiter.
 *
 * The Host does not own the queue or tables; it coordinates existing domain
 * objects and returns ActionResult so invalid choices can be retried.
 */
public class Host {
    private int lastActionTurn = -1;

    /**
     * Seat one queued customer at a free table.
     * Failed actions do not consume the Host's turn.
     */
    public ActionResult seatCustomer(Customer customer, Table table,
                                     WaitingQueue queue, int currentTurn) {
        if (currentTurn <= 0) {
            return ActionResult.failure("Turn number must be positive.");
        }
        if (lastActionTurn == currentTurn) {
            return ActionResult.failure("Host has already acted this turn.");
        }
        if (customer == null || queue == null
                || queue.findCustomer(customer.getId()) != customer) {
            return ActionResult.failure("Customer is not in the waiting queue.");
        }
        if (table == null) {
            return ActionResult.failure("Table not found.");
        }
        if (!table.isFree()) {
            return ActionResult.failure("Table is occupied.");
        }

        // Reuse the tested queue/table seating path, including state change.
        try {
            queue.seatCustomer(customer.getId(), table);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ActionResult.failure(ex.getMessage());
        }

        lastActionTurn = currentTurn;
        return ActionResult.success("Host seated customer " + customer.getId()
                + " at table " + table.getId() + ".");
    }

    /** Choose not to use the Host's task this turn. */
    public ActionResult doNothing(int currentTurn) {
        if (currentTurn <= 0) {
            return ActionResult.failure("Turn number must be positive.");
        }
        if (lastActionTurn == currentTurn) {
            return ActionResult.failure("Host has already acted this turn.");
        }
        lastActionTurn = currentTurn;
        return ActionResult.success("Host waits this turn.");
    }
}
