# Restaurant Rush (SC2002 Group 4)

A turn-based, command-line restaurant management game written in Java. You play the manager: each turn you direct the **Host**, **Waiter** and the **Chef** telling them what to do. The **Cashier** collects payments automatically. Manage customer satisfactionm prepare meals and use Happy Hour strategically to meet all three targets within 18 turns.

**Current status:** Stage 3 (**Final stage**) - Host seating, combo meals and Happy Hour, building on stage 2 regular, VIP and Critic customers.

Repository: [SC2002-Restaurant-Rush-GRP4](https://github.com/hosamuel2005-ctrl/SC2002-Restaurant-Rush-GRP4).

Reproducible victory and defeat runs: [TEST_NOTES.md](TEST_NOTES.md).

---

## Contents

1. [Quick start](#1-quick-start)
2. [Game rules](#2-game-rules)
3. [How to play](#3-how-to-play)
4. [Project structure](#4-project-structure)
5. [Class reference](#5-class-reference)
6. [Design notes](#6-design-notes)
7. [Assumptions and known limitations](#7-assumptions-and-known-limitations)
8. [Testing and stage history](#8-testing-and-stage-history)
9. [Timestamps for video recording](#9-timestamps-for-video-recording)_

---

## 1. Quick start

Requires JDK 8 or later. There are no external libraries, and all files sit in the default package in one folder.

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out *.java
java -cp out Main
```

These commands run from the project root in PowerShell. On Bash, use `mkdir -p out` for the first command. The game starts immediately and reads from standard input.

---

## 2. Game rules

### Setup

| Item | Value |
|---|---|
| Turns | 18 |
| Tables | 2 (one customer per table) |
| Staff | 1 Host, 1 Waiter, 1 Chef, 1 Cashier |
| Customers | 6 in total, arriving on turns 1, 4, 7, 10, 13, 16 (customer IDs 1 to 6) |
| Starting satisfaction | 100 per customer |

### Menu

| Dish | Price | Preparation units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Pasta Combo | $24.30 | 3 |

Customers choose their own food based on deterministic rules.

| Type | Customer ID | Satisfaction loss per waiting turn | Menu selection | Pricing |
|---|---|---|---|---|
| Regular | 1, 4 | 8 | `(id - 1) % menuSize` | Full Price |
| VIP | 2, 5 | 5 | `(id - 1) % menuSize` | 10% discount, rounded to the nearest cent |
| Critic | 3, 6 | 12 | Last menu item | Full price |

With the current four-item menu, customers 1-6 choose Salad, Burger, Salad + Pasta Combo, Salad + Pasta Combo, Salad and Salad + Pasta Combo respectively.
A Combo combines two menu items. Its price is 90% of their combined price, rounded to the nearest centm and its preparation units are added together.
The final customer-specific price is calculated and stored when the order is created. Payment uses this stored price.

### Happy Hour

The manager may activate **Happy Hour** once per game, before staff actions. It lasts for the activation turn and the following turn.

- New orders receive a 20% discount.
- Customer specific discount apply first, followed by the Happy Hour discount.
- Each discount is rounded to the nearest cent.
- Customers in `WAITING`, `SEATED` or `ORDERED` recover up to 15 satisfaction at turn start, before staff actions, capped at 100.
- Existing orders keep their locked prices.
- Happy Hour cannot be activated again after it expires.

For example, a VIP Burger costs $15.00 x 90% x 80% = $10.80 during Happy Hour.

### Win condition

The game runs all 18 turns, with no early-ending gameplay option. Victory or defeat is checked after turn 18. You win only if **all** of these hold:

- at least **4** customers have paid,
- revenue is at least **$45.00**,
- the average satisfaction of paying customers (recorded at the moment each paid) is at least **60**.

If nobody has paid, average satisfaction is shown as **N/A** and the game is lost.

### Turn order

Each turn runs these steps in order:

1. **Arrivals**: a scheduled customer joins the waiting queue.
2. **Happy Hour**: after the turn banner, the manager may activate it if it has not been used. Apply recovery to eligible customers before staff actions.
3. **State display**: show customers after recovery, tables, revenue and Happy Hour status.
4. **Host**: seat one queued customer or wait.
5. **Waiter**: the manager picks one action (seat a queued customer, take order, serve a ready order, or wait).
6. **Chef**: the manager picks one action (prepare one unit of an order, or wait).
7. **Eating and payment**: advance eating then automatically collect payment from at most one eligible customer.
8. **Satisfaction penalty**: apply normal waiting penalties. Customers reaching zero satisfaction level will leave, departure cancels any existing order and frees the customer's table. 
9. **End-of-turn summary**: served, unsatisfied departures, paid count, revenue, average satisfaction.

### Timing rules

- The Host acts before the Waiter, so a customer can be seated and place an order during the same turn.
- The Waiter acts before the Chef, so a dish finished this turn can be served **next turn at the earliest**.
- A customer served on turn *t* finishes eating on turn *t + 1* and is charged on turn *t + 2* at the earliest.
- An invalid choice (for example seating at an occupied table) prints why it failed and lets you choose again. It does not use up the task.
- The Cashier charges at most one customer per turn. If several are ready, the one who has been ready longest goes first (ties go to the lower customer ID).
- Each staff member receives a separate action. A failed action allowz a retry.

### Customer lifecycle

```
WAITING -> SEATED -> ORDERED -> SERVED -> READY_TO_PAY -> PAID
   \          \          \
    +----------+----------+----> LEFT   (satisfaction reached 0)
```

Satisfaction falls while customers are `WAITING`, `SEATED` and `ORDERED`: regular customers lose 8 per turn, VIP lose 5 and Critic loser 12. During Happy Hour, eligible customers recover up to 15 satisfaction at turn start, before staff actions. Satisfaction stops falling once food is served.

### Order lifecycle

```
WAITING_FOR_FOOD -> READY -> SERVED -> PAID
        \
         +--> CANCELLED   (customer left before payment)
```

---

## 3. How to play

Each turn the game prints the current state: turn number, revenue, average satisfaction, the waiting queue, both tables with their customer and order progress, and the tasks currently available. Happy Hour activation and recovery happen after the turn banner and before the detailed state display and staff prompts.
Before staff actions, the game offers Happy Hour activation if it is still available: choose 1 to activate it or2 to save it for a later turn.

**Host menu**

| Choice | Action | Prompts |
|---|---|---|
| 1 | Seat customer | Customer ID, Table ID |
| 2 | Wait | none |

**Waiter menu**

| Choice | Action | Prompts |
|---|---|---|
| 1 | Seat customer | Customer ID, Table ID |
| 2 | Take order | Customer ID |
| 3 | Serve ready order | Customer ID |
| 4 | Wait | none |

**Chef menu**

| Choice | Action | Prompts |
|---|---|---|
| 1 | Prepare an order | Customer ID |
| 2 | Wait | none |

Input is validated: non-numbers and out-of-range values are rejected and re-asked. Customer IDs accept 1 to 6 and table IDs 1 to 2. The Cashier needs no input. If standard input closes, the application prints `Input closed. Exiting the game.` and exits without a stack trace.

**Tips**

- Use the Host and Waiter together to seat a customer and take their order during the same turn.
- Seat and take orders early. Satisfaction starts draining the moment a customer arrives.
- Combo meals require three Chef actions; start preparing them early.
- Pasta takes two Chef turns, so start it as soon as it is ordered.
- Happy Hour improves waiting satisfaction but reduces revenue from new orders.

---

## 4. Project structure

```
.
├── Main.java             # entry point
├── GameController.java   # game loop, turn steps, win/lose check
├── GameUI.java           # console input and output
│
├── Customer.java         # customer state, satisfaction, state transitions
├── VIPCustomer.java      # customer with lower patience loss and a 10% discount
├── CriticCustomer.java   # customer with higher patience loss who chooses the last menu item
├── CustomerState.java    # enum of customer states
├── Table.java            # single-customer table
├── WaitingQueue.java     # customers waiting for a table
├── Host.java             # seats customers, one action per turn
│
├── Menu.java             # three individual dishes and one combo
├── MenuItem.java         # name, price (cents), preparation units
├── Order.java            # preparation progress and order status
├── OrderStatus.java      # enum of order states
├── ComboMeal.java        # combines two menu items at a discounted price
│
├── HappyHour.java        # activation, duration, discounts and recovery bonus
├── Waiter.java           # seat / take order / serve / wait
├── Chef.java             # prepares one unit of an order
├── Cashier.java          # automatic payment, revenue, satisfaction totals
├── ActionResult.java     # success/failure + message from staff actions
├── PaymentRecord.java    # immutable receipt for one payment
├── Money.java            # cents -> "$18.00"
└── TEST_NOTES.md         # replay inputs and verified expected results
```

---

## 5. Class reference

### Game flow

**`Main`**: creates a `GameUI` and a `GameController`, then calls `startGame()`. Handles closed input and displays a clean exit message through the UI.

**`GameController`**: owns the menu, queue, two tables, customer list, four staff members (Host, Waiter, Chef and Cashier). Runs the 18-turn loop, coordinates Happy Hour activation and staff actions, advances earting, processes payments, applies satisfaction recovery and penalties, and checks the final victory conditions. 
Game constants are `MAX_TURNS = 18`, `TARGET_PAID = 4`, `TARGET_REVENUE = 4500` (cents) and `TARGET_SATISFACTION = 60.0`. Each turn it calls `handleArrivals`, `handleHappyHour`, `applyHappyHourRecovery`, `displayGameState`, `handleHostAction`, `handleWaiterAction`, `handleChefAction`, `advanceEating`, `processPayment`, `applySatisfactionPenalty` and `displayEndOfTurn`. After turn 18 it prints VICTORY or DEFEAT.

**`GameUI`**: handles console input and output using a `Scanner`. Displays the turn banner, food menu, messages, and Host, Waiter and Chef action menus. `readInt(prompt, min, max)` repeatedly asks until the player enters an integer within the allowed range. Throws `NoSuchElementException` when input closes. 

### Customers and tables

**`Customer`**: stores ID, satisfaction (starts at 100), state, served turn, ready-to-pay turn and order.

| Method | Effect |
|---|---|
| `seat()` | `WAITING` to `SEATED` |
| `placeOrder(order)` | `SEATED` to `ORDERED`, stores the order |
| `markServed(turn)` | `ORDERED` to `SERVED`, records the turn |
| `finishEating(turn)` | `SERVED` to `READY_TO_PAY` on a later turn; returns whether it changed |
| `markPaid()` | `READY_TO_PAY` to `PAID` |
| `applyEndOfTurnPenalty()` | Applies the customer specific penalty in `WAITING`, `SEATED` or `ORDERED`. At zero satisfaction, sets `LEFT`, cancels any order and returns `true`. |
| `restoreSatisfaction(points)` | Restores satisfaction in `WAITING`, `SEATED` or `ORDERED`, capped at 100. Returns the amount actually restored. Rejects negative points. |

**`VIPCustomer`**: extends `Customer`, loses 5 satisfaction per waiting turn and receives a 10% discount.

**`CriticCustomer`**: extends `Customer`, loses 12 satisfaction per waiting turn and chooses the last menu item.

Invalid transitions throw `IllegalStateException`, so the Waiter and Cashier check state first.

**`CustomerState`**: `WAITING`, `SEATED`, `ORDERED`, `SERVED`, `READY_TO_PAY`, `PAID`, `LEFT`.

**`Table`**: holds one customer or none. `isFree()`, `seatCustomer(customer)` (also seats the customer), `freeTable()` (only allowed once the customer is `PAID` or `LEFT`).

**`WaitingQueue`**: `addCustomer`, `findCustomer(id)`, `seatCustomer(id, table)`, `removeAbandonedCustomers()`, `size()`, and `getCustomers()` (a read-only copy).

### Food

**`Menu`**: holds four menu items; exposes `getItem(index)` and `getMenuSize()`. The UI displays each item's name, price and preparation units.

**`MenuItem`**: immutable name, price in integer cents, and preparation units. Rejects null or empty names, negative prices, and non-positive preparation units.

**`ComboMeal`**: extends `MenuItem` and combines two existing items. Charges 90% of their combined price and sums their preparation units. Reject null component items.

**`HappyHour`**: tracks once per game activation and its two turn duration. Provides the 20% order discount, 15 point satisfaction bonus and status text. The controller applies the recovery to eligible customers.

**`Order`**: stores a menu item, preparation progress, order status and a locked final price. Applies the customer's discount first, followed by the Happy Hour discount if active when the order is created. Later changes to Happy Hour do not affect existing orders. The two argument constructor creates an order without a Happy Hour discount.
Starts with zero prepared units and status `WAITING_FOR_FOOD`. `prepareOneUnit()` advances progress and flips the status to `READY` once the required units are done. `markServed()` (requires `READY`), `markPaid()` (requires `SERVED`) and `cancel()` (anything not yet `PAID`) move it along. Helpers: `isReady()`, `canPrepare()`, `getItem()`, `getPrice()`, `getPreparedUnits()`, `getStatus()`. Contains no console output.

**`OrderStatus`**: enum containing `WAITING_FOR_FOOD`, `READY`, `SERVED`, `PAID`, and `CANCELLED`.

**`Chef`**: `prepareOrder(order)` returns an `ActionResult`. On success it prepares one unit and reports progress or readiness; on failure it explains why the order cannot be prepared. The controller passes the message to the UI.

### Staff

**`Host`**: seats one queued customer at a free table per turn, or waits. Successful actions consume the Host's turn, failed attempts allow a retry.

**`Waiter`**: seats customers, takes orders, serves ready food or waits. Seating uses the Waiter action; the Host has its own separate seating action. Each action returns an `ActionResult`. The controller allowz one successful Waiter action per turn, failed attempts allow a retry.

| Method | Checks |
|---|---|
| `seatCustomer(customer, table, queue)` | Requires a queued waiting customer and a free table; seats the customer and removes them from the queue |
| `takeOrder(customer, currentTurn, happyHour)` | customer is `SEATED`; picks the dish, creates the `Order`, reports name and price |
| `serveOrder(customer, currentTurn)` | customer is `ORDERED` and the order is `READY` |
| `doNothing()` | none |

**`Cashier`**: one automatic payment per turn.

| Method | Purpose |
|---|---|
| `processPayment(turn, tables)` | charges at most one customer; returns `Optional<PaymentRecord>` |
| `previewNextPayer(turn, tables)` / `describeAvailableTask(turn, tables)` | for the start-of-turn display; does not charge |
| `getRevenueCents()`, `getPaidCount()` | running totals |
| `getSatisfactionTotal()`, `getAverageSatisfaction()` | based on satisfaction at the moment of payment; average is an empty `OptionalDouble` if nobody paid |
| `getPaymentHistory()` | read-only list of receipts |

A customer is chargeable only if they are `READY_TO_PAY`, have an order, and became ready on an earlier turn. Payment adds to revenue, marks customer and order `PAID`, and frees the table. Revenue changes nowhere else, so unpaid or abandoned orders can never be counted.

### Helpers

**`ActionResult`**: immutable result with `success(msg)` / `failure(msg)` factories, `isSuccess()` and `getMessage()`. Staff actions return failures instead of throwing, so the controller can print the reason and ask again.

**`PaymentRecord`**: immutable receipt (turn, customer, table, dish, bill in cents, satisfaction) with `describe()` for the turn log.

**`Money`**: `format(int cents)` gives `"$18.00"`. All money is stored as integer cents to avoid rounding errors.

---

## 6. Design notes

- **Separation of concerns.** `GameController` drives the turn loop, `GameUI` handles I/O, and domain classes (`Customer`, `Table`, `Order`, staff) hold their own state and rules.
- **Fail softly, not loudly.** Player mistakes return a failed `ActionResult`. Exceptions are reserved for programming errors such as illegal state transitions.
- **State machines.** `CustomerState` and `OrderStatus` represent lifecycle states. Transition checks in the domain classes and staff actions protect the normal game flow from invalid actions such as paying before service.
- **Integer money.** Prices, bills and revenue are `int` cents. `Money.format` is the only place dollars appear.
- **Timing is explicit.** "Next turn at the earliest" rules are implemented with turn comparisons (`currentTurn > servedTurn`, `readyToPayTurn < turn`) rather than by relying on call order.
- **Customer polymorphism.** `GameController` creates the customer types. Satisfaction penalties, menu selection and pricing use overridable customer methods, so the shared gameplay code works with every type through `Customer` references.

---

## 7. Assumptions and known limitations

- **"Oldest first" for payments** is read as "longest in `READY_TO_PAY`", with ties going to the lower customer ID. If the team reads it as earliest arrival, change `Cashier.isOlder`.
- **Dish choice is deterministic** (by customer ID) rather than random, which keeps runs reproducible.
- **Same-turn serving** is prevented by the turn order (Waiter before Chef), not by a separate check inside `Waiter.serveOrder`.
- **One task per turn** is enforced by the controller loop. The controller allows one successful action per Hostm Waiter and Chef each turn. Failed attempts allow a retry. The Host tracked its last successful action turn internally. The Cashier automatically processes at most one payment per turn. 
- **Locale-dependent display.** Average satisfaction may display as `79.2` or `79,2`, depending on the machine's locale.
- **Replay coverage.** The documented victory and defeat replays cover complete games but do not exhaust every possible action sequence. No permanent automated unit-test suite is included.

---

## 8. Testing and stage history

[TEST_NOTES.md](TEST_NOTES.md) contains one reproducible 18 turn victory run and one reproducible 18 turn defeat run, with step-by-step actions and verified expected results.

Stage 1 has established the base restaurant operation with regular customers. Stage 2 introduced VIP and Critic customers with different satisfaction penalties, selection rules and pricing. Stage 3 adds the Host, Combo meals and Happy Hour.

## 9. Timestamps for video recording
- segment 1: 0:06
- segment 2: 2:21
- segment 3: 3:53
- segment 4: 5:15
- segment 5: 6:51
- segment 6: 8:55
- segment 7: 10:14
