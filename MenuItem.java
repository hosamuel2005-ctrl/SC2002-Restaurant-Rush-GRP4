public class MenuItem {

    private final String name;
    private final int price;
    private final int preparationUnits;

    // Create a menu item
    public MenuItem(String name, int price, int preparationUnits) {
        // Validate input parameters
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty.");
        }
        // Price cannot be negative
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        // Preparation units must be positive
        if (preparationUnits <= 0) {
            throw new IllegalArgumentException("Preparation units must be positive.");
        }

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
}
