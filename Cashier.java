import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class Cashier {
    // Turns start at 1
    private int lastPaymentTurn = 0;
    private int revenueCents = 0;
    private int satisfactionTotal = 0;
    private final List<PaymentRecord> records = new ArrayList<>();

    // Charge at most one customer per turn; the bill is already locked in the order
    public Optional<PaymentRecord> processPayment(int turn, List<Table> tables) {
        requireValid(turn, tables);
        if (turn == lastPaymentTurn) {
            return Optional.empty();
        }
        Table table = findNextTableToPay(turn, tables);
        if (table == null) {
            return Optional.empty();
        }

        Customer customer = table.getCustomer();
        Order order = customer.getOrder();

        // Read everything before the table is freed
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

    // Customer who would be charged this turn, without charging
    public Optional<Customer> previewNextPayer(int turn, List<Table> tables) {
        requireValid(turn, tables);
        if (turn == lastPaymentTurn) {
            return Optional.empty();
        }
        Table table = findNextTableToPay(turn, tables);
        return table == null ? Optional.empty() : Optional.of(table.getCustomer());
    }

    // Line for the available staff tasks display
    public String describeAvailableTask(int turn, List<Table> tables) {
        Optional<Customer> next = previewNextPayer(turn, tables);
        if (next.isPresent()) {
            Customer c = next.get();
            Order order = c.getOrder();
            return "Cashier will collect " + Money.format(order.getPrice())
                    + describeDiscount(order)
                    + " from customer #" + c.getId() + " automatically.";
        }
        return "Cashier: no customer ready to pay.";
    }

    // Revenue from paid orders only
    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return records.size();
    }

    // Sum of satisfaction recorded at payment
    public int getSatisfactionTotal() {
        return satisfactionTotal;
    }

    // Empty when nobody has paid yet
    public OptionalDouble getAverageSatisfaction() {
        if (records.isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) satisfactionTotal / records.size());
    }

    public List<PaymentRecord> getPaymentHistory() {
        return Collections.unmodifiableList(records);
    }

    private void requireValid(int turn, List<Table> tables) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (tables == null) {
            throw new IllegalArgumentException("Tables cannot be null.");
        }
    }

    // Empty text when the bill equals the menu price
    private String describeDiscount(Order order) {
        int saved = order.getItem().getPrice() - order.getPrice();
        return saved > 0 ? " (discount applied: " + Money.format(saved) + ")" : "";
    }

    // Oldest-first table that can be charged this turn, or null
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

    // A customer who became READY_TO_PAY this turn pays next turn at the earliest
    private boolean canCharge(Customer c, int turn) {
        return c.getState() == CustomerState.READY_TO_PAY
                && c.getOrder() != null
                && c.getReadyToPayTurn() < turn;
    }

    // Longer waiting to pay comes first, ties go to the lower id
    private boolean isOlder(Customer a, Customer b) {
        if (a.getReadyToPayTurn() != b.getReadyToPayTurn()) {
            return a.getReadyToPayTurn() < b.getReadyToPayTurn();
        }
        return a.getId() < b.getId();
    }
}
