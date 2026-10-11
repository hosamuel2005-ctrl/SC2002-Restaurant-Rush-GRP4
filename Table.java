/** One restaurant table holds at most one customer at a time. */
public class Table {
    private final int id;
    private Customer customer;

    public Table(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Table ID must be positive.");
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isFree() {
        return customer == null;
    }

    /** Usually called by WaitingQueue.seatCustomer(). */
    public void seatCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!isFree()) {
            throw new IllegalStateException("Table " + id + " is occupied.");
        }
        // Changing the state first ensures an invalid seat does not occupy the table.
        customer.seat();
        this.customer = customer;
    }

    /** A table is only released when its customer has paid or left. */
    public void freeTable() {
        if (isFree()) {
            throw new IllegalStateException("Table " + id + " is already free.");
        }
        CustomerState state = customer.getState();
        if (state != CustomerState.PAID && state != CustomerState.LEFT) {
            throw new IllegalStateException(
                "Table " + id + " cannot be freed before payment or abandonment."
            );
        }
        customer = null;
    }

    @Override
    public String toString() {
        return isFree() ? "Table " + id + ": FREE" : "Table " + id + ": " + customer;
    }
}
