public class ComboMeal extends MenuItem {

    private final MenuItem firstItem;
    private final MenuItem secondItem;

    // Create a combo from two existing menu items
    public ComboMeal(String name, MenuItem firstItem, MenuItem secondItem) {
        super(name,
                calculateComboPrice(firstItem, secondItem),
                calculatePreparationUnits(firstItem, secondItem));

        this.firstItem = firstItem;
        this.secondItem = secondItem;
    }

    // Calculate combo price at 90% of total price
    private static int calculateComboPrice(
            MenuItem firstItem, MenuItem secondItem) {

        validateItems(firstItem, secondItem);

        int totalPrice = firstItem.getPrice() + secondItem.getPrice();

        return (totalPrice * 90 + 50) / 100;
    }

    // Add preparation units of both items
    private static int calculatePreparationUnits(
            MenuItem firstItem, MenuItem secondItem) {

        validateItems(firstItem, secondItem);

        return firstItem.getPreparationUnits()
                + secondItem.getPreparationUnits();
    }

    // Validate combo items
    private static void validateItems(
            MenuItem firstItem, MenuItem secondItem) {

        if (firstItem == null || secondItem == null) {
            throw new IllegalArgumentException(
                    "Combo items cannot be null.");
        }
    }

    // Get first combo item
    public MenuItem getFirstItem() {
        return firstItem;
    }

    // Get second combo item
    public MenuItem getSecondItem() {
        return secondItem;
    }
}
