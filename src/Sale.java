/*****************************************************************************************
 * 
 * Sale.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Sale.java manages sales to a customer
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;



public class Sale extends AbstractCommandHandler {

  private static String TABLE_ORDER = "Orders";
  private static String TABLE_ORDER_ITEM = "ItemOrder";
  
  private static String STATUS_SHIPPED = "shipped";
  private static String STATUS_ORDERED = "ordered";
  private static String STATUS_RECEIVED = "received";
  
  /*
   * Contruct a handler for sale objects.
   */
	public Sale(Connection connection) { 
    super(connection);
  }

  /**
	 * List all orders in the system, ordered by the order id.
	 */
	public void execAll() throws SQLException {
  
    // Select all rows in the order table and sort by ID
    String sql = getOrderSql(null);

    Statement statement = createStatement();
    int cnt = displayOrders(statement.executeQuery(sql));

    System.out.println(cnt+" Row(s) Returned");
    
	}
  
  /**
	 * Display the properties of a specific order.
   *
   * @param id
   *   The order id. Must be convertable to an integer.
	 */
	public void execList(@Param("order id") String id) throws SQLException {
		    
    int orderId = Integer.parseInt(id);
    // Select row in the Book table with ID
    String sql = getOrderSql("o.id=" + orderId);
    
    // Display order-level info.
    Statement statement = createStatement();
    int cnt = displayOrders(statement.executeQuery(sql));
    
    // If an order was found, display the line items
    if (cnt > 0) {
      System.out.println("Items ordered:");
      sql = "SELECT b.title AS bookTitle, b.retailPrice, oi.salePrice, oi.quantity" +
        " FROM " + TABLE_ORDER_ITEM + " oi " +
        " INNER JOIN " + Book.TABLE + " b ON oi.bookId=b.id " +
        " WHERE oi.orderId=" + orderId;
        
      statement = createStatement();
      displayOrderItems(statement.executeQuery(sql));
    }
	}
  
  /**
   * Create a customer sale.
   *
   * @param staff
   *   The staff id who performed the sale.
   * @param customer
   *   The customer id which the books were sold to.
   * @param book
   *   The book id which is being sold.
   * @param salePrice
   *   The price the book is sold at. Must be a double.
   * @param quantity
   *   The quantity of books being sold. Optional (default to 1)
   */
  public void execCreate(
    @Param("staff id") String staff,
    @Param("customer id") String customer,
    @Param("book id") String book,
    @Param("sale price") String salePrice,
    @Param(value="quantity", optional=true) String qty) throws SQLException {
    
    int staffId = Integer.parseInt(staff);
    int customerId = Integer.parseInt(customer);
    int bookId = Integer.parseInt(book);
    double price = Double.parseDouble(salePrice);
    int quantity = qty != null ? Integer.parseInt(qty) : 1;
    
    try {
      // Require consistency across multiple queries via serialized transaction isolation
      connection.setAutoCommit(false);
      connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      
      // Lets check if the specified ids exist
      if (getRowCount(Staff.TABLE, "id=" + staffId) == 0) exitProgram("Could not find staff " + staffId + ".");
      if (getRowCount(Customer.TABLE, "id=" + customerId) == 0) exitProgram("Could not find customer " + customerId + ".");
      if (getRowCount(Book.TABLE, "id=" + bookId) == 0) exitProgram("Could not find book " + bookId + ".");
    
      // TODO: What do we do if there is insufficient quantity of this book in stock? Create a purchase order? Or?
      
      // TODO: Should we check sale price against retailPrice and not allow loss sales? I guess not
      
      // Orders occur RIGHT NOW!
      java.util.Date now = new java.util.Date();

      // Create an order record
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("staffId", staffId);
      params.put("customerId", customerId);
      params.put("status", STATUS_ORDERED);
      params.put("orderDate", new java.sql.Date(now.getTime()));
      int orderId = insertRow(TABLE_ORDER, "id", 5001, params, true);
      
      // Create an item order record
      String sql = "INSERT INTO " + TABLE_ORDER_ITEM + " (orderId,bookId,salePrice,quantity) VALUES (?,?,?,?)";
      PreparedStatement insertStatement = prepareStatement(sql);
      insertStatement.setInt(1, orderId);
      insertStatement.setInt(2, bookId);
      insertStatement.setDouble(3, price);
      insertStatement.setInt(4, quantity);      
      insertStatement.executeUpdate();
      
      // Commit the transaction
      connection.commit();
      
      System.out.println("Inserted Order with ID " + orderId + " into Database"); 
      
    } catch (SQLException e ) {
      // Rollback if any unanticipated SQL errors occur.
      connection.rollback();
      throw e;
    }
  }
  
  /**
   * Add a book to an existing order.
   *
   * @param order
   *   The order id that books are added to.
   * @param book
   *   The book id which is being sold.
   * @param salePrice
   *   The price the book is sold at. Must be a double.
   * @param quantity
   *   The quantity of books being sold. Optional (default to 1)
   */
  public void execAppend (
    @Param("order id") String order,
    @Param("book id") String book,
    @Param("sale price") String salePrice,
    @Param(value="quantity", optional=true) String qty) throws SQLException {
    
    int orderId = Integer.parseInt(order);
    int bookId = Integer.parseInt(book);
    double price = Double.parseDouble(salePrice);
    int quantity = qty != null ? Integer.parseInt(qty) : 1;
    
    try {
      // Require consistency across multiple queries via serialized transaction isolation
      connection.setAutoCommit(false);
      connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      
      // Lets check if the specified ids exist
      if (getRowCount(TABLE_ORDER, "id=" + orderId) == 0) exitProgram("Could not find order " + orderId + ".");
      if (getRowCount(Book.TABLE, "id=" + bookId) == 0) exitProgram("Could not find book " + bookId + ".");
      
      // If the order was shipped, we can't append to it.
      exitIfShipped(orderId);
    
      // TODO: What do we do if there is insufficient quantity of this book in stock? Create a purchase order? Or?
      
      // TODO: Should we check sale price against retailPrice and not allow loss sales? I guess not
    
      // Create an item order record
      String sql = "INSERT INTO " + TABLE_ORDER_ITEM + " (orderId,bookId,salePrice,quantity) VALUES (?,?,?,?)";
      PreparedStatement insertStatement = prepareStatement(sql);
      insertStatement.setInt(1, orderId);
      insertStatement.setInt(2, bookId);
      insertStatement.setDouble(3, price);
      insertStatement.setInt(4, quantity);      
      insertStatement.executeUpdate();
      
      // Commit the transaction
      connection.commit();
      
      System.out.println("Appended book to Order ID " + orderId); 
      
    } catch (SQLException e ) {
      // Rollback if any unanticipated SQL errors occur.
      connection.rollback();
      throw e;
    }    
  }
  
  /**
   * Check if the order has been shipped and exit the program if it has.
   *
   * @param orderId
   *   The order id to check.
   */
  private void exitIfShipped(int orderId) throws SQLException {
    String sql = "SELECT status FROM " + TABLE_ORDER + " WHERE id=" + orderId;
    Statement statement = createStatement();
    ResultSet result = statement.executeQuery(sql);
    if (!result.next()) {
      // Whoops, order was not found!
      exitProgram("Could not find order " + orderId + ".");
    }
    String orderStatus = result.getString("status").trim();
    if (orderStatus.equals(STATUS_SHIPPED)) {
      // Sorry, you can't delete an order that already shipped to the customer.
      exitProgram("Order " + orderId + " could not be deleted. It has already been shipped!");
    }
  }
  
  /**
	 * Delete the specified order
   *
   * @param id
   *   The order id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("order id") String id) throws SQLException  {

    // Require consistency across multiple queries via serialized transaction isolation
    connection.setAutoCommit(false);
    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    
    int orderId = Integer.parseInt(id);
    // Check the order and determine if it has been shipped. Disallow if true
    exitIfShipped(orderId);
    
    // TODO: Do we need to increase the stock quantity of books in this order, or are they only reduced when shipped?
    
    // Delete the order, let SQL cascade delete the order items.
    int count = deleteRow(TABLE_ORDER, orderId);
    
    // Commit the transaction.
    connection.commit();
    
    System.out.println("Deleted "+ count + " Order with ID " + id + " from Database"); 
    
	}
  
  
  private String getOrderSql(String whereClause) {
    String sql = "SELECT o.id, s.name AS staffName, c.name AS customerName, o.status, o.orderDate " +
      " FROM " + TABLE_ORDER + " o " +
      " INNER JOIN " + Staff.TABLE + " s ON o.staffId=s.id " +
      " INNER JOIN " + Customer.TABLE + " c ON o.customerId=c.id ";
    if (whereClause != null) sql += " WHERE " + whereClause;
    sql += " ORDER BY o.id";
    return sql;
  }
  
  /**
   * Display the orders from the result set and return the total count.
   */
  private int displayOrders(ResultSet result) throws SQLException {
  
    int cnt = 0;
    // loop through the result set printing attributes
    while (result.next()) {
      cnt++;
      int id = result.getInt("id");
      String staff = result.getString("staffName");
      String customer = result.getString("customerName");
      String status = result.getString("status");
      String orderDate = result.getString("orderDate");
      System.out.println(
        cnt+
        "\tID: "+id+
        "\tStaff: "+staff+
        "\tCustomer: "+customer+
        "\tStatus: "+status+
        "\tDate: "+orderDate
      );
    }
    return cnt;
    
  }

  /**
   * Display the order items from the result set and return the total count.
   */
  private int displayOrderItems(ResultSet result) throws SQLException {
  
    int cnt = 0;
    // loop through the result set printing attributes
    double grandTotal = 0;
    while (result.next()) {
      cnt++;
      String book = result.getString("bookTitle");
      double retail = result.getDouble("retailPrice");
      double sale = result.getDouble("salePrice");
      int qty = result.getInt("quantity");
      System.out.println(
        "\tBook: "+book+
        "\tRetail: $"+new DecimalFormat("0.00").format(retail)+
        "\tSale: $"+new DecimalFormat("0.00").format(sale)+
        "\tQty: "+qty+
        "\tSub-Total: $"+new DecimalFormat("0.00").format(sale*qty)
      );
      grandTotal += (sale*qty);
    }
    System.out.println();
    System.out.println("ORDER TOTAL $" + new DecimalFormat("0.00").format(grandTotal));
    return cnt;
    
  }
}
