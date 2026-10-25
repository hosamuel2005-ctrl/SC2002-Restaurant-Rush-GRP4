public class VIPCustomer extends Customer {
    public VIPCustomer(int id) { super(id); }

    @Override
    public int getSatisfactionLossPerTurn() { return 5; }

    @Override
    public int calculatePrice(int basePrice) {
        super.calculatePrice(basePrice);
        return (int) (basePrice * 90L / 100);
    }

    @Override
    public String toString() { return "VIP " + super.toString(); }
}
