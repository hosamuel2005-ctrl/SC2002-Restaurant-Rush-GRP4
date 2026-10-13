package restaurant.staff;

/**
 * Outcome of a manager-chosen staff action.
 *
 * The assignment says an invalid choice must "explain why it failed and let
 * the manager choose again, without using up the staff task" (Section 3.4).
 * Staff methods therefore never throw for a bad choice; they return a failed
 * ActionResult whose message the CLI can print as-is.
 */
public final class ActionResult {

    private final boolean success;
    private final String message;

    private ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResult success(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult failure(String message) {
        return new ActionResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return (success ? "OK: " : "FAILED: ") + message;
    }
}
