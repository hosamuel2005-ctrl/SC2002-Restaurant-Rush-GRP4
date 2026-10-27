// VIP customer with lower satisfaction loss and a 10% discount
public class VIPCustomer extends Customer {

    // Create a VIP customer
    public VIPCustomer(int id) {
        super(id);
    }

    // VIP loses 5 satisfaction per waiting turn
    @Override
    public int getSatisfactionLossPerTurn() {
        return 5;
    }

    // Apply 10% VIP discount
    @Override
    public int calculatePrice(int basePrice) {
        return (int) Math.round(basePrice * 0.9);
    }
}
