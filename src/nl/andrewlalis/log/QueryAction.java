package nl.andrewlalis.log;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An action in which a query result set is returned.
 */
public class QueryAction extends ExecutionAction {

    private String[] columns;
    private String[][] values;

    public QueryAction(ResultSet resultSet) throws SQLException {
        // Read the columns into this object's memory.
        ResultSetMetaData metaData = resultSet.getMetaData();
        this.columns = new String[metaData.getColumnCount()];
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            columns[i] = metaData.getColumnName(i + 1);
        }

        resultSet.absolute(1);// Ensure that this result set cursor is at the beginning.

        // Read the rows into this object's memory.
        List<String[]> rows = new ArrayList<>();
        while (resultSet.next()) {
            String[] row = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                row[i] = resultSet.getString(i + 1);
            }
            rows.add(row);
        }
        this.values = new String[rows.size()][];
        rows.toArray(this.values);
    }

    public String[] getColumns() {
        return this.columns;
    }

    public String[][] getValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof QueryAction)) {
            return false;
        }

        QueryAction action = (QueryAction) other;

        if (action.getColumns().length != this.columns.length || action.getValues().length != this.values.length) {
            return false;
        }

        for (int i = 0; i < this.values.length; i++) {
            Map<String, String> thisColumnValues = new HashMap<>();
            Map<String, String> otherColumnValues = new HashMap<>();
            for (int k = 0; k < this.values[i].length; k++) {
                thisColumnValues.put(this.columns[k], this.values[i][k]);
                otherColumnValues.put(action.getColumns()[k], action.getValues()[i][k]);
            }
            for (String column : this.columns) {
                if (thisColumnValues.get(column).equals(otherColumnValues.get(column))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        // First build a list of columns.
        StringBuilder sb = new StringBuilder("Query Result:\n\tColumns: (");
        for (int i = 0; i < this.columns.length; i++) {
            sb.append(this.columns[i]);
            if (i < this.columns.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")\n\tValues:\n");

        // Then build a list of the rows.
        for (int i = 0; i < this.values.length; i++) {
            sb.append("\t(");
            for (int k = 0; k < this.values[i].length; k++) {
                sb.append(this.values[i][k]);
                if (k < this.values[i].length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n");
        }

        return sb.toString();
    }

}
