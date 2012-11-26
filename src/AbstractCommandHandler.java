/*****************************************************************************************
 * 
 * AbstractCommandHandler.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * AbstractCommandHandler.java is a useful base class
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AbstractCommandHandler {

  protected Connection connection;
  
  private static int STATEMENT_TIMEOUT = 10;
  
  /**
   * Construct a command handler.
   */
  public AbstractCommandHandler(Connection connection) {
    this.connection = connection;
  }
  
  protected void exitProgram(String error) {
    System.out.println(error);
    System.exit(-1);
  }
  
  /**
   * Create a statement and set the default timeout.
   */
  protected Statement createStatement() throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(STATEMENT_TIMEOUT);
    return statement;
  }
  
  /**
   * Create a prepared statement and set the default timeout.
   */
  protected PreparedStatement prepareStatement(String sql) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setQueryTimeout(STATEMENT_TIMEOUT);
    return statement;
  }
  
  /**
   * Delete a row in the database from the specified table. Returns the count of rows deleted.
   *
   * @param table
   *   The table to delete from
   * @param id
   *   The row id to delete
   * @return The number of rows deleted
   */
  protected int deleteRow(String table, int id) throws SQLException {
    // Construct the delete statement
    String sql = "DELETE FROM " + table + " WHERE id="+id;
    
    // Execute and return number of rows deleted
    return createStatement().executeUpdate(sql);  
  }
  
  /**
   * Insert a row in the specified table.
   *
   * @param table
   *   The table to insert into
   * @param idColumn
   *   The name of the column that the has the row id
   * @param defaultId
   *   The default starting id if no rows exist
   * @param params
   *   A map of {column name, value} of values to insert for the row
   * @return The id of the newly inserted record.
   */
  protected int insertRow(String table, String idColumn, int defaultId, Map<String, Object> params) throws SQLException {
    return insertRow(table, idColumn, defaultId, params, false);
  }
  
  /**
   * Insert a row in the specified table.
   *
   * @param table
   *   The table to insert into
   * @param idColumn
   *   The name of the column that the has the row id
   * @param defaultId
   *   The default starting id if no rows exist
   * @param params
   *   A map of {column name, value} of values to insert for the row
   * @param insideTransaction
   *   If true, no transaction calls are made.
   * @return The id of the newly inserted record.
   */
  protected int insertRow(String table, String idColumn, int defaultId, Map<String, Object> params, boolean insideTransaction) throws SQLException {
  
    if (!insideTransaction) {
      // Set the transaction isolation to prevent row duplicates
      connection.setAutoCommit(false);
      connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    // Get the last ID assigned and add one to it to create a new ID for this row
    String sql = "SELECT MAX(" + idColumn + ") AS max FROM " + table;

    Statement statement = createStatement();
    ResultSet result = statement.executeQuery(sql);

    int newID = defaultId;
    if (result.next()) {
      newID = result.getInt("max");
      newID++;
    }

    // Collect the column names and parameter placeholders
    String columns = idColumn;
    String values = "?";
    for (Map.Entry<String, Object> column : params.entrySet()) {
      columns += "," + column.getKey();
      values += ",?";
    }
    
    // Prepare the insert statement
    sql = "INSERT INTO " + table + " ("+ columns + ") VALUES (" + values + ")";
    PreparedStatement insertStatement = prepareStatement(sql);
    
    // Specify parameter values for insert statement
    insertStatement.setInt(1, newID);
    int pos = 2;
    for (Map.Entry<String, Object> column : params.entrySet()) {
      insertStatement.setObject(pos, column.getValue());
      pos++;
    }
    
    // Execute that sucker!
    insertStatement.executeUpdate();
    
    if (!insideTransaction) {
      connection.commit();
    }
    
    return newID;
  }
  
  /**
   * Update a row in the specified table.
   *
   * @param table
   *   The table to insert into
   * @param idColumn
   *   The name of the column that the has the row id
   * @param id
   *   The id of the row to update
   * @param params
   *   A map of {column name, value} of values to insert for the row
   * @return A count of the rows updated.
   */
  protected int updateRow(String table, String idColumn, int id, Map<String, Object> params) throws SQLException {
    // Collect the column names and parameter placeholders
    String update = "";
    for (Map.Entry<String, Object> column : params.entrySet()) {
      if (update.length() > 0) update += ",";
      update += column.getKey() + "=?";
    }
    
    // Prepare the insert statement
    String sql = "UPDATE " + table + " SET " + update + " WHERE " + idColumn + "=" + id;
    PreparedStatement updateStatement = prepareStatement(sql);
    
    // Specify parameter values for insert statement
    int pos = 1;
    for (Map.Entry<String, Object> column : params.entrySet()) {
      updateStatement.setObject(pos, column.getValue());
      pos++;
    }
    
    // Execute that sucker!
    return updateStatement.executeUpdate();  
  }
  
  protected int getRowCount(String table, String whereClause) throws SQLException {
    Statement statement = createStatement();
    String sql = "SELECT COUNT(*) AS count FROM " + table + " WHERE " + whereClause;
    ResultSet result = statement.executeQuery(sql);
    result.next();
    return result.getInt("count");
  }
  
  /**
   * Validate a code by looking up in valid values. Throws ValidationException if not found.
   *
   * @param code
   *   The code to validate
   * @param description
   *   The code description
   * @param valid
   *   A map of {code, description}
   * @return The string description if found.
   */
  protected String validateCode(String code, String description, Map<String, String> valid) throws ValidationException {
    String upValue = code.toUpperCase();
    
    if (!valid.containsKey(upValue)) {
      String msg = "";
      for (Map.Entry<String, String> value : valid.entrySet()) {
        if (msg.length() > 0) msg += ", ";
        msg += value.getKey() + ":(" + value.getValue() + ")";
      }
      msg = "Invalid " + description + " - Valid values are: " + msg;
      
      throw new ValidationException(msg);
    }
    
    return valid.get(upValue);
  }
  
  /**
   * Return a display string for a database value which might be null.
   */
  protected String nullSafe(String possiblyNull) {
    return possiblyNull == null ? "{null}" : possiblyNull;
  }
  
  /**
   * Return a display string for a database value which might be null.
   */
  protected String nullSafe(Date possiblyNull) {
    return possiblyNull == null ? "{null}" : possiblyNull.toString();
  }
}