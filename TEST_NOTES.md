# Stage 2 test notes

Verified on 29 October 2026 using the current source with regular, VIP and critic customers.
The two runs below exercise the real `Main` console interface for all 18 turns.
Customer food choices are deterministic; no network connection or seed is needed.
Customers 1 and 4 are regular, 2 and 5 are VIPs, and 3 and 6 are critics.

## Build and replay

Run from the project root with a JDK installed. In PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out *.java
```

After compiling, copy and run either complete PowerShell replay block below.
The inputs are embedded here, so no separate input files are needed. Each block
starts a fresh game and supplies one integer per line in prompt order. Preserve
the line breaks: the input reader discards the remainder of the line after
reading an integer.

To play manually, run `java -cp out Main` and follow the table below. Waiter
choices are 1 = Seat, 2 = Take order, 3 = Serve, 4 = Wait. Chef choices are
1 = Prepare, 2 = Wait. Seat prompts for customer ID then table ID; taking an
order, serving, and preparing each prompt for a customer ID.

## Test 1: Reproducible victory

Run this complete block in PowerShell from the project root:

```powershell
@'
1
1
1
2
2
1
1
1
3
1
2
1
2
2
2
2
2
1
2
3
2
2
1
3
1
2
2
3
1
3
4
1
3
3
3
2
1
4
2
2
2
4
1
4
3
4
2
1
5
1
2
2
5
1
5
3
5
2
1
6
2
2
2
6
1
6
'@ | java -cp out Main
```

| Turn | Waiter action | Chef action |
|---|---|---|
| 1 | Seat customer 1 at table 1 | Wait |
| 2 | Take order from customer 1 | Prepare customer 1's order |
| 3 | Serve customer 1 | Wait |
| 4 | Seat customer 2 at table 2 | Wait |
| 5 | Take order from customer 2 | Prepare customer 2's order |
| 6 | Serve customer 2 | Wait |
| 7 | Seat customer 3 at table 1 | Wait |
| 8 | Take order from customer 3 | Prepare customer 3's order |
| 9 | Wait | Prepare customer 3's order |
| 10 | Serve customer 3 | Wait |
| 11 | Seat customer 4 at table 2 | Wait |
| 12 | Take order from customer 4 | Prepare customer 4's order |
| 13 | Serve customer 4 | Wait |
| 14 | Seat customer 5 at table 1 | Wait |
| 15 | Take order from customer 5 | Prepare customer 5's order |
| 16 | Serve customer 5 | Wait |
| 17 | Seat customer 6 at table 2 | Wait |
| 18 | Take order from customer 6 | Prepare customer 6's order |

Expected final result:

```text
=== GAME OVER ===
VICTORY!
Paid customers: 5
Served customers: 5
Unsatisfied departures: 0
Revenue: $63.00
Average satisfaction: 79.8
Targets: 4 paid customers, $45.00 revenue, 60.0 average satisfaction.
```

The machine's locale may render the average as `79,8`; this is the same value.

Checkpoints:

| Customer | Dish | Served turn | Payment turn | Satisfaction at payment | Bill |
|---|---|---|---|---|---|
| 1 | Salad | 3 | 5 | 84 | $9.00 |
| 2 | Burger | 6 | 8 | 90 | $13.50 |
| 3 | Pasta | 10 | 12 | 64 | $18.00 |
| 4 | Salad | 13 | 15 | 76 | $9.00 |
| 5 | Burger | 16 | 18 | 85 | $13.50 |

Revenue is $63.00 and average satisfaction is `(84 + 90 + 64 + 76 + 85) / 5 = 79.8`.
VIP customers 2 and 5 receive a 10% discount on their Burger orders.
All three targets are met. Customer 6 arrives on turn 16 and cannot pay before
the game ends; their unpaid Pasta order contributes no revenue. Pasta for
customer 3 takes two Chef turns and is served on the turn after it becomes READY.

## Test 2: Reproducible defeat

Run this complete block in PowerShell from the project root:

```powershell
@'
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
4
2
'@ | java -cp out Main
```

On every turn from 1 through 18 choose Waiter option **4** and Chef option **2**.
The replay block contains this pair repeated 18 times. No customer is seated,
ordered for, served, or charged.

Expected final result:

```text
=== GAME OVER ===
DEFEAT!
Paid customers: 0
Served customers: 0
Unsatisfied departures: 2
Revenue: $0.00
Average satisfaction: N/A
Targets: 4 paid customers, $45.00 revenue, 60.0 average satisfaction.
```

Customer 1 leaves on turn 13 after 13 penalties of 8 satisfaction each, and
critic customer 3 leaves on turn 15 after 9 penalties of 12 satisfaction each.
The remaining customers have not reached zero satisfaction by turn 18. The game
still completes all 18 turns and fails the victory criteria.

## Verification scope

Both replays must exit normally, reach `End of turn 18`, and produce the final
statistics above. These are complete-game regression replays, not an exhaustive
test of every possible manager action. Closing input early exits cleanly but is
not a completed victory or defeat run.
