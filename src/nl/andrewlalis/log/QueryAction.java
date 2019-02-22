package nl.andrewlalis.log;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * An action in which a query result set is returned. Note that SCROLL_INSENSITIVE statements must be used, otherwise
 * an SQL exception will be thrown at each attempt to go through the result set.
 */
public class QueryAction extends ExecutionAction {

    private ResultSet resultSet;

    public QueryAction(ResultSet resultSet) {
        this.resultSet = resultSet;
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

        return true;
    }

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

            while (this.resultSet.next()) {
                sb.append("\t(");
                for (int i = 0; i < columnCount; i++) {
                    sb.append(this.resultSet.getString(i + 1));
                    if (i < columnCount - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")\n");
            }

            return sb.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLException; Please use a SCROLL_INSENSITIVE statement when executing the query.";
        }
    }

}
