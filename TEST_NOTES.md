# Stage 3 test notes

Verified on 10 November 2026 against the current Java source. Both replays
below were run through the real `Main` console interface for all 18 turns.
They use deterministic customer choices and need no external libraries or seeds.

## How to reproduce the runs

Start a fresh game using the README quick-start instructions for each run.
Enter each menu choice and requested ID separately, pressing Enter after each
value. Follow the victory table in turn order, or repeat the defeat choices
for all 18 turns.

| Prompt | Choices and additional inputs |
|---|---|
| Happy Hour | 1 = Activate, 2 = Decline; appears each turn until activated, then never again |
| Host | 1 = Seat (customer ID, table ID), 2 = Wait |
| Waiter | 1 = Take order (customer ID), 2 = Serve (customer ID), 3 = Wait |
| Chef | 1 = Prepare (customer ID), 2 = Wait |

Prompt order is Happy Hour (if available), Host, Waiter, Chef. Eating and payment
follow automatically. Happy Hour recovery occurs before the normal penalty.
Average satisfaction may use a decimal comma instead of a dot, depending on
the machine's locale; 80,8 and 80.8 represent the same value.

Customer arrivals are unchanged: IDs 1–6 arrive on turns 1, 4, 7, 10, 13 and 16.
IDs 1 and 4 are regular, 2 and 5 are VIPs, and 3 and 6 are Critics.
With four menu items, their choices are Salad, Burger, Combo, Combo, Salad and
Combo. The Salad + Pasta Combo costs $24.30 and needs three preparation units.

## Test 1: Victory with Host, combos and Happy Hour pricing

Activate Happy Hour on turn 5. It is active on turns 5 and 6 and expires on
turn 7. The VIP Burger ordered on turn 5 costs $15.00 × 0.90 × 0.80 = $10.80.
It is paid on turn 8, after Happy Hour expires, proving that its price stays locked.

| Turn | Happy Hour choice | Host action | Waiter action | Chef action |
|---|---|---|---|---|
| 1 | Decline | Seat 1 at table 1 | Order for 1 | Prepare 1 |
| 2 | Decline | Wait | Serve 1 | Wait |
| 3 | Decline | Wait | Wait | Wait |
| 4 | Decline | Seat 2 at table 2 | Wait | Wait |
| 5 | Activate | Wait | Order for 2 | Prepare 2 |
| 6 | No prompt | Wait | Serve 2 | Wait |
| 7 | No prompt | Seat 3 at table 1 | Order for 3 | Prepare 3 |
| 8 | No prompt | Wait | Wait | Prepare 3 |
| 9 | No prompt | Wait | Wait | Prepare 3 |
| 10 | No prompt | Seat 4 at table 2 | Serve 3 | Wait |
| 11 | No prompt | Wait | Order for 4 | Prepare 4 |
| 12 | No prompt | Wait | Wait | Prepare 4 |
| 13 | No prompt | Seat 5 at table 1 | Wait | Prepare 4 |
| 14 | No prompt | Wait | Serve 4 | Wait |
| 15 | No prompt | Wait | Order for 5 | Prepare 5 |
| 16 | No prompt | Wait | Serve 5 | Wait |
| 17 | No prompt | Seat 6 at table 2 | Order for 6 | Prepare 6 |
| 18 | No prompt | Wait | Wait | Prepare 6 |

Expected final result:

```text
=== GAME OVER ===
VICTORY!
Paid customers: 5
Served customers: 5
Unsatisfied departures: 0
Revenue: $76.50
Average satisfaction: 80.8
Targets: 4 paid customers, $45.00 revenue, 60.0 average satisfaction.
```

Verified payment checkpoints:

| Customer | Dish | Order turn | Preparation turns | Served turn | Payment turn | Satisfaction at payment | Bill |
|---|---|---|---|---|---|---|---|
| 1 | Salad | 1 | 1 | 2 | 4 | 92 | $9.00 |
| 2 | Burger | 5 | 5 | 6 | 8 | 95 | $10.80 |
| 3 | Salad + Pasta Combo | 7 | 7, 8, 9 | 10 | 12 | 64 | $24.30 |
| 4 | Salad + Pasta Combo | 11 | 11, 12, 13 | 14 | 16 | 68 | $24.30 |
| 5 | Salad | 15 | 15 | 16 | 18 | 85 | $8.10 |

Revenue is $9.00 + $10.80 + $24.30 + $24.30 + $8.10 = $76.50.
Average satisfaction is (92 + 95 + 64 + 68 + 85) / 5 = 80.8.

Additional checks:

- On turn 1, the Host seats customer 1, the Waiter takes their order, and the
  Chef prepares it. Each staff member has a separate action.
- Customer 2 ends turn 4 at 95 satisfaction. On turn 5, recovery caps it at
  100 before the VIP penalty brings it to 95 again. Serving on turn 6 stops
  further penalties and recovery; payment records 95.
- The turn 7 combo order is charged at $24.30, without a Happy Hour discount.
- Each paid combo needs three Chef actions and is served on a later turn.
- Customer 5 receives only the VIP discount: $9.00 × 0.90 = $8.10.
- Customer 4 pays at the end of turn 16, so table 2 can next be used by the
  Host on turn 17, not during turn 16.
- Customer 6 ends turn 18 still `ORDERED`, with preparation progress 2/3 and
  satisfaction 64. Their unpaid $24.30 order contributes no revenue.

## Test 2: Defeat without Happy Hour

Decline Happy Hour and make every staff member wait on all 18 turns.
On every turn from 1 through 18, enter these choices in order:

| Prompt | Choice |
|---|---|
| Happy Hour | 2 - Decline |
| Host | 2 - Wait |
| Waiter | 3 - Wait |
| Chef | 2 - Wait |

No customer or table IDs are requested in this run.

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

Customer 1 leaves on turn 13 after 13 penalties of 8. Critic customer 3 leaves
on turn 15 after 9 penalties of 12. The other customers remain waiting through
turn 18. No customer is seated, ordered for, served or charged. Happy Hour
remains available throughout the game.

## Verification scope

Both replays were compiled and run against the current source. Each must
exit normally, reach `End of turn 18`, and print exactly one `GAME OVER` result
with the statistics above. No replay should report invalid input, a failed
staff action, an exception or `Input closed. Exiting the game.`

These runs cover complete victory and defeat games, separate Host actions,
regular/VIP/Critic selection, combo preparation, Happy Hour pricing and
recovery, capped satisfaction, stacked discounts, locked prices, payment timing,
and exclusion of unpaid orders from revenue.

They do not exhaustively test invalid-action retries, direct API rejection of
a second Happy Hour activation, arbitrary price rounding, all customer-state
transitions or all possible manager strategies. Early input closure is a clean
exit, not a completed replay.
