public class Menu {

  // Menu item names
  private final String[] names = {"Salad", "Burger", "Pasta"};

  // Menu item prices in cents
  private final int[] prices = {900, 1500, 1800};

  // Preparation units
  private final int[] preparationUnits = {1, 1, 2};

  // Name of dish
  public String getName(int index) {
    return names[index];
  }

  // Price of a dish
  public int getPrice(int index) {
    return prices[index];
  }

  // Get the preparation units
  public int getPreparationUnits(int index) {
    return preparationUnits[index];
  }

  // Number of availables dishes (here three)
  public int getMenuSize() {
    return names.length;
  }

  // Display the menu
  public void displayMenu() {
    System.out.println("====RESTAURANT MENU====");

    for (int i = 0; i < names.length; i++) {
      System.out.println((i + 1) + ". " names[i] + " $" + String.format("%.2f", prices[i] / 100) + " - " + preparationUnits[i] + " preparation unit(s)");
    }
  }
}
