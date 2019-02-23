package nl.andrewlalis.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a log of all actions performed to a database.
 */
public class ExecutionLog {

    private List<ExecutionAction> actions;

    public ExecutionLog() {
        this.actions = new ArrayList<>();
    }

    public void recordAction(ExecutionAction action) {
        this.actions.add(action);
    }

    private List<ExecutionAction> getActions() {
        return this.actions;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ExecutionLog)) {
            return false;
        }

        ExecutionLog otherLog = (ExecutionLog) other;

        if (otherLog.getActions().size() != this.getActions().size()) {
            System.out.println("Size difference in logs.");
            return false;
        }

        List<ExecutionAction> otherLogActions = otherLog.getActions();

        for (int i = 0; i < this.getActions().size(); i++) {
            ExecutionAction myAction = this.getActions().get(i);
            ExecutionAction theirAction = otherLogActions.get(i);
            System.out.println("My action: " + myAction + "\nTheir action: " + theirAction);
            System.out.println("\tEqual? " + myAction.equals(theirAction));
            if (!myAction.equals(theirAction)) {
                return false;
            }
        }

        return true;
    }
}
