import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Customers waiting for a table
public class WaitingQueue {
    // Internal waiting list
    private final List<Customer> customers = new ArrayList<>();

    // Add a waiting customer
    public void addCustomer(Customer customer) {
        // Only valid waiting customers allowed
        if (customer == null || customer.getState() != CustomerState.WAITING) {
            throw new IllegalArgumentException("Only waiting customers can join the queue.");
        }
        // Prevent duplicate customer IDs
        if (findCustomer(customer.getId()) != null) {
            throw new IllegalArgumentException("Customer " + customer.getId() + " is already queued.");
        }
        customers.add(customer);
    }

    // Find customer by ID
    public Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        // No matching customer
        return null;
    }

    // Seat any chosen queued customer
    public Customer seatCustomer(int customerId, Table table) {
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer " + customerId + " is not in the queue.");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null.");
        }
        // Table also changes customer state
        table.seatCustomer(customer);
        customers.remove(customer);
        return customer;
    }

    // Remove customers who already left
    public int removeAbandonedCustomers() {
        int oldSize = customers.size();
        customers.removeIf(c -> c.getState() == CustomerState.LEFT);
        // Number of removed customers
        return oldSize - customers.size();
    }

    // Number of waiting customers
    public int size() {
        return customers.size();
    }

    // Read-only copy of the queue
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers));
    }
}
