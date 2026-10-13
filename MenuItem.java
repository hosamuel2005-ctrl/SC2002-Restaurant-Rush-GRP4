public class MenuItem {

    private String name;
    private int price;
    private int preparationUnits;

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
        System.out.println(name + " - $" + Money.format(price)
                + " - " + preparationUnits + " preparation unit(s)");
    }
}
