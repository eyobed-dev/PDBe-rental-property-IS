package cz.vut.fit.pdbe.xnuriey00.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for executing SQL script files.
 * Handles Oracle-specific syntax including PL/SQL blocks with '/' delimiters.
 */
public class SQLScriptExecutor {

      /**
       * Executes a SQL script file from the resources directory.
       * 
       * @param conn       Database connection to use for execution
       * @param scriptPath Path to the SQL script in resources (e.g., "schema.sql")
       * @return List of any error messages encountered during execution
       * @throws IOException  if the script file cannot be read
       * @throws SQLException if a critical database error occurs
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
                              System.out.println("Executed: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                        } catch (SQLException e) {
                              String error = "Error executing statement: " + e.getMessage();
                              errors.add(error);
                              System.err.println(error);
                              // Continue execution even if one statement fails
                        }
                  }

                  // Commit all changes
                  conn.commit();
            }

            return errors;
      }

      /**
       * Parses a SQL script file into individual statements.
       * Handles Oracle PL/SQL blocks that are terminated with '/' on a separate line.
       * 
       * @param scriptPath Path to the SQL script in resources
       * @return List of SQL statements
       * @throws IOException if the script file cannot be read
       */
      private static List<String> parseScript(String scriptPath) throws IOException {
            List<String> statements = new ArrayList<>();
            StringBuilder currentStatement = new StringBuilder();
            boolean inPlSqlBlock = false;

            try (InputStream input = SQLScriptExecutor.class.getClassLoader().getResourceAsStream(scriptPath)) {
                  if (input == null) {
                        throw new IOException("Unable to find " + scriptPath + " in resources");
                  }

                  try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                              String trimmedLine = line.trim();

                              // Skip comments and empty lines
                              if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                                    continue;
                              }

                              // Check if we're entering a PL/SQL block
                              if (trimmedLine.toUpperCase().startsWith("BEGIN") ||
                                          trimmedLine.toUpperCase().startsWith("DECLARE")) {
                                    inPlSqlBlock = true;
                              }

                              // Check for PL/SQL block terminator
                              if (trimmedLine.equals("/")) {
                                    if (currentStatement.length() > 0) {
                                          statements.add(currentStatement.toString().trim());
                                          currentStatement = new StringBuilder();
                                    }
                                    inPlSqlBlock = false;
                                    continue;
                              }

                              // Add line to current statement
                              currentStatement.append(line).append("\n");

                              // Check for statement terminator (semicolon) if not in PL/SQL block
                              if (!inPlSqlBlock && trimmedLine.endsWith(";")) {
                                    statements.add(currentStatement.toString().trim());
                                    currentStatement = new StringBuilder();
                              }
                        }

                        // Add any remaining statement
                        if (currentStatement.length() > 0) {
                              statements.add(currentStatement.toString().trim());
                        }
                  }
            }

            return statements;
      }
}
