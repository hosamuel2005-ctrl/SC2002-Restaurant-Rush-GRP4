import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tracks only customers who are waiting for a table. */
public class Queue {
    private final List<Customer> customers = new ArrayList<>();

    public void addCustomer(Customer customer) {
        if (customer == null || customer.getState() != CustomerState.WAITING) {
            throw new IllegalArgumentException("Only waiting customers can join the queue.");
        }
        if (findCustomer(customer.getId()) != null) {
            throw new IllegalArgumentException("Customer " + customer.getId() + " is already queued.");
        }
        customers.add(customer);
    }

    public Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        return null;
    }

    /** The manager may choose any queued customer, not only the first. */
    public Customer seatCustomer(int customerId, Table table) {
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer " + customerId + " is not in the queue.");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null.");
        }
        table.seatCustomer(customer);
        customers.remove(customer);
        return customer;
    }

    /** Remove customers who abandoned while waiting. */
    public int removeAbandonedCustomers() {
        int oldSize = customers.size();
        customers.removeIf(c -> c.getState() == CustomerState.LEFT);
        return oldSize - customers.size();
    }

    public int size() {
        return customers.size();
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers));
    }
}
