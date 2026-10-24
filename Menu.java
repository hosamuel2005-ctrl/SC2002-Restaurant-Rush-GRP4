public class Menu {

  private MenuItem[] items = {
    new MenuItem("Salad", 900, 1),
    new MenuItem("Burger", 1500, 1),
    new MenuItem("Pasta", 1800, 2),
  };

  // Get a menu item
  public MenuItem getItem(int index) {
    return items[index];
  }
  
  // Number of availables dishes (here three)
  public int getMenuSize() {
    return items.length;
  }
}
