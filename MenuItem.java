public class MenuItem {

    private final String name;
    private final int price;
    private final int preparationUnits;

    public MenuItem(String name, int price, int preparationUnits) {
        this.name = name;
        this.price = price;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    public void displayInfo() {
        System.out.println(name + " - " + Money.format(price)
                + " - " + preparationUnits + " preparation unit(s)");
    }
}
