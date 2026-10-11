/** A regular customer. Order details belong to the Order class. */
public class Customer {
    private final int id;
    private int satisfaction;
    private CustomerState state;
    private int servedTurn;
    private int readyToPayTurn;

    public Customer(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.id = id;
        this.satisfaction = 100;
        this.state = CustomerState.WAITING;
        this.servedTurn = -1;
        this.readyToPayTurn = -1;
    }

    public int getId() {
        return id;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    /** -1 until ready; used by Cashier to pay the oldest ready customer first. */
    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    /** Stage 1 regular customers lose 8 satisfaction per unserved turn. */
    public int getSatisfactionLossPerTurn() {
        return 8;
    }

    public void seat() {
        requireState(CustomerState.WAITING);
        state = CustomerState.SEATED;
    }

    public void placeOrder() {
        requireState(CustomerState.SEATED);
        state = CustomerState.ORDERED;
    }

    /** Called when a READY order is actually served by the Waiter. */
    public void markServed(int currentTurn) {
        if (currentTurn < 1) {
            throw new IllegalArgumentException("Turn must be positive.");
        }
        requireState(CustomerState.ORDERED);
        state = CustomerState.SERVED;
        servedTurn = currentTurn;
    }

    /** At the eating step, a previously served customer becomes ready to pay. */
    public boolean finishEating(int currentTurn) {
        if (state == CustomerState.SERVED && currentTurn > servedTurn) {
            state = CustomerState.READY_TO_PAY;
            readyToPayTurn = currentTurn;
            return true;
        }
        return false;
    }

    public void markPaid() {
        requireState(CustomerState.READY_TO_PAY);
        state = CustomerState.PAID;
    }

    /**
     * Apply the end-of-turn penalty once. Returns true if this customer
     * has just abandoned the restaurant due to zero satisfaction.
     */
    public boolean applyEndOfTurnPenalty() {
        if (state != CustomerState.WAITING &&
            state != CustomerState.SEATED &&
            state != CustomerState.ORDERED) {
            return false;
        }

        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
        if (satisfaction == 0) {
            state = CustomerState.LEFT;
            return true;
        }
        return false;
    }

    private void requireState(CustomerState expected) {
        if (state != expected) {
            throw new IllegalStateException(
                "Customer " + id + " must be " + expected + ", but is " + state + "."
            );
        }
    }

    @Override
    public String toString() {
        return "Customer " + id + " [" + state + ", satisfaction=" + satisfaction + "]";
    }
}
