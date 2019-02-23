package nl.andrewlalis;

import nl.andrewlalis.log.ExecutionAction;
import nl.andrewlalis.log.ExecutionLog;
import nl.andrewlalis.log.QueryAction;
import nl.andrewlalis.log.UpdateAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static nl.andrewlalis.Window.*;

class DatabaseHelper {

    private String host;
    private int port;
    private String user;
    private String password;
    private Window window;

    DatabaseHelper(String host, int port, String user, String password, Window window) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.window = window;
    }

    void executeSQLComparison(String initializationSQL, String templateSQL, String testingSQL) {
        // Run the database code in a separate thread to update the UI quickly.
        Thread t = new Thread(() -> {
            // Setup both databases.
            this.window.appendOutput("Dropping old databases and re-creating them...");
            this.window.indentOutput();
            String dropDatabases = "DROP DATABASE " + DB_TEMPLATE + "; " +
                    "DROP DATABASE " + DB_TESTING + ";";
            String createDatabases = "CREATE DATABASE " + DB_TEMPLATE + "; " +
                    "CREATE DATABASE " + DB_TESTING + ";";
            this.executeQueries("", dropDatabases, false);
            this.executeQueries("", createDatabases, false);
            this.window.unindentOutput();

            // Run initialization script on each database.
            this.window.appendOutput("Running initialization SQL on databases...");
            this.window.indentOutput();
            this.executeQueries(DB_TEMPLATE, initializationSQL, false);
            this.executeQueries(DB_TESTING, initializationSQL, false);
            this.window.unindentOutput();

            // TESTING SQL HERE

            // Template-specific output.
            this.window.setOutputChannel(OUTPUT_TEMPLATE);
            ExecutionLog templateLog = this.executeQueries(DB_TEMPLATE, templateSQL, true);

            // Testing-specific output.
            this.window.setOutputChannel(OUTPUT_TESTING);
            ExecutionLog testingLog = this.executeQueries(DB_TESTING, testingSQL, true);

            // Output results.
            this.window.setOutputChannel(OUTPUT_GENERAL);
            this.window.appendOutput("Execution test result: " + templateLog.equals(testingLog));
        });
        t.start();
    }

//    private void listDatabases() {
//        try {
//            PreparedStatement ps = connection
//                    .prepareStatement("SELECT datname FROM pg_database WHERE datistemplate = false;");
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                System.out.println(rs.getString(1));
//            }
//            rs.close();
//            ps.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Executes possibly many queries which are contained in one string.
     * @param database The database name to connect to, or an empty string to connect to the user's database.
     * @param queriesString The string of queries.
     * @param safe Whether the queries should be checked for safety.
     * @return The execution log from this series of queries.
     */
    private ExecutionLog executeQueries(String database, String queriesString, boolean safe) {
        ExecutionLog executionLog = new ExecutionLog();
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
                    if (!safe || isQuerySafe(query)) {
                        executionLog.recordAction(executeQuery(query, st));
                    } else {
                        window.appendOutput("Blocked execution of unsafe query: " + query);
                    }
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

        return executionLog;
    }

    /**
     * Executes a single query and outputs the results.
     * @param query The query to execute. Must be only one query in the string.
     * @param statement The statement used to execute the query.
     * @return The execution action which was done by executing this query.
     */
    private ExecutionAction executeQuery(String query, Statement statement) throws SQLException {
        if (isSQLStatementQuery(query)) {
            // A result set is expected.
            window.appendOutput("Executing query:\n" + query);

            QueryAction action = new QueryAction(statement.executeQuery(query), isQueryOrdered(query));
            window.appendOutput(action.toString());
            return action;
        } else {
            // A result set is not expected.
            window.appendOutput("Executing update:\n" + query);
            UpdateAction action = new UpdateAction(statement.executeUpdate(query), query);
            window.appendOutput(action.toString());
            return action;
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
     * Determines if the given query is safe to run.
     * @param query The query to run.
     * @return True if this query is safe, or false if it would cause damage to the system.
     */
    private static boolean isQuerySafe(String query) {
        String upper = query.trim().toUpperCase();
        return !upper.startsWith("CREATE DATABASE")
                && !upper.startsWith("DROP DATABASE");
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

    /**
     * Determines if a query is ordered by something.
     * @param query The query to check.
     * @return True if the query makes use of the 'ORDER BY' clause.
     */
    private static boolean isQueryOrdered(String query) {
        return query.toUpperCase().contains("ORDER BY");
    }

}
