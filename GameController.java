import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GameController {
    // === Game constants ===
    // Maximum number of turns in the game
    private static final int MAX_TURNS = 18;

    // Minimum number of paid customers required to win
    private static final int TARGET_PAID = 4;

    // Minimum revenue required to win (in cents)
    private static final int TARGET_REVENUE = 4500;

    // Minimum average satisfaction required to win
    private static final double TARGET_SATISFACTION = 60.0;

    // === Game objects ===
    // Handles communication with the player through the user interface
    private final GameUI ui;

    // Stores the menu of available food items
    private final Menu menu = new Menu();

    // Manages customers waiting to be seated
    private final WaitingQueue queue = new WaitingQueue();

    // Creates a list of tables in the restaurant, each with a unique ID
    private final List<Table> tables = Arrays.asList(new Table(1), new Table(2));

    // Stores all customers that have arrived during the game
    private final List<Customer> customers = new ArrayList<>();

    // === Staff members ===
    private final Waiter waiter;
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();

    // === Game state variables ===
    // Tracks the current turn number
    private int currentTurn;

    // Count the total number of customers served during the game
    private int servedCount;

    // Count the total number of customers who left unsatisfied during the game
    private int nonsatisfiedLeave;

    // Constructor initializes the game controller with the user interface and creates a waiter
    public GameController(GameUI ui) {
        this.ui = ui;
        this.waiter = new Waiter(menu);
    }

    // Main game loop 
    public void startGame() {
        ui.showMessage("Welcome to Restaurant Rush!");
        menu.displayMenu();

        for (currentTurn = 1; currentTurn <= MAX_TURNS; currentTurn++) {
            playTurn();
        }

        currentTurn = MAX_TURNS;
        showGameResult();
    }

    // Execute one turn
    private void playTurn() {
        // Step 1: Scheduled arrivals
        handleArrivals();

        // Display current game state
        displayGameState();

        // Step 2 & 3: Manager assigns staff actions
        // Waiter acts before Chef, according to the game rules
        handleWaiterAction();
        handleChefAction();

        // Step 4: Eating and payment
        advanceEating();
        processPayment();

        // Step 5: Reduce satisfaction for waiting customers and remove those who leave unsatisfied
        applySatisfactionPenalty();

        // Step 6: End of turn summary
        displayEndOfTurn();
    }

    // Customer arrivals
    private void handleArrivals() {
        if (currentTurn == 1 ||
            currentTurn == 4 ||
            currentTurn == 7 ||
            currentTurn == 10 ||
            currentTurn == 13 ||
            currentTurn == 16) {
                // Calculate customer ID based on the arrival turn.
                // Turn 1 = ID 1, Turn 4 = ID 2...
                int customerId = (currentTurn + 2) / 3;
                Customer customer = new Customer(customerId);
                customers.add(customer);
                queue.addCustomer(customer);
                ui.showMessage("Customer " + customerId + " arrived!");
            }
    }

    // Display game state
    private void displayGameState() {
        ui.showTurn(currentTurn);
        ui.showMessage("Revenue: " + Money.format(cashier.getRevenueCents()));
        ui.showMessage("Average satisfaction: " + averageText());
        
        // Waiting queue
        ui.showMessage("Waiting queue: ");
        if (queue.size() == 0) {
            ui.showMessage(" Empty");
        } else {
            for (Customer customer : queue.getCustomers()) {
                ui.showMessage(" " + customer);
            }
        }

        // Table information
        ui.showMessage("Tables: ");
        for (Table table : tables) {
            if (table.isFree()) {
                ui.showMessage(" Table " + table.getId() + ": FREE");
            } else {
                Customer customer = table.getCustomer();
                Order order = customer.getOrder();
                String orderInfo = (order == null) ? "No order" : order.getItem().getName() + " [" + order.getStatus() + ", " + order.getPreparedUnits() + "/" + order.getItem().getPreparationUnits() + "]";
                ui.showMessage(" Table " + table.getId() + ": " + customer + ", " + orderInfo);
            }
        }

        // Available staff tasks
        ui.showMessage("Available staff task:");
        boolean freeTable = false;
        for (Table table : tables) {
            if (table.isFree()) {
                freeTable = true;
            }
        }

        if (freeTable && queue.size() > 0) {
            ui.showMessage(" Waiter: seat a queued customer");
        }

        for (Table table : tables) {
            if (!table.isFree()) {
                Customer customer = table.getCustomer();
                if (customer.getState() == CustomerState.SEATED) {
                    ui.showMessage(" Waiter : take order from customer " + customer.getId());
                }
                if (customer.getState() == CustomerState.ORDERED && customer.getOrder() != null && customer.getOrder().isReady()) {
                    ui.showMessage(" Waiter : serve customer " + customer.getId());
                } 
                if (customer.getOrder() != null && customer.getOrder().canPrepare()) {
                    ui.showMessage(" Chef : prepare customer " + customer.getId() + "'s order");
                }
            }
        }
        ui.showMessage(" Waiter and Chef may choose Wait");
        ui.showMessage(" " + cashier.describeAvailableTask(currentTurn, tables));
    }

    // Waiter action
    private void handleWaiterAction() {
        while (true) {
            ui.showWaiterMenu();
            int choice = ui.readInt("Enter choice: ", 1, 4);
            ActionResult result;

            switch (choice) {
                case 1:
                    int customerId = ui.readInt("Customer ID to seat: ", 1, 6);
                    int tableId = ui.readInt("Table ID: ", 1, 2);
                    result = waiter.seatCustomer(findCustomer(customerId), findTable(tableId), queue);
                    break;
                case 2:
                    customerId = ui.readInt("Customer ID to take order: ", 1, 6);
                    result = waiter.takeOrder(findCustomer(customerId));
                    break;
                case 3:
                    customerId = ui.readInt("Customer ID to serve: ", 1, 6);
                    result = waiter.serveOrder(findCustomer(customerId), currentTurn);
                    if (result.isSuccess()) {
                        servedCount++;
                    }
                    break;
                default:
                    result = waiter.doNothing();
                    break;
            }

            ui.showMessage(result.getMessage());
            if (result.isSuccess()) {
                return;
            }
        }
    }

    // Chef action
    private void handleChefAction() {
        while (true) {
            ui.showChefMenu();
            int choice = ui.readInt("Enter choice: ", 1, 2);
            if (choice == 2) {
                ui.showMessage("Chef waits this turn.");
                return;
            }

            int customerId = ui.readInt("Customer ID for preparation: ", 1, 6);
            Customer customer = findCustomer(customerId);
            Order order = customer == null ? null : customer.getOrder();

            if (customer == null || customer.getState() != CustomerState.ORDERED) {
                ui.showMessage("Customer is not waiting for food.");
                continue;
            }

            if (chef.prepareOrder(order)) {
                return;
            }
        }
    }

    // Eating progess
    private void advanceEating() {
        for (Table table : tables) {
            if (!table.isFree() && table.getCustomer().finishEating(currentTurn)) {
                ui.showMessage("Customer " + table.getCustomer().getId() + " finished eating and is ready to pay.");
            }
        }
    }

    // Payment processing
    private void processPayment() {
        Optional<PaymentRecord> payment = cashier.processPayment(currentTurn, tables);
        if (payment.isPresent()) {
            ui.showMessage(payment.get().describe());
        }
    }

    // Apply satisfaction penalty for waiting customers and remove those who leave unsatisfied
    private void applySatisfactionPenalty() {
        for (Customer customer : customers) {
            if (customer.applyEndOfTurnPenalty()) {
                nonsatisfiedLeave++;
                ui.showMessage("Customer " + customer.getId() + " left unhappy.");
            }
        }

        queue.removeAbandonedCustomers();
        
        for (Table table : tables) {
            if (!table.isFree() && table.getCustomer().getState() == CustomerState.LEFT) {
                table.freeTable();
            }
        }
    }

    // Display game result at the end of the game
    private void showGameResult() {
        double average = cashier.getAverageSatisfaction().orElse(Double.NaN);
        boolean victory = cashier.getPaidCount() >= TARGET_PAID && cashier.getRevenueCents() >= TARGET_REVENUE && average >= TARGET_SATISFACTION;

        ui.showMessage("\n=== GAME OVER ===");
        ui.showMessage(victory ? "VICTORY!" : "DEFEAT!");
        displayEndOfTurn();
        ui.showMessage("Targets: 4 paid customers, $45.00 revenue, 60 average satisfaction.");
    }

    // Helper methods to find customers and tables by ID, and to format average satisfaction
    private String averageText() {
        if (!cashier.getAverageSatisfaction().isPresent()) {
            return "N/A";
        }
        return String.format("%.1f", cashier.getAverageSatisfaction().getAsDouble());
    }

    // Find a customer by ID from the list of customers
    private Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        return null;
    }

    // Find a table by ID from the list of tables
    private Table findTable(int id) {
        for (Table table : tables) {
            if (table.getId() == id) {
                return table;
            }
        }
        return null;
    }

    // Display end-of-turn summary including served customers, unsatisfied departures, paid customers, revenue, and average satisfaction
    private void displayEndOfTurn() {
        ui.showMessage("\nEnd of turn " + currentTurn);

        ui.showMessage("Served: " + servedCount + " | Unsatisfied departures: " + nonsatisfiedLeave + " | Paid: " + cashier.getPaidCount());
        ui.showMessage("Revenue: " + Money.format(cashier.getRevenueCents()) + " | Average satisfaction: " + averageText());
    }
}