// Regular restaurant customer
public class Customer {
    // Unique customer ID
    private final int id;
    // Current satisfaction score
    private int satisfaction;
    // Current customer stage
    private CustomerState state;
    // Turn when food was served
    private int servedTurn;
    // Turn when payment became possible
    private int readyToPayTurn;
    // Order placed by the customer
    private Order order;

    // Create a new waiting customer
    public Customer(int id) {
        // IDs must be positive
        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.id = id;
        this.satisfaction = 100;
        this.state = CustomerState.WAITING;
        // -1 = not served yet
        this.servedTurn = -1;
        // -1 = not ready to pay yet
        this.readyToPayTurn = -1;
    }

    // Customer ID
    public int getId() {
        return id;
    }

    // Customer order
    public Order getOrder() {
        return order;
    }

    // Assign an order to the customer
    public void setOrder(Order order) {
        this.order = order;
    }

    // Current satisfaction
    public int getSatisfaction() {
        return satisfaction;
    }

    // Current state
    public CustomerState getState() {
        return state;
    }

    // Used to find the oldest ready customer
    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    // Stage 1 satisfaction loss per turn
    public int getSatisfactionLossPerTurn() {
        return 8;
    }

    // Move from waiting to seated
    public void seat() {
        requireState(CustomerState.WAITING);
        state = CustomerState.SEATED;
    }

    // Move from seated to ordered
    public void placeOrder() {
        requireState(CustomerState.SEATED);
        state = CustomerState.ORDERED;
    }

    // Mark food as served
    public void markServed(int currentTurn) {
        // Turn numbers start at 1
        if (currentTurn < 1) {
            throw new IllegalArgumentException("Turn must be positive.");
        }
        requireState(CustomerState.ORDERED);
        state = CustomerState.SERVED;
        servedTurn = currentTurn;
    }

    // Finish eating on a later turn
    public boolean finishEating(int currentTurn) {
        if (state == CustomerState.SERVED && currentTurn > servedTurn) {
            state = CustomerState.READY_TO_PAY;
            readyToPayTurn = currentTurn;
            return true;
        }
        return false;
    }

    // Mark payment complete
    public void markPaid() {
        requireState(CustomerState.READY_TO_PAY);
        state = CustomerState.PAID;
    }

    // Apply one end-of-turn penalty
    public boolean applyEndOfTurnPenalty() {
        // No penalty after service or departure
        if (state != CustomerState.WAITING &&
            state != CustomerState.SEATED &&
            state != CustomerState.ORDERED) {
            return false;
        }

        // Keep satisfaction at or above zero
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
        if (satisfaction == 0) {
            // Customer leaves at zero satisfaction
            state = CustomerState.LEFT;
            return true;
        }
        return false;
    }

    // Enforce valid state transitions
    private void requireState(CustomerState expected) {
        if (state != expected) {
            throw new IllegalStateException(
                "Customer " + id + " must be " + expected + ", but is " + state + "."
            );
        }
    }

    // Readable customer summary
    @Override
    public String toString() {
        return "Customer " + id + " [" + state + ", satisfaction=" + satisfaction + "]";
    }
}
