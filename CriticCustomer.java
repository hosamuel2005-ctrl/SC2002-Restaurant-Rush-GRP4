// Critic customer with higher satisfaction loss and a distinct order preference
public class CriticCustomer extends Customer {

    // Create a critic customer
    public CriticCustomer(int id) {
        super(id);
    }

    // Critic loses 12 satisfaction per waiting turn
    @Override
    public int getSatisfactionLossPerTurn() {
        return 12;
    }

    // Critic chooses the last available menu item
    @Override
    public int chooseMenuItemIndex(int menuSize) {
        if (menuSize <= 0) {
            throw new IllegalArgumentException("Menu size must be positive.");
        }

        return menuSize - 1;
    }
}
