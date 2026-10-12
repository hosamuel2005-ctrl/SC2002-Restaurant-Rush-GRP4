public class Menu {

  private MenuItem[] items;

  public Menu() {
    items = new MenuItem[3];

    items[0] = new MenuItem("Salad", 900, 1);
    items[1] = new MenuItem("Bruger", 1500, 1);
    items[2] = new MenuItem("Salad", 1800, 1);
  }

  public MenuItem getItem(int index) {
    return items[index];
  }
  
  // Number of availables dishes (here three)
  public int getMenuSize() {
    return names.length;
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
