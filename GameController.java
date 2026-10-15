import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GameController {
    private static final int MAX_TURNS = 18;
    private static final int TARGET_PAID = 4;
    private static final int TARGET_REVENUE = 4500;
    private static final double TARGET_SATISFACTION = 60.0;

    private final GameUI ui;
    private final Menu menu = new Menu();
    private final WaitingQueue queue = new WaitingQueue();
    private final List<Table> tables = Arrays.asList(new Table(1), new Table(2));
    private final List<Customer> customers = new ArrayList<>();

    private final Waiter waiter;
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();

    private int currentTurn;
    private int servedCount;
    private int nonsatisfiedLeave;

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

    // Execute one complete turn
    private void playTurn() {
        // Step 1: Scheduled arrivals
        handleArrivals();

        // Display current game state
        displayGameState();

        // Step 2 & 3: Waiter acts before Chef
        handleWaiterAction();
        handleChefAction();

        // Step 4: Eating and payment
        advanceEating();
        processPayment();

        // Step 5: Satisfaction and abandonment
        applySatisfactionPenalty();

        // Step 6: End of turn summary
        displayEndOfTurn();
    }

    // Scheduled customer arrivals 
    private void handleArrivals() {
        if (currentTurn == 1 ||
            currentTurn == 4 ||
            currentTurn == 7 ||
            currentTurn == 10 ||
            currentTurn == 13 ||
            currentTurn == 16) {
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

        ui.showMessage("Waiting queue: ");
        if (queue.size() == 0) {
            ui.showMessage(" Empty");
        } else {
            for (Customer customer : queue.getCustomers()) {
                ui.showMessage(" " + customer);
            }
        }

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
                    ui.showMessage(" Waiter : serve custumer " + customer.getId());
                } 
                if (customer.getOrder() != null && customer.getOrder().canPrepare()) {
                    ui.showMessage(" Chef : prepare custumer " + customer.getId() + "'s order");
                }
            }
        }
        ui.showMessage(" Waiter and Chef may choose Wait");
        ui.showMessage(" " + cashier.describeAvailableTask(currentTurn, tables));
    }

    // Manager chooses waiter action
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

    // Manager chooses chef action
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

    private void advanceEating() {
        for (Table table : tables) {
            if (!table.isFree() && table.getCustomer().finishEating(currentTurn)) {
                ui.showMessage("Customer " + table.getCustomer().getId() + " finished eating and is ready to pay.");
            }
        }
    }

    private void processPayment() {
        Optional<PaymentRecord> payment = cashier.processPayment(currentTurn, tables);
        if (payment.isPresent()) {
            ui.showMessage(payment.get().describe());
        }
    }

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

    private void showGameResult() {
        double average = cashier.getAverageSatisfaction().orElse(Double.NaN);
        boolean victory = cashier.getPaidCount() >= TARGET_PAID && cashier.getRevenueCents() >= TARGET_REVENUE && average >= TARGET_SATISFACTION;

        ui.showMessage("\n=== GAME OVER ===");
        ui.showMessage(victory ? "VICTORY!" : "DEFEAT!");
        displayEndOfTurn();
        ui.showMessage("Targets: 4 paid customers, $45.00 revenue, 60 average satisfaction.");
    }

    private String averageText() {
        if (!cashier.getAverageSatisfaction().isPresent()) {
            return "N/A";
        }
        return String.format("%.1f", cashier.getAverageSatisfaction().getAsDouble());
    }

    private Customer findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        return null;
    }

    private Table findTable(int id) {
        for (Table table : tables) {
            if (table.getId() == id) {
                return table;
            }
        }
        return null;
    }

    private void displayEndOfTurn() {
        ui.showMessage("\nEnd of turn " + currentTurn);

        ui.showMessage("Served: " + servedCount + " | Unsatisfied departures: " + nonsatisfiedLeave + " | Paid: " + cashier.getPaidCount());
        ui.showMessage("Revenue: " + Money.format(cashier.getRevenueCents()) + " | Average satisfaction: " + averageText());
    }
}