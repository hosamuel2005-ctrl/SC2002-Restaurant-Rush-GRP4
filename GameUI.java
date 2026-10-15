import java.util.Scanner;

// Handles all user interface interactions for the game, including displaying messages, menus, and reading user input.
public class GameUI {
    private final Scanner scanner = new Scanner(System.in);

    // Display text
    public void showMessage(String message) {
        System.out.println(message);
    }

    // Display the current turn
    public void showTurn(int turn) {
        System.out.println("\n==============================");
        System.out.println("RESTAURANT RUSH");
        System.out.println("TURN " + turn);
        System.out.println("==============================");
    }

    // Display game statistics
    public void showStats(int revenueCents, double avgSatisfaction) {
        System.out.println("Revenue: " + Money.format(revenueCents));

        if (Double.isNaN(avgSatisfaction)) {
            System.out.println("Average satisfaction: N/A");
        } else {
            System.out.printf("Average satisfaction: %.1f%n", avgSatisfaction);
        }
    }

    // Waiter action menu: diplay the available actions for the waiter
    public void showWaiterMenu() {
        System.out.println();
        System.out.println(" === WAITER ACTION === ");
        System.out.println("1. Seat customer");
        System.out.println("2. Take order");
        System.out.println("3. Serve ready order");
        System.out.println("4. Wait");
    }

    // Chef action menu: display the available actions for the chef
    public void showChefMenu() {
        System.out.println();
        System.out.println(" === CHEF ACTION === ");
        System.out.println("1. Prepare an order");
        System.out.println("2. Wait");
    }

    // Read any integer within a range
    // Keep asking until the player enters a valid integer within the specified range
    public int readInt(String prompt, int min, int max) {
        while(true) {
            System.out.print(prompt);

            if(!scanner.hasNextInt()) {
                System.out.println("Invalid input. Enter a number.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice < min || choice > max) {
                System.out.println("Choose between " + min + " and " + max + ".");
                continue;
            }

            return choice;
        }
    }

    // Display the final game result and statistics
    public void showResult(boolean victory, int paidCount, int revenueCents, int average) {
        System.out.println("\n==============================");
        System.out.println("GAME OVER");
        System.out.println("==============================");

        System.out.println(victory ? "VICTORY!" : "DEFEAT!");

        System.out.println("Paid customers: " + paidCount);
        showStats(revenueCents, average);
    }
}