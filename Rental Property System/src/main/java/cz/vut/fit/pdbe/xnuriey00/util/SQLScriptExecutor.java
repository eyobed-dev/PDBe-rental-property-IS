package cz.vut.fit.pdbe.xnuriey00.util;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for executing SQL script files.
 * @author Eyobed
 * 
 * It is used to execute SQL scripts from the resources directory.
 * Specific to the Oracle database and this project.
 */
public class SQLScriptExecutor {

    /**
     * Executes a SQL script file from the resources directory.
     * This method automatically commits the transaction if all statements are processed
     * (even if some fail). "Table not found" errors during DROP statements are ignored.
     *
     * @param conn       Database connection
     * @param scriptPath Path to the SQL script
     * @return List of error messages (excluding ignored DROP errors) to display to the user
     * @throws IOException  if the script file cannot be found or read
     * @throws SQLException if a database access error occurs
     */
    public static List<String> executeScript(Connection conn, String scriptPath) throws IOException, SQLException {
        List<String> errors = new ArrayList<>();
        List<String> statements = parseScript(scriptPath);

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                if (sql.trim().isEmpty()) {
                    continue;
                }

                try {
                    stmt.execute(sql);
                    // Log success trimmed out 50 characters and replaced new lines with spaces
                    System.out.println("Executed: " + sql.substring(0, Math.min(50, sql.length())).replace("\n", " ") + "...");
                } catch (SQLException e) {
                    // IGNORE ORA-00942: "Table or view does not exist"
                    if (e.getErrorCode() == 942 && sql.trim().toUpperCase().startsWith("DROP")) {
                        System.out.println("Notice: Target table did not exist (skipping DROP statement). ");
                    } else {
                        // Capture actual errors
                        String error = "Error executing statement: " + e.getMessage();
                        errors.add(error);
                        System.err.println(error + "\nSQL: " + sql);
                    }
                }
            }
            // Commit all successful changes
            conn.commit();
        }
        return errors;
    }

    /**
     * Parses a SQL script file into individual executable statements.
     *
     * @param scriptPath Path to the SQL script
     * @return List of clean SQL statements
     * @throws IOException if the script file cannot be read
     * @throws FileNotFoundException if the file does not exist
     */
    private static List<String> parseScript(String scriptPath) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        // Flag to track if we are inside a PL/SQL block
        boolean inPlSqlBlock = false;

        // Load from classpath (src/main/resources)
        try (InputStream input = SQLScriptExecutor.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (input == null) {
                throw new FileNotFoundException("SQL Script not found in resources: " + scriptPath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();

                    // Skip comments and empty lines
                    if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                        continue;
                    }

                    // Detect start of PL/SQL Block
                    // We must catch "CREATE OR REPLACE TRIGGER" or "DECLARE" or "BEGIN"
                    String upper = trimmedLine.toUpperCase();
                    if (upper.startsWith("DECLARE") ||
                            upper.startsWith("BEGIN") ||
                            upper.startsWith("CREATE OR REPLACE TRIGGER") ||
                            upper.startsWith("CREATE TRIGGER")) {
                        inPlSqlBlock = true;
                    }

                    // Check for PL/SQL block terminator (Slash on its own line)
                    if (trimmedLine.equals("/")) {
                        if (currentStatement.length() > 0) {
                            statements.add(currentStatement.toString().trim());
                            currentStatement = new StringBuilder();
                        }
                        inPlSqlBlock = false;
                        continue;
                    }

                    // Handle Standard SQL (Semicolon termination)
                    // We must remove the semicolon for JDBC execution
                    if (!inPlSqlBlock && trimmedLine.endsWith(";")) {
                        // Append everything BEFORE the last semicolon
                        currentStatement.append(line.substring(0, line.lastIndexOf(";")));
                        statements.add(currentStatement.toString().trim());
                        currentStatement = new StringBuilder();
                    } else {
                        // Append line as-is like queries or PL/SQL blocks
                        currentStatement.append(line).append(" ");
                    }
                }

                // Add any thing else
                if (currentStatement.length() > 0) {
                    statements.add(currentStatement.toString().trim());
                }
            }
        }
        return statements;
    }
}