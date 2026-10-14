public class MenuItem {

    private final String name;
    private final int price;
    private final int preparationUnits;

    // Create a menu item
    public MenuItem(String name, int price, int preparationUnits) {
        this.name = name;
        this.price = price;
        this.preparationUnits = preparationUnits;
    }

    // Get item name
    public String getName() {
        return name;
    }

    // Get item price
    public int getPrice() {
        return price;
    }

    // Get required preparation units
    public int getPreparationUnits() {
        return preparationUnits;
    }

    // Display item information
    public void displayInfo() {
        System.out.println(name + " - " + Money.format(price)
                + " - " + preparationUnits + " preparation unit(s)");
    }
}
