package restaurant.staff;

import restaurant.customer.Customer;
import restaurant.customer.CustomerState;
import restaurant.customer.Table;
import restaurant.kitchen.Order;
import restaurant.util.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * The Cashier. Stage 1 base setting: exactly one Cashier who handles at most
 * ONE payment per turn, automatically (no manager choice).
 *
 * Rules implemented (Section 3.2 step 4 and 3.4):
 * <ul>
 *   <li>Only READY_TO_PAY customers can be charged.</li>
 *   <li>A customer who became READY_TO_PAY this turn pays next turn at the
 *       earliest (checked with {@code getReadyToPayTurn() < turn}, so the result
 *       does not depend on whether eating is advanced before or after payment).</li>
 *   <li>Oldest first: the customer who has been READY_TO_PAY the longest is
 *       charged first; ties go to the lower customer id (earlier arrival).</li>
 *   <li>Payment adds the bill to revenue, marks the customer and the order PAID,
 *       and frees the table.</li>
 *   <li>Revenue only ever grows through {@link #processPayment}, so unpaid or
 *       abandoned orders can never be counted.</li>
 * </ul>
 *
 * The Cashier also owns the running totals the end-of-game check needs: revenue,
 * paid count and the satisfaction recorded at the moment each customer paid.
 */
public class Cashier {

    private int lastPaymentTurn = 0;          // turns start at 1
    private int revenueCents = 0;
    private int satisfactionTotal = 0;
    private final List<PaymentRecord> records = new ArrayList<>();

    // ------------------------------------------------------------------
    // Step 4: automatic payment
    // ------------------------------------------------------------------

    /**
     * Collects payment from at most one customer this turn.
     *
     * @param turn   the current turn number (1-18)
     * @param tables all tables; customers waiting to pay are seated at them
     * @return the receipt, or empty if nobody could be charged this turn
     *         (nobody READY_TO_PAY yet, or the Cashier already charged someone this turn)
     */
    public Optional<PaymentRecord> processPayment(int turn, List<Table> tables) {
        if (turn == lastPaymentTurn) {
            return Optional.empty();           // one payment per turn
        }
        Table table = findNextTableToPay(turn, tables);
        if (table == null) {
            return Optional.empty();
        }

        Customer customer = table.getCustomer();
        Order order = customer.getOrder();

        // Read everything we need BEFORE freeing the table / changing states.
        int bill = order.getPrice();
        int satisfaction = customer.getSatisfaction();
        PaymentRecord record = new PaymentRecord(turn, customer.getId(), table.getId(),
                order.getItem().getName(), bill, satisfaction);

        order.markPaid();
        customer.markPaid();
        table.freeTable();

        revenueCents += bill;
        satisfactionTotal += satisfaction;
        records.add(record);
        lastPaymentTurn = turn;
        return Optional.of(record);
    }

    // ------------------------------------------------------------------
    // Queries for the display and the end-of-game check
    // ------------------------------------------------------------------

    /** Customer the Cashier would charge on this turn, if any (does not charge). */
    public Optional<Customer> previewNextPayer(int turn, List<Table> tables) {
        if (turn == lastPaymentTurn) {
            return Optional.empty();
        }
        Table table = findNextTableToPay(turn, tables);
        return table == null ? Optional.empty() : Optional.of(table.getCustomer());
    }

    /** Line for the "staff tasks available" part of the start-of-turn display. */
    public String describeAvailableTask(int turn, List<Table> tables) {
        Optional<Customer> next = previewNextPayer(turn, tables);
        if (next.isPresent()) {
            Customer c = next.get();
            return "Cashier will collect " + Money.format(c.getOrder().getPrice())
                    + " from customer #" + c.getId() + " automatically.";
        }
        return "Cashier: no customer ready to pay.";
    }

    /** Total revenue in cents. Includes paid orders only. */
    public int getRevenueCents() {
        return revenueCents;
    }

    /** Number of customers who have paid. */
    public int getPaidCount() {
        return records.size();
    }

    /** Sum of the satisfaction values recorded at payment time. */
    public int getSatisfactionTotal() {
        return satisfactionTotal;
    }

    /**
     * Average satisfaction of paid customers, using the value recorded when each
     * paid. Empty when nobody has paid yet (the display shows "N/A").
     */
    public OptionalDouble getAverageSatisfaction() {
        if (records.isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) satisfactionTotal / records.size());
    }

    /** Read-only list of all receipts, in payment order. */
    public List<PaymentRecord> getPaymentHistory() {
        return Collections.unmodifiableList(records);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /** Oldest-first table whose customer may be charged this turn, or null. */
    private Table findNextTableToPay(int turn, List<Table> tables) {
        Table best = null;
        for (Table t : tables) {
            if (t.isFree() || !canCharge(t.getCustomer(), turn)) {
                continue;
            }
            if (best == null || isOlder(t.getCustomer(), best.getCustomer())) {
                best = t;
            }
        }
        return best;
    }

    private boolean canCharge(Customer c, int turn) {
        return c.getState() == CustomerState.READY_TO_PAY
                && c.getOrder() != null
                && c.getReadyToPayTurn() < turn;
    }

    /** a has been waiting to pay longer than b (ties: lower id = earlier arrival). */
    private boolean isOlder(Customer a, Customer b) {
        if (a.getReadyToPayTurn() != b.getReadyToPayTurn()) {
            return a.getReadyToPayTurn() < b.getReadyToPayTurn();
        }
        return a.getId() < b.getId();
    }
}
