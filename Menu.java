public class Menu {

  private MenuItem[] items = {
    new MenuItem("Salad", 900, 1),
    new MenuItem("Bruger", 1500, 1),
    new MenuItem("Salad", 1800, 2),
  };

  public MenuItem getItem(int index) {
    return items[index];
  }
  
  // Number of availables dishes (here three)
  public int getMenuSize() {
    return items.length;
  }

  // Display the menu
  public void displayMenu() {
    System.out.println("====RESTAURANT MENU====");

    for (int i = 0; i < items.length; i++) {
      System.out.println((i + 1) + ". ");
      items[i].displayInfo();
    }
  }
}
