package restaurant.staff;

import restaurant.util.Money;

/**
 * Immutable receipt for one completed payment. The satisfaction is the value
 * recorded at the moment of payment, which is what the "average satisfaction
 * of paid customers" objective uses (Section 3.5).
 */
public final class PaymentRecord {

    private final int turn;
    private final int customerId;
    private final int tableId;
    private final String itemName;
    private final int billCents;
    private final int satisfaction;

    PaymentRecord(int turn, int customerId, int tableId,
                  String itemName, int billCents, int satisfaction) {
        this.turn = turn;
        this.customerId = customerId;
        this.tableId = tableId;
        this.itemName = itemName;
        this.billCents = billCents;
        this.satisfaction = satisfaction;
    }

    public int getTurn() { return turn; }
    public int getCustomerId() { return customerId; }
    public int getTableId() { return tableId; }
    public String getItemName() { return itemName; }
    public int getBillCents() { return billCents; }
    public int getSatisfaction() { return satisfaction; }

    /** One-line text for the end-of-turn log. */
    public String describe() {
        return "Cashier collected " + Money.format(billCents)
                + " from customer #" + customerId
                + " (" + itemName + ", table " + tableId + "). "
                + "Satisfaction recorded: " + satisfaction + ".";
    }

    @Override
    public String toString() {
        return "Turn " + turn + ": " + describe();
    }
}
