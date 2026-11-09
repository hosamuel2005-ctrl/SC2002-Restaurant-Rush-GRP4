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

    // Waiter action menu: diplay the available actions for the waiter
    public void showWaiterMenu() {
        System.out.println();
        System.out.println(" === WAITER ACTION === ");
        System.out.println("1. Take order");
        System.out.println("2. Serve ready order");
        System.out.println("3. Wait");
    }

    // Chef action menu: display the available actions for the chef
    public void showChefMenu() {
        System.out.println();
        System.out.println(" === CHEF ACTION === ");
        System.out.println("1. Prepare an order");
        System.out.println("2. Wait");
    }
 
    // Host action menu: display the available actions for the host
    public void showHostMenu() {
        showMessage("\n === HOST ACTION === ");
        showMessage("1. Seat new customer");
        showMessage("2. Wait");
    }

    // Display the menu
    public void showMenu(Menu menu) {
        System.out.println("====RESTAURANT MENU====");

        for (int i = 0; i < menu.getMenuSize(); i++) {
            MenuItem item = menu.getItem(i);
            showMessage((i + 1) + ". " + item.getName() + " - " + Money.format(item.getPrice()) + " - " + item.getPreparationUnits() + " preparation unit(s)");
        }
    }

    // Read any integer within a range
    // Keep asking until the player enters a valid integer within the specified range
    public int readInt(String prompt, int min, int max) {
        while(true) {
            System.out.print(prompt);

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("Input closed.");
            }

            if(!scanner.hasNextInt()) {
                System.out.println("Invalid input. Enter a number.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            if (choice < min || choice > max) {
                System.out.println("Choose between " + min + " and " + max + ".");
                continue;
            }

            return choice;
        }
    }
}