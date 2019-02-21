package nl.andrewlalis.log;

/**
 * Represents an action in which the schema or data was updated and no result set was returned.
 */
public class UpdateAction extends ExecutionAction {

    private int rowsAffected;

    public UpdateAction(int rowsAffected, String statement) {
        this.rowsAffected = rowsAffected;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UpdateAction)) {
            return false;
        }

        UpdateAction action = (UpdateAction) other;

        return action.rowsAffected == this.rowsAffected;
    }

    @Override
    public String toString() {
        return "Update result:\n\tRows affected: " + this.rowsAffected + "\n";
    }
}
