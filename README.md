# SC2002-Restaurant-Rush-GRP4

**My part:** Waiter (seat customer, take order, serve READY order) and Cashier (READY_TO_PAY, payment, revenue).
**Status:** Stage 1 only (basic operation). Written against the interface in section 5; compile it together with the real Customer, Table, Order, Chef etc. once they exist.

---

## 1. What is in this folder

```
src/restaurant/
├── staff/
│   ├── Waiter.java         # seat / take order / serve / wait, one task per turn
│   ├── Cashier.java        # automatic payment, revenue, paid count, satisfaction totals
│   ├── ActionResult.java   # success/failure + message returned by staff actions
│   └── PaymentRecord.java  # immutable receipt of one payment
└── util/Money.java         # cents -> "$18.00"
```

## 2. Build

Requires JDK 11 or later (no external libraries). These files will not compile on their own: they depend on the teammate classes listed in section 5. Once those are in the project, compile everything together, for example:

```bash
mkdir -p out
javac -d out $(find src -name '*.java')
```

## 3. Waiter

Base setting: **1 Waiter, one task per turn.**

| Method | Task | What it checks |
|---|---|---|
| `startTurn(int turn)` | (not a task) | Resets the one-task limit. **Controller must call it at the start of every turn.** |
| `seatCustomer(customer, table, queue)` | Seat one queued customer | Customer is in the queue; table is free |
| `takeOrder(customer)` | Take one seated customer's order | Customer is SEATED and has no order yet |
| `serveOrder(order)` | Serve one READY order | Order is READY **and became READY on an earlier turn** |
| `doNothing()` | Wait | – |

Every method returns an `ActionResult` (`isSuccess()`, `getMessage()`).

- **A failed choice never uses up the task.** The message says why (for example "Table 1 is already occupied by customer #2."), so the manager can choose again (brief, Section 3.4).
- **Customers choose their own food.** `takeOrder` asks `customer.chooseMenuItem(menu)` and shows the choice and price in the result message (brief, Section 2.2).
- **The bill is locked when the order is taken.** `takeOrder` fixes the bill and passes it into the `Order`. The Cashier later just reads `order.getBillCents()`.
- **Same-turn serving is blocked in the Waiter itself.** A dish that became READY this turn is refused ("can be served from next turn"). This holds even if the controller runs the Chef before the Waiter.

**Query methods for the CLI and start-of-turn display** (read-only):
`getFreeTables(tables)`, `getSeatableCustomers(queue, tables)`, `getCustomersAwaitingOrder(tables)`, `getServableOrders(tables)`, `describeAvailableTasks(queue, tables)`.

## 4. Cashier

Base setting: **1 Cashier, one payment per turn, automatic (no manager choice).**

| Method | Purpose |
|---|---|
| `processPayment(int turn, List<Table> tables)` | Step 4. Charges at most one customer. Returns `Optional<PaymentRecord>` (empty if nobody could be charged). |
| `previewNextPayer(turn, tables)` / `describeAvailableTask(turn, tables)` | For the start-of-turn display. Does not charge. |
| `getRevenueCents()` | Revenue in integer cents (paid orders only). |
| `getPaidCount()` | Number of paid customers. |
| `getSatisfactionTotal()` / `getAverageSatisfaction()` | Based on the satisfaction recorded at the moment of payment. `OptionalDouble` is empty when nobody has paid, so the display shows **N/A**. |
| `getPaymentHistory()` | Read-only list of receipts. |

Rules implemented:
- Only READY_TO_PAY customers are charged.
- A customer who became READY_TO_PAY **this turn** pays next turn at the earliest (`getReadyToPayTurn() < turn`). This works whether the controller advances eating before or after calling the Cashier.
- **Oldest first** means the customer who has been READY_TO_PAY longest. Ties go to the lower customer id (earlier arrival). *This is my interpretation of "oldest first"; see section 7.*
- Payment adds the bill to revenue, marks the customer and order PAID, and frees the table.
- Revenue only changes inside `processPayment`, so unpaid or abandoned orders can never be counted.

**For the victory check (Samuel):** use integers to avoid rounding issues, for example
`paidCount >= 4 && revenueCents >= 4500 && satisfactionTotal >= 60 * paidCount`.
Format money with `Money.format(cents)`.

## 5. Integration contract: what my code needs from teammates

Package names below are my assumption. If the team uses different packages, only the `import` lines in `Waiter.java` and `Cashier.java` need to change.

**Person 1 (Jingyi): `restaurant.customer`**

| Class | Methods I call |
|---|---|
| `Customer` | `getId()`, `getState()`, `getSatisfaction()`, `getOrder()`, `getReadyToPayTurn()`, `chooseMenuItem(Menu)`, `placeOrder(Order)` (stores the order, state → ORDERED), `markServed(int turn)` (state → eating), `markPaid()` |
| `CustomerState` | constants `SEATED`, `READY_TO_PAY` |
| `Table` | `getId()`, `isOccupied()`, `getCustomer()`, `seat(Customer)` (must also set the customer to SEATED), `free()` |
| `WaitingQueue` | `asList()` (in queue order), `remove(Customer)` |

Also needed from `Customer`: the eating step. After `markServed(t)`, a customer must become READY_TO_PAY on a later turn and record `getReadyToPayTurn()`. Whoever owns it (Jingyi or Samuel's loop), it must run once per turn in step 4.

**Person 2 (Fang Fei): `restaurant.kitchen`**

| Class | Methods I call |
|---|---|
| `Order` | constructor `Order(Customer, MenuItem, int billCents, int turnPlaced)`, `getId()`, `getItem()`, `getCustomer()`, `getStatus()`, `getBillCents()`, **`getReadyTurn()`** (turn the order became READY), `markServed()`, `markPaid()` |
| `OrderStatus` | constant `READY` |
| `MenuItem` | `getName()`, `getPriceCents()`, `getPrepUnits()` |
| `Chef` | `receiveOrder(Order)` (the Waiter hands the new order to the kitchen) |
| `Menu` | passed to `customer.chooseMenuItem(menu)` |

**Person 4 (Samuel): turn loop must do**

1. At the start of each turn: `waiter.startTurn(turn)`.
2. Step 2/3: call the Waiter first, then the Chef.
3. Step 4: advance eating for customers, then `cashier.processPayment(turn, tables)`. Print the returned `PaymentRecord.describe()` if present.
4. After turn 18: read `cashier.getPaidCount()`, `getRevenueCents()`, `getSatisfactionTotal()` for Victory/Defeat.
5. Print `ActionResult.getMessage()` for failed choices and ask the manager to choose again.

## 6. Rules from the brief, and where my code enforces them

| Rule (brief section) | Where |
|---|---|
| Waiter completes at most one task per turn (2.1, 3.4) | `Waiter.taskUsed`, reset by `startTurn` |
| Invalid choice explains why and does not use up the task (3.4) | `ActionResult.failure(...)`; `taskUsed` set only on success |
| Dish finished this turn is served next turn at the earliest (3.2 step 3) | `Waiter.serveBlocker` (`getReadyTurn() >= currentTurn`) |
| Cannot serve before READY | `Waiter.serveBlocker` |
| Customers choose their own food; show the choice (2.2) | `Waiter.takeOrder` message |
| Cashier handles at most one payment per turn (2.1, 3.2 step 4) | `Cashier.lastPaymentTurn` |
| Cashier cannot charge before READY_TO_PAY (3.4) | `Cashier.canCharge` |
| READY_TO_PAY this turn means pays next turn at the earliest (3.2 step 4) | `Cashier.canCharge` (`< turn`) |
| Payment adds revenue, marks customer and order PAID, frees table (3.2 step 4) | `Cashier.processPayment` |
| Revenue never includes unpaid or abandoned orders (3.4) | revenue changes only in `processPayment` |
| Average satisfaction uses value recorded at payment; N/A if no one paid (3.5) | `Cashier.getAverageSatisfaction` |
| Money stored as integer cents, shown with two decimals (2.2) | `int` cents throughout; `Money.format` |

## 7. Assumptions to confirm with the team

1. **"Oldest first"** = longest in READY_TO_PAY, ties by lower customer id. If the team reads it as "earliest arrival", change `Cashier.isOlder` (one method).
2. **Waiter acts immediately when the manager chooses.** If Samuel prefers to collect all choices first and execute afterwards, the Waiter's methods still work if he calls them in step 3 in the order Waiter then Chef.
3. **Who advances eating** (SERVED → READY_TO_PAY) is not owned by the Waiter or Cashier (see section 5).
4. **Who removes cancelled orders from the Chef's list** when a customer abandons is Person 2 / Person 4. The Cashier never sees them because only READY_TO_PAY customers are charged.
