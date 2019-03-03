package nl.andrewlalis.log;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * An action in which a query result set is returned. Note that SCROLL_INSENSITIVE statements must be used, otherwise
 * an SQL exception will be thrown at each attempt to go through the result set.
 */
public class QueryAction extends ExecutionAction {

    private ResultSet resultSet;
    private boolean isOrdered;

    public QueryAction(ResultSet resultSet, boolean isOrdered) {
        this.resultSet = resultSet;
        this.isOrdered = isOrdered;
    }

    /**
     * The algorithm to determine if two query sets are equivalent is as follows:
     *      If all of the values of one column contain all of the values of another column, then these two columns must
     *      almost certainly represent the same value, even if in the wrong order.
     * @param other The other object to check equality with.
     * @return True if the two query sets are equivalent, or false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof QueryAction)) {
            return false;
        }

        QueryAction otherAction = (QueryAction) other;

        try {
            ResultSetMetaData myMetaData = this.resultSet.getMetaData();
            ResultSetMetaData theirMetaData = otherAction.resultSet.getMetaData();

            if (myMetaData.getColumnCount() != theirMetaData.getColumnCount()) {
                return false;
            }

            int columnCount = myMetaData.getColumnCount();

            List<Integer> myColumnsQueue = new ArrayList<>(columnCount);
            List<Integer> theirColumnsQueue = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                myColumnsQueue.add(i);
                theirColumnsQueue.add(i);
            }

            while (!myColumnsQueue.isEmpty()) {

                System.out.println(myColumnsQueue);
                System.out.println(theirColumnsQueue);

                // Pop the first column value.
                int myColumn = myColumnsQueue.remove(0);
                System.out.println("Testing my column " + myColumn);

                // Find a column in their columns which has the same type as this one.
                boolean columnMatchFound = false;
                int failedAttempts = 0;
                while (!theirColumnsQueue.isEmpty() && failedAttempts < myColumnsQueue.size() + 1) {
                    int theirColumn = theirColumnsQueue.remove(0);
                    System.out.println("\tWith their column " + theirColumn);
                    // Check if this column is of a compatible type.
                    if (myMetaData.getColumnType(myColumn) == theirMetaData.getColumnType(theirColumn)) {
                        System.out.println("\t\t+ Column type matches.");
                        // Now check if the data inside is a match.
                        List<String> myValues = new ArrayList<>();
                        List<String> theirValues = new ArrayList<>();
                        this.resultSet.beforeFirst();
                        otherAction.resultSet.beforeFirst();

                        // Iterate until one of the result sets goes past the last row.
                        while (true) {
                            this.resultSet.next();
                            otherAction.resultSet.next();
                            if (this.resultSet.isAfterLast() || otherAction.resultSet.isAfterLast()) {
                                break;
                            }
                            // Collect this row's values for both my column and their column.
                            myValues.add(this.resultSet.getString(myColumn));
                            theirValues.add(otherAction.resultSet.getString(theirColumn));
                        }
                        // Check if both row counts are the same; i.e. both reach the end at the same time.
                        if (this.resultSet.isAfterLast() != otherAction.resultSet.isAfterLast()) {
                            System.out.println("\t\t- Result sets have differing row counts!");
                            return false;
                        }

                        // Compare the values until an error.
                        boolean isMatch = true;
                        int index = 0;
                        for (String value : myValues) {
                            // If either query action is ordered, then require this.
                            if (this.isOrdered || otherAction.isOrdered) {
                                if (!(myValues.get(index).equals(theirValues.get(index)))) {
                                    System.out.println("\t\t- Their column (" + theirColumn + ") does not contain my column's (" + myColumn + ") value of " + value);
                                    isMatch = false;
                                    break;
                                }
                            } else {
                                if (!theirValues.contains(value)) {
                                    System.out.println("\t\t- Their column (" + theirColumn + ") does not contain my column's (" + myColumn + ") value of " + value);
                                    isMatch = false;
                                    break;
                                }
                            }
                            index++;
                        }

                        // If the column data matches.
                        if (isMatch) {
                            System.out.println("\t\t+ Columns Match! Ensuring my column " + myColumn +" and their column " + theirColumn + " are not added back to the queues.");
                            // Leave the loop knowing we found a column, without adding it back.
                            columnMatchFound = true;
                            break;
                        } else {
                            System.out.println("\t\t- Columns do not match.");
                            // No column was found to match, so add it back to their queue.
                            theirColumnsQueue.add(theirColumn);
                            failedAttempts++;
                        }
                    } else {
                        System.out.println("\t\t- Column types do not match.");
                        theirColumnsQueue.add(theirColumn);
                        failedAttempts++;
                    }
                }

                // columnMatchFound == true when a column match has been found somewhere in other result set.
                if (!columnMatchFound) {
                    System.out.println("\t\t- Could not find a matching column for my column " + myColumn);
                    return false;
                }
            }

            // If we manage to get to the end without failing, return true.
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Translates the result set into a listing of column names and queries.
     * @return The string representation of this QueryAction.
     */
    @Override
    public String toString() {
        try {
            this.resultSet.absolute(1);
            ResultSetMetaData metaData = this.resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            StringBuilder sb = new StringBuilder("Query Result:\n\tColumns: (");
            for (int i = 0; i < columnCount; i++) {
                sb.append(metaData.getColumnName(i + 1));
                if (i < columnCount - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n\tValues:\n");

            do {
                sb.append("\t(");
                for (int i = 0; i < columnCount; i++) {
                    sb.append(this.resultSet.getString(i + 1));
                    if (i < columnCount - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")\n");
            } while (this.resultSet.next());

            return sb.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLException; Please use a SCROLL_INSENSITIVE statement when executing the query.";
        }
    }

}
