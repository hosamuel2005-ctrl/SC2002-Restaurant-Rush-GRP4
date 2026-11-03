public class Menu {

    private final MenuItem salad =
            new MenuItem("Salad", 900, 1);

    private final MenuItem burger =
            new MenuItem("Burger", 1500, 1);

    private final MenuItem pasta =
            new MenuItem("Pasta", 1800, 2);

    private final MenuItem[] items = {
        salad,
        burger,
        pasta,
        new ComboMeal(
                "Salad + Pasta Combo",
                salad,
                pasta)
    };

    // Get a menu item
    public MenuItem getItem(int index) {
        return items[index];
    }

    // Get number of available dishes
    public int getMenuSize() {
        return items.length;
    }
}
