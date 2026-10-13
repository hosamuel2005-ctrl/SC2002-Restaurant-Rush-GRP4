// Single-customer restaurant table
public class Table {
    // Unique table ID
    private final int id;
    // Current customer, or null when free
    private Customer customer;

    // Create an empty table
    public Table(int id) {
        // IDs must be positive
        if (id <= 0) {
            throw new IllegalArgumentException("Table ID must be positive.");
        }
        this.id = id;
    }

    // Table ID
    public int getId() {
        return id;
    }

    // Current seated customer
    public Customer getCustomer() {
        return customer;
    }

    // Check table availability
    public boolean isFree() {
        return customer == null;
    }

    // Seat one waiting customer
    public void seatCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!isFree()) {
            throw new IllegalStateException("Table " + id + " is occupied.");
        }
        // Validate state before occupying table
        customer.seat();
        this.customer = customer;
    }

    // Release table after payment or departure
    public void freeTable() {
        if (isFree()) {
            throw new IllegalStateException("Table " + id + " is already free.");
        }
        CustomerState state = customer.getState();
        // Customer must be finished first
        if (state != CustomerState.PAID && state != CustomerState.LEFT) {
            throw new IllegalStateException(
                "Table " + id + " cannot be freed before payment or abandonment."
            );
        }
        customer = null;
    }

    // Readable table summary
    @Override
    public String toString() {
        return isFree() ? "Table " + id + ": FREE" : "Table " + id + ": " + customer;
    }
}
