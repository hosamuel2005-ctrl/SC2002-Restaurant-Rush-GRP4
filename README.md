# Stage 2 Customer Classes

- `Customer.java`: current Stage 1 lifecycle, order cancellation and validation preserved; adds `calculatePrice(int)` (regular price) and `chooseMenuItemIndex(int)` (default menu choice).
- `VIPCustomer.java`: loses 5 satisfaction per turn and gets 10% off.
- `CriticCustomer.java`: loses 12 satisfaction per turn, pays full price, and prefers Pasta (menu index 2).

All files use the default package, matching the current project. No direct console output.

## Team integration
- Order teammate: construct `new Order(item, customer)`, lock `customer.calculatePrice(item.getPrice())` at creation, preserve `OrderStatus` and validation.
- Waiter teammate: use `customer.chooseMenuItemIndex(menu.getMenuSize())` and `new Order(item, customer)`.
- Controller teammate: create VIP and Critic instances in the scheduled arrivals.
- Cashier: keep reading `order.getPrice()`.
