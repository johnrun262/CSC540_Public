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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



public class Sale extends AbstractCommandHandler {

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
		int orderId;

		// Validate the inputs
		try {
			orderId = ValidationHelpers.checkId(connection, id, ValidationHelpers.TABLE_ORDERS);

		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// Select row in the Book table with ID
		String sql = getOrderSql("o.id=" + orderId);

		// Display order-level info.
		Statement statement = createStatement();
		displayOrders(statement.executeQuery(sql));

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
			@Param(value="quantity", optional=true) String qty) throws  ValidationException, SQLException {

		int staffId;
		int customerId;
		int bookId;
		double price;
		int orderQuantity;

		// Validate the inputs
		try {
			staffId = ValidationHelpers.checkId(connection, staff, ValidationHelpers.TABLE_STAFF);
			customerId = ValidationHelpers.checkId(connection, customer, ValidationHelpers.TABLE_CUSTOMER);
			bookId = ValidationHelpers.checkId(connection, book, ValidationHelpers.TABLE_BOOK);
			price = Double.parseDouble(salePrice);
			orderQuantity = qty != null ? Integer.parseInt(qty) : 1;
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		} catch (Exception ex) {
			System.out.println("Invalid Numeric Input");
			return;			
		}

		try {
			// Require consistency across multiple queries via serialized transaction isolation
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			// Orders occur RIGHT NOW!
			java.util.Date now = new java.util.Date();

			// Create an order record
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("staffId", staffId);
			params.put("customerId", customerId);
			params.put("status", STATUS_ORDERED);
			params.put("orderDate", new java.sql.Date(now.getTime()));
			int orderId = insertRow(ValidationHelpers.TABLE_ORDERS, "id", 5001, params, true);

			// add the item to the order after checking there is sufficient stock. 
			// Then, decrease the stock
			addItemOrder (bookId, orderId, orderQuantity, price);

			// Commit the transaction
			connection.commit();

			System.out.println("Inserted Order with ID " + orderId + " into Database"); 

		} catch (SQLException e ) {
			// Rollback if any unanticipated SQL errors occur.
			connection.rollback();
			System.out.println("Order not created due to error: " + e.getMessage()); 
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
			@Param(value="quantity", optional=true) String qty) throws ValidationException, SQLException {

		int orderId;
		int bookId;
		double price;
		int orderQuantity;

		// Validate the inputs
		try {
			orderId = ValidationHelpers.checkId(connection, order, ValidationHelpers.TABLE_ORDERS);
			bookId = ValidationHelpers.checkId(connection, book, ValidationHelpers.TABLE_BOOK);
			price = Double.parseDouble(salePrice);
			orderQuantity = qty != null ? Integer.parseInt(qty) : 1;
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		} catch (Exception ex) {
			System.out.println("Invalid Numeric Input");
			return;			
		}

		try {
			// Require consistency across multiple queries via serialized transaction isolation
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			// If the order was shipped, we can't append to it.
			String orderStatus = getOrderStatus(orderId);
			if (orderStatus.equals(STATUS_SHIPPED)) {
				// Sorry, you can't append to an order that already shipped to the customer.
				throw new ValidationException ("Order " + orderId + " cannot be appended to. It has already been shipped!");
			}

			// add the item to the order after checking there is sufficient stock. 
			// Then, decrease the stock
			addItemOrder (bookId, orderId, orderQuantity, price);

			// Commit the transaction
			connection.commit();

			System.out.println("Appended book to Order ID " + orderId); 

		} catch (SQLException e ) {
			// Rollback if any unanticipated SQL errors occur.
			connection.rollback();
			throw e;
		}    
	}

	/*
	 * Add an itemOrdered record to an existing order
	 * Called from Create and Append
	 * 
	 */
	// Only allow the order if there is sufficient quantity 
	// Get number of books available inside transaction so other order doesn't count too
	private void addItemOrder (int bookId, int orderId, int orderQuantity, double price) 
			throws ValidationException, SQLException {

		String sql = "SELECT * FROM "+ValidationHelpers.TABLE_BOOK+" Where Id="+bookId;

		Statement statement = connection.createStatement();
		statement.setQueryTimeout(10);
		ResultSet result = statement.executeQuery(sql);

		if (result.next()) {
			// get the quantity from the result
			int stockQuantity = result.getInt("stockQuantity");
			if (stockQuantity < orderQuantity) {
				throw new ValidationException("Insufficient stock for Book "+ bookId + " Ordered: "+orderQuantity+" In Stock: "+ stockQuantity);
			}
		}

		// Create an item order record
		sql = "INSERT INTO " + ValidationHelpers.TABLE_ITEMORDER + " (orderId,bookId,salePrice,quantity) VALUES (?,?,?,?)";
		PreparedStatement insertStatement = prepareStatement(sql);
		insertStatement.setInt(1, orderId);
		insertStatement.setInt(2, bookId);
		insertStatement.setDouble(3, price);
		insertStatement.setInt(4, orderQuantity);      
		insertStatement.executeUpdate();

		// Decrease the stock quantity by the order quantity
		// Change status to "received"
		sql = "UPDATE "+ValidationHelpers.TABLE_BOOK+" SET stockQuantity = stockQuantity - "+ orderQuantity + " Where Id="+ bookId;

		statement = connection.createStatement();
		statement.setQueryTimeout(10);
		statement.executeUpdate(sql);
	}

	/**
	 * Mark an order as shipped.
	 *
	 * 1) Ensure the order is in the correct status.
	 * 2) Ensure there is sufficient books in stock to fulfill the order
	 * 3) Decrease stock by quantity on order
	 * 4) Mark order as shipped
	 *
	 * @param order
	 *   The order id that will be shipped.
	 */
	public void execShip (
			@Param("order id") String order) throws ValidationException, SQLException {

		int orderId;

		// Validate the inputs
		try {
			orderId = ValidationHelpers.checkId(connection, order, ValidationHelpers.TABLE_ORDERS);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		} catch (Exception ex) {
			System.out.println("Invalid Numeric Input");
			return;			
		}

		try {
			// Mark order shipped
			String sql = "UPDATE " + ValidationHelpers.TABLE_ORDERS + " SET status=? WHERE id=?";
			PreparedStatement updateStatement = connection.prepareStatement(sql);
			updateStatement.setString(1, STATUS_SHIPPED);
			updateStatement.setInt(2, orderId);
			updateStatement.executeUpdate();

			System.out.println("Order ID " + orderId + " has been marked as shipped"); 

		} catch (SQLException e ) {
			throw e;
		}    
	}

	/**
	 * Check if the order has been shipped and exit the program if it has.
	 *
	 * @param orderId
	 *   The order id to check.
	 */
	private String getOrderStatus(int orderId) throws SQLException {
		String sql = "SELECT status FROM " + ValidationHelpers.TABLE_ORDERS + " WHERE id=" + orderId;
		Statement statement = createStatement();
		ResultSet result = statement.executeQuery(sql);
		if (!result.next()) {
			// Whoops, order was not found!
			exitProgram("Could not find order " + orderId + ".");
		}
		return result.getString("status").trim();
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
		String orderStatus = getOrderStatus(orderId);
		if (orderStatus.equals(STATUS_SHIPPED)) {
			// Sorry, you can't delete an order that already shipped to the customer.
			exitProgram("Order " + orderId + " could not be deleted. It has already been shipped!");
		}


		// TODO: Do we need to increase the stock quantity of books in this order, or are they only reduced when shipped?

		// Delete the order, let SQL cascade delete the order items.
		int count = deleteRow(ValidationHelpers.TABLE_ORDERS, orderId);

		// Commit the transaction.
		connection.commit();

		System.out.println("Deleted "+ count + " Order with ID " + id + " from Database"); 

	}

	/**
	 * Execute the command to mark receipt of payment
	 * 
	 * @param id
	 *   The id of the order record we are updating
	 * 
	 */
	public void execPaid(	 
			@Param("order id") String purId,
			@Param(value="status", optional=true) String status) throws ValidationException, SQLException {

		int orderIDValue;
		
		// validate input parameters
		try {
			// check the purchase record id is numeric and in database
			orderIDValue = ValidationHelpers.checkId(connection, purId, ValidationHelpers.TABLE_ORDERS);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// set paid date to today
		Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String date = formatter.format(todaysDate);
		
		// status not supplied then it is paid
		if (status == null) {
			status = "paid";
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", purId);
		params.put("paidDate", date);
		params.put("status", status);

		updateRow(ValidationHelpers.TABLE_ORDERS, "id", orderIDValue, params);

		System.out.println("Order status changed to "+status+" for record with ID " + purId); 

	} // execPaid

	private String getOrderSql(String whereClause) {
		String sql = "SELECT o.id, s.id AS staffId, s.name AS staffName, c.id AS customerID, c.name AS customerName, o.status, o.orderDate, o.paidDate " +
				" FROM " + ValidationHelpers.TABLE_ORDERS + " o " +
				" INNER JOIN " + ValidationHelpers.TABLE_STAFF + " s ON o.staffId=s.id " +
				" INNER JOIN " + ValidationHelpers.TABLE_CUSTOMER + " c ON o.customerId=c.id ";
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
			int staffId = result.getInt("staffID");
			String customer = result.getString("customerName");
			int customerId = result.getInt("customerID");
			String status = result.getString("status");
			Date orderDate = result.getDate("orderDate");
			Date paidDate = result.getDate("paidDate");
			System.out.println(
					cnt+
					"\tID: "+id+
					"\tStaff: "+staff+"("+staffId+")"+
					"\tCustomer: "+customer+"("+customerId+")"+
					"\tStatus: "+status+
					"\tOrder Date: "+orderDate+
					"\tPaid Date: "+paidDate
					);
			System.out.println();

			// display the line items for the order
			System.out.println("Items ordered:");
			String sql = "SELECT b.title AS bookTitle, b.id AS bookID, b.retailPrice, oi.salePrice, oi.quantity" +
					" FROM " + ValidationHelpers.TABLE_ITEMORDER + " oi " +
					" INNER JOIN " + ValidationHelpers.TABLE_BOOK + " b ON oi.bookId=b.id " +
					" WHERE oi.orderId=" + id;

			Statement statement = createStatement();
			displayOrderItems(statement.executeQuery(sql));

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
			int bookId = result.getInt("BookID");
			double retail = result.getDouble("retailPrice");
			double sale = result.getDouble("salePrice");
			int qty = result.getInt("quantity");
			System.out.println(
					"\tBook: "+book+"("+bookId+")"+
							"\tRetail: $"+new DecimalFormat("0.00").format(retail)+
							"\tSale: $"+new DecimalFormat("0.00").format(sale)+
							"\tQty: "+qty+
							"\tSub-Total: $"+new DecimalFormat("0.00").format(sale*qty)
					);
			System.out.println();

			grandTotal += (sale*qty);
		}
		
		System.out.println();
		System.out.println("ORDER TOTAL $" + new DecimalFormat("0.00").format(grandTotal));
		System.out.println();

		return cnt;

	}
}
