package nl.andrewlalis;

import nl.andrewlalis.log.ExecutionLog;
import nl.andrewlalis.log.QueryAction;
import nl.andrewlalis.log.UpdateAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private String host;
    private int port;
    private String user;
    private String password;
    private Window window;

    private ExecutionLog executionLog;

    public DatabaseHelper(String host, int port, String user, String password, Window window) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.window = window;

        this.executionLog = new ExecutionLog();
    }

    /**
     * Executes possibly many queries which are contained in one string.
     * @param database The database name to connect to, or an empty string to connect to the user's database.
     * @param queriesString The string of queries.
     */
    public void executeQueries(String database, String queriesString) {
        String url = String.format(
                "jdbc:postgresql://%s:%4d/%s?user=%s&password=%s",
                host,
                port,
                database,
                user,
                password);
        try {
            Connection conn = DriverManager.getConnection(url);

            if (!conn.isValid(1000)) {
                throw new SQLException("Invalid connection.");
            }

            List<String> queries = splitQueries(queriesString);

            Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            for (String query : queries) {
                try {
                    executeQuery(query, st);
                } catch (SQLException e) {
                    window.appendOutput("Exception while executing statement: " + e.getMessage());
                }
            }

            conn.close();
        } catch (SQLException e) {
            int previousChannel = window.getOutputChannel();
            window.setOutputChannel(Window.OUTPUT_GENERAL);
            window.appendOutput("Unexpected SQL Exception occurred. URL:\n" + url + "\n\tException: " + e.getMessage() + "\n\tSQL State: " + e.getSQLState());
            window.setOutputChannel(previousChannel);
        }
    }

    /**
     * Executes a single query and outputs the results.
     * @param query The query to execute. Must be only one query in the string.
     * @param statement The statement used to execute the query.
     */
    private void executeQuery(String query, Statement statement) throws SQLException {
        if (isSQLStatementQuery(query)) {
            // A result set is expected.
            window.appendOutput("Executing query:\n" + query);

            QueryAction action = new QueryAction(statement.executeQuery(query));
            window.appendOutput(action.toString());
            this.executionLog.recordAction(action);
        } else {
            // A result set is not expected.
            window.appendOutput("Executing update:\n" + query);
            UpdateAction action = new UpdateAction(statement.executeUpdate(query), query);
            window.appendOutput(action.toString());
            this.executionLog.recordAction(action);
        }
    }

    /**
     * Splits and cleans each query so that it will run properly.
     * @param queriesString A string containing one or more queries to execute.
     * @return A list of individual queries.
     */
    private static List<String> splitQueries(String queriesString) {
        String[] sections = queriesString.split(";");
        List<String> strings = new ArrayList<>();

        for (String section : sections) {
            String s = section.trim();
            if (!s.isEmpty()) {
                strings.add(s);
            }
        }

        return strings;
    }

    /**
     * Determines if an SQL string is a query (it should return a result set)
     * @param str The string to check.
     * @return True if this is a query, or false if it is an update.
     */
    private static boolean isSQLStatementQuery(String str) {
        String upper = str.toUpperCase();
        return upper.startsWith("SELECT");
    }

}
