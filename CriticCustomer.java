public class CriticCustomer extends Customer {
    public CriticCustomer(int id) { super(id); }

    @Override
    public int getSatisfactionLossPerTurn() { return 12; }

    @Override
    public int calculatePrice(int basePrice) { return super.calculatePrice(basePrice); }

    @Override
    public int chooseMenuItemIndex(int menuSize) {
        int fallback = super.chooseMenuItemIndex(menuSize);
        return menuSize > 2 ? 2 : fallback;
    }

    @Override
    public String toString() { return "Critic " + super.toString(); }
}
