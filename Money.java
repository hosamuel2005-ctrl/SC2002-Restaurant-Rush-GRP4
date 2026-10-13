package restaurant.util;

/**
 * Tiny helper for the "store money as integer cents, show dollars with two
 * decimals" rule (assignment Section 2.2). Shared by anyone who prints money.
 */
public final class Money {

    private Money() {
        // static helper, no instances
    }

    /** 1800 -> "$18.00", 2430 -> "$24.30", 5 -> "$0.05". */
    public static String format(int cents) {
        String sign = cents < 0 ? "-" : "";
        int abs = Math.abs(cents);
        return String.format("%s$%d.%02d", sign, abs / 100, abs % 100);
    }
}
