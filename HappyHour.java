public class HappyHour {
    public static final int DURATION_TURNS = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int SATISFACTION_BONUS = 15;

    // 0 = not used yet
    private int activationTurn = 0;

    public ActionResult activate(int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (activationTurn != 0) {
            return ActionResult.failure("Happy Hour has already been used this game.");
        }
        activationTurn = turn;
        return ActionResult.success("Happy Hour is ON for " + DURATION_TURNS + " turns: "
                + DISCOUNT_PERCENT + "% off new orders, waiting customers recover "
                + SATISFACTION_BONUS + " satisfaction each turn.");
    }

    public boolean isAvailable() {
        return activationTurn == 0;
    }

    public boolean isActive(int turn) {
        return activationTurn != 0
                && turn >= activationTurn
                && turn < activationTurn + DURATION_TURNS;
    }

    // Counts the given turn: 2 on the activation turn, 1 on the next, 0 otherwise
    public int getRemainingTurns(int turn) {
        return isActive(turn) ? activationTurn + DURATION_TURNS - turn : 0;
    }

    public int getSatisfactionBonus(int turn) {
        return isActive(turn) ? SATISFACTION_BONUS : 0;
    }

    // Pass the price after the customer's own discount (VIP first, then Happy Hour)
    public int applyDiscount(int priceCents, int turn) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (!isActive(turn)) {
            return priceCents;
        }
        // nearest cent, halves round up
        return (int) ((priceCents * (100 - DISCOUNT_PERCENT) + 50) / 100);
    }

    public String describeStatus(int turn) {
        if (isActive(turn)) {
            return "Happy Hour: ACTIVE, " + getRemainingTurns(turn) + " turn(s) remaining";
        }
        return isAvailable() ? "Happy Hour: available (once per game)" : "Happy Hour: already used";
    }
}
