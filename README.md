# Restaurant Rush (SC2002 Group 4)

A turn-based, command-line restaurant management game written in Java. You play the manager: each turn you tell the **Waiter** and the **Chef** what to do, the **Cashier** collects payments automatically, and customers lose patience the longer they wait. Survive 18 turns and hit all three targets to win.

**Current status:** Stage 1 (base setting) only.

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

| Item | Stage 1 value |
|---|---|
| Turns | 18 |
| Tables | 2 (one customer per table) |
| Staff | 1 Waiter, 1 Chef, 1 Cashier |
| Customers | 6 in total, arriving on turns 1, 4, 7, 10, 13, 16 (customer IDs 1 to 6) |
| Starting satisfaction | 100 per customer |

### Menu

| Dish | Price | Preparation units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Customers choose their own food. In this implementation the choice is deterministic: customer *n* orders menu item `(n - 1) mod 3`, so customers 1 to 6 order Salad, Burger, Pasta, Salad, Burger, Pasta.

### Win condition

Checked after turn 18. You win only if **all** of these hold:

- at least **4** customers have paid,
- revenue is at least **$45.00**,
- the average satisfaction of paying customers (recorded at the moment each paid) is at least **60**.

If nobody has paid, average satisfaction is shown as **N/A** and the game is lost.

### Turn order

Each turn runs these steps in order:

1. **Arrivals**: a scheduled customer joins the waiting queue.
2. **Waiter**: the manager picks one action (seat, take order, serve, or wait).
3. **Chef**: the manager picks one action (prepare one unit of an order, or wait).
4. **Eating and payment**: customers who were served on an earlier turn finish eating and become ready to pay, then the Cashier charges at most one customer.
5. **Satisfaction penalty**: every customer still waiting for a table, an order taker or food loses 8 satisfaction. At 0 they leave, their order is cancelled and their table is freed.
6. **End-of-turn summary**: served, unsatisfied departures, paid count, revenue, average satisfaction.

### Timing rules

- The Waiter acts before the Chef, so a dish finished this turn can be served **next turn at the earliest**.
- A customer served on turn *t* finishes eating on turn *t + 1* and is charged on turn *t + 2* at the earliest.
- An invalid choice (for example seating at an occupied table) prints why it failed and lets you choose again. It does not use up the task.
- The Cashier charges at most one customer per turn. If several are ready, the one who has been ready longest goes first (ties go to the lower customer ID).

### Customer lifecycle

```
WAITING -> SEATED -> ORDERED -> SERVED -> READY_TO_PAY -> PAID
   \          \          \
    +----------+----------+----> LEFT   (satisfaction reached 0)
```

Satisfaction drops by 8 per turn in `WAITING`, `SEATED` and `ORDERED`. It stops dropping once the food is served, so a customer who is served cannot walk out. Starting from 100, a customer who is never helped leaves after 13 turns.

### Order lifecycle

```
WAITING_FOR_FOOD -> READY -> SERVED -> PAID
        \
         +--> CANCELLED   (customer left before payment)
```

---

## 3. How to play

Each turn the game prints the current state: turn number, revenue, average satisfaction, the waiting queue, both tables with their customer and order progress, and the tasks currently available. Then it prompts you.

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

- Seat and take orders early. Satisfaction starts draining the moment a customer arrives.
- Pasta takes two Chef turns, so start it as soon as it is ordered.
- Only 2 tables and 1 task per staff member per turn means you cannot serve everyone. The target needs only 4 paid customers out of 6, so choose whom to prioritise.

---

## 4. Project structure

```
.
├── Main.java             # entry point
├── GameController.java   # game loop, turn steps, win/lose check
├── GameUI.java           # console input and output
│
├── Customer.java         # customer state, satisfaction, state transitions
├── CustomerState.java    # enum of customer states
├── Table.java            # single-customer table
├── WaitingQueue.java     # customers waiting for a table
│
├── Menu.java             # the three Stage 1 dishes
├── MenuItem.java         # name, price (cents), preparation units
├── Order.java            # preparation progress and order status
├── OrderStatus.java      # enum of order states
│
├── Waiter.java           # seat / take order / serve / wait
├── Chef.java             # prepares one unit of an order
├── Cashier.java          # automatic payment, revenue, satisfaction totals
├── ActionResult.java     # success/failure + message from staff actions
├── PaymentRecord.java    # immutable receipt for one payment
├── Money.java            # cents -> "$18.00"
└── TEST_NOTES.md         # embedded replay inputs and expected Stage 1 results
```

---

## 5. Class reference

### Game flow

**`Main`**: creates a `GameUI` and a `GameController`, then calls `startGame()`. Handles closed input and displays a clean exit message through the UI.

**`GameController`**: owns the menu, queue, two tables, customer list and the three staff members, and runs the 18-turn loop. Game constants are `MAX_TURNS = 18`, `TARGET_PAID = 4`, `TARGET_REVENUE = 4500` (cents) and `TARGET_SATISFACTION = 60.0`. Each turn it calls `handleArrivals`, `displayGameState`, `handleWaiterAction`, `handleChefAction`, `advanceEating`, `processPayment`, `applySatisfactionPenalty` and `displayEndOfTurn`. After turn 18 it prints VICTORY or DEFEAT.

**`GameUI`**: wraps a `Scanner`. `readInt(prompt, min, max)` loops until the player enters a valid integer in range, or throws `NoSuchElementException` when input closes. It displays messages, the turn banner, the food menu through `showMenu(menu)`, and the Waiter and Chef menus. Console input and output are kept here.

### Customers and tables

**`Customer`**: stores ID, satisfaction (starts at 100), state, served turn, ready-to-pay turn and order.

| Method | Effect |
|---|---|
| `seat()` | `WAITING` to `SEATED` |
| `placeOrder(order)` | `SEATED` to `ORDERED`, stores the order |
| `markServed(turn)` | `ORDERED` to `SERVED`, records the turn |
| `finishEating(turn)` | `SERVED` to `READY_TO_PAY` on a later turn; returns whether it changed |
| `markPaid()` | `READY_TO_PAY` to `PAID` |
| `applyEndOfTurnPenalty()` | subtracts 8 while waiting; at 0 sets `LEFT`, cancels the order, returns `true` |

Invalid transitions throw `IllegalStateException`, so the Waiter and Cashier check state first.

**`CustomerState`**: `WAITING`, `SEATED`, `ORDERED`, `SERVED`, `READY_TO_PAY`, `PAID`, `LEFT`.

**`Table`**: holds one customer or none. `isFree()`, `seatCustomer(customer)` (also seats the customer), `freeTable()` (only allowed once the customer is `PAID` or `LEFT`).

**`WaitingQueue`**: `addCustomer`, `findCustomer(id)`, `seatCustomer(id, table)`, `removeAbandonedCustomers()`, `size()`, and `getCustomers()` (a read-only copy).

### Food

**`Menu`**: holds the three dishes; exposes `getItem(index)` and `getMenuSize()`. The UI displays the menu.

**`MenuItem`**: immutable name, price in integer cents, and preparation units. Rejects null or empty names, negative prices, and non-positive preparation units.

**`Order`**: holds a non-null, final menu item and starts at 0 prepared units with status `WAITING_FOR_FOOD`. `prepareOneUnit()` advances progress and flips the status to `READY` once the required units are done. `markServed()` (requires `READY`), `markPaid()` (requires `SERVED`) and `cancel()` (anything not yet `PAID`) move it along. Helpers: `isReady()`, `canPrepare()`, `getItem()`, `getPrice()`, `getPreparedUnits()`, `getStatus()`. Contains no console output.

**`OrderStatus`**: enum containing `WAITING_FOR_FOOD`, `READY`, `SERVED`, `PAID`, and `CANCELLED`.

**`Chef`**: `prepareOrder(order)` returns an `ActionResult`. On success it prepares one unit and reports progress or readiness; on failure it explains why the order cannot be prepared. The controller passes the message to the UI.

### Staff

**`Waiter`**: every method returns an `ActionResult`.

| Method | Checks |
|---|---|
| `seatCustomer(customer, table, queue)` | customer is in the queue; table exists and is free |
| `takeOrder(customer)` | customer is `SEATED`; picks the dish, creates the `Order`, reports name and price |
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

---

## 7. Assumptions and known limitations

- **"Oldest first" for payments** is read as "longest in `READY_TO_PAY`", with ties going to the lower customer ID. If the team reads it as earliest arrival, change `Cashier.isOlder`.
- **Dish choice is deterministic** (by customer ID) rather than random, which keeps runs reproducible.
- **Same-turn serving** is prevented by the turn order (Waiter before Chef), not by a separate check inside `Waiter.serveOrder`.
- **One task per turn** is enforced by the controller loop, which returns after the first successful Waiter or Chef action. The `Waiter` class itself does not track it.
- **Locale-dependent display.** Average satisfaction may display as `79.2` or `79,2`, depending on the machine's locale.
- **Replay coverage.** The documented victory and defeat replays cover complete games but do not exhaust every possible action sequence. No permanent automated unit-test suite is included.
- Only Stage 1 is implemented.

---

## 8. Testing and stage history

[TEST_NOTES.md](TEST_NOTES.md) contains a complete 18-turn victory strategy, a defeat strategy, expected results, and PowerShell replay blocks with all inputs embedded directly in the document. Neither run needs separate input files, network access, or random seeds.

Stage 1 includes only regular customers and the base restaurant operation. The `stage-1` tag should identify its verified runnable commit before Stage 2 work begins. Stage 2 and Stage 3 are not implemented yet; document each transition here with new/changed/deleted files, reasons, and commit or compare links when those stages are completed.
