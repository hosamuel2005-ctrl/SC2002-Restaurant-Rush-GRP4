# Java Class Guide

## Customer.java
Represents a customer and stores their ID, satisfaction (starts at 100), and current state.

- `seat()`, `placeOrder()`, `markServed(turn)`, and `markPaid()` update the customer's state.
- `finishEating(turn)` changes the state to `READY_TO_PAY` on a later turn after serving.
- `applyEndOfTurnPenalty()` removes 8 satisfaction per turn while waiting for food. At 0, the customer becomes `LEFT`.

## CustomerState.java
An enum containing the possible customer states:
`WAITING`, `SEATED`, `ORDERED`, `SERVED`, `READY_TO_PAY`, `PAID`, and `LEFT`.

## Table.java
Stores a table ID and its current customer.

- `isFree()` checks whether the table is available.
- `seatCustomer(customer)` seats a waiting customer.
- `freeTable()` clears the table once the customer has `PAID` or `LEFT`.

## Queue.java
Stores customers who are waiting for a table.

- `addCustomer(customer)` adds a customer to the queue.
- `findCustomer(id)` finds a customer by ID.
- `seatCustomer(id, table)` seats the chosen customer and removes them from the queue.
- `removeAbandonedCustomers()` removes customers who have `LEFT`.
- `getCustomers()` and `size()` let other classes view the queue.
