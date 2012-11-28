/*****************************************************************************************
 * 
 * Purchase.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Purchase.java processes purchases for books from a vendor.
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;



public class Purchase extends AbstractCommandHandler {

	private static String STATUS_RECEIVED = "received";
	private static String STATUS_ORDERED = "ordered";
	private static String STATUS_SHIPPED = "shipped";

	/*
	 * Contruct a handler for staff objects.
	 */
	public Purchase(Connection connection) { 
		super(connection);
	}



	/**
	 * Execute the command to create a purchase record.
	 * 
	 * @param bookId
	 *   The Id of the book record
	 * @param vendorId
	 *   The Id of the vendor record
	 * @param staffId
	 *   The Id of the staff record
	 * @param qty
	 *   The quantity of the purchase
	 * @param price
	 *   The wholesale price of the book 
	 */
	public void execAdd(		
			@Param("bookId") String bookId, 
			@Param("vendorId") String vendorId, 
			@Param("staffId") String staffId,
			@Param("quantity") String qty,
			@Param("price") String price) throws ValidationException, SQLException {

		Double priceValue = null;
		Integer qtyValue = null;

		// validate input parameters
		try {
			// check book ID numeric and in database
			ValidationHelpers.checkId(connection, bookId, ValidationHelpers.TABLE_BOOK);
			// check vendor ID numeric
			ValidationHelpers.checkId(connection, vendorId, ValidationHelpers.TABLE_VENDOR);
			// check staff ID numeric
			ValidationHelpers.checkId(connection, staffId, ValidationHelpers.TABLE_STAFF);
			// check quantity numeric and > 0
			qtyValue = checkQty(qty);
			// check price numeric and >= 0
			priceValue = checkPrice(price);


		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// set purchase date to today
		Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		String date = formatter.format(todaysDate);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("bookId", bookId);
		params.put("vendorId", vendorId);
		params.put("staffId", staffId);
		params.put("quantity", qtyValue);
		params.put("wholesalePrice", priceValue);
		params.put("orderDate", date);
		params.put("status", STATUS_ORDERED);

		int newID = insertRow(ValidationHelpers.TABLE_PURCHASE, "id", 1001, params);

		System.out.println("Inserted Purchase record with ID " + newID + " into Database"); 

	} // execAdd


	/**
	 * List all purchases in the system, ordered by the purchase id.
	 */
	public void execAll() throws SQLException {

		// Select all rows in the staff table and sort by ID
		String sql = "SELECT * FROM " + ValidationHelpers.TABLE_PURCHASE + " ORDER BY id";

		Statement statement = createStatement();
		int cnt = displayPurchase(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	} // execAll

	/**
	 * Delete the specified purchase record
	 *
	 * @param id
	 *   The purchase id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("staff id") String id) throws SQLException {

		// check if the id is valid
		try {
			ValidationHelpers.checkId(connection, id, ValidationHelpers.TABLE_PURCHASE);
		} catch (ValidationException e) {
			System.out.println("Validation Error: " + e.getMessage());
			return;
		}

		// do the delete
		int count = deleteRow(ValidationHelpers.TABLE_PURCHASE, Integer.parseInt(id));

		System.out.println("Deleted "+ count + " Staff with ID " + id + " from Database"); 

	} // exec delete

	/**
	 * Display the properties of a specific purchase record.
	 *
	 * @param id
	 *   The purchase id. Must be convertable to an integer.
	 */
	public void execList(@Param("staff id") String id) throws SQLException {

		// check if the id is valid
		try {
			ValidationHelpers.checkId(connection, id, ValidationHelpers.TABLE_PURCHASE);
		} catch (ValidationException e) {
			System.out.println("Validation Error: " + e.getMessage());
			return; 
		}

		// Select row in the Staff table with ID
		String sql = "SELECT * FROM " + ValidationHelpers.TABLE_PURCHASE + " WHERE id = "+ Integer.parseInt(id);

		Statement statement = createStatement();
		int cnt = displayPurchase(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Execute the command to update a purchase record.
	 * 
	 * @param id
	 *   The id of the purchase record we are updating
	 * @param bookId
	 *   The Id of the book record
	 * @param vendorId
	 *   The Id of the vendor record
	 * @param staffId
	 *   The Id of the staff record
	 * @param qty
	 *   The quantity of the purchase
	 * @param price
	 *   The wholesale price of the book 
	 * @param orderDate
	 *   The date the order was placed
	 * @param status
	 *   The status of the order (ordered, received, shipped)
	 */
	// TODO this needs testing!
	public void execUpdate(	 
			@Param("id") String purId,
			@Param("bookId") String bookId, 
			@Param("vendorId") String vendorId, 
			@Param("staffId") String staffId,
			@Param("quantity") String qty,
			@Param("price") String price,
			@Param("orderDate") String orderDate,
			@Param("status") String status) throws ValidationException, SQLException {

		Double priceValue = null;
		Integer qtyValue = null;
		Integer purIDValue = null;

		// validate input parameters
		try {
			// check the purchase record id is numeric and in database
			ValidationHelpers.checkId(connection, purId, ValidationHelpers.TABLE_PURCHASE);
			purIDValue = Integer.parseInt(purId);
			// check book ID numeric and in database
			ValidationHelpers.checkId(connection, bookId, ValidationHelpers.TABLE_BOOK);
			// check vendor ID numeric
			ValidationHelpers.checkId(connection, vendorId, ValidationHelpers.TABLE_VENDOR);
			// check staff ID numeric
			ValidationHelpers.checkId(connection, staffId, ValidationHelpers.TABLE_STAFF);
			// check quantity numeric and > 0
			qtyValue = checkQty(qty);
			// check price numeric and >= 0
			priceValue = checkPrice(price);
			// check valid format date
			// TODO uses date of birth routine but not DoB
			ValidationHelpers.checkDateOfBirth(orderDate);
			// check status ordered, shipped, received
			status = checkStatus(status);

		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", purId);
		params.put("bookId", bookId);
		params.put("vendorId", vendorId);
		params.put("staffId", staffId);
		params.put("quantity", qtyValue);
		params.put("wholesalePrice", priceValue);
		params.put("orderDate", orderDate);
		params.put("status", status);


		updateRow(ValidationHelpers.TABLE_PURCHASE, "id", purIDValue, params);

		System.out.println("Update Purchase record with ID " + purId); 

	} // execUpdate

	/**
	 * Receive the specified purchase record
	 * 
	 * This function updates the status of the purchase to received
	 * and adds the quantity to the book. All this is done as a
	 * transaction.
	 *
	 * @param id
	 *   The purchase id. Must be convertable to an integer.
	 */
	public void execRec(@Param("id") String purId) throws ValidationException, SQLException {

		String purStatus = null;
		int quantity = 0;
		int bookId = 0;

		// validate input parameters
		try {
			// check the purchase record id is numeric and in database
			ValidationHelpers.checkId(connection, purId, ValidationHelpers.TABLE_PURCHASE);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// Start a transaction
		try {
			// Require consistency across multiple queries via serialized transaction isolation
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			// Get information about purchase inside transaction so two tasks
			// don't process the same receive.
			String sql = "SELECT * FROM "+ValidationHelpers.TABLE_PURCHASE+" Where Id="+purId;

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				// get the quantity from the result
				quantity = result.getInt("quantity");
				// get status
				purStatus = result.getString("status");
				// Get the book ID
				bookId = result.getInt("bookId");
			}

			// Validate purchase is not already received
			if (purStatus.equals(Purchase.STATUS_RECEIVED)) {
				throw new ValidationException("Purchase already received: " + purId);
			}


			// Add quantity to quantity of book
			sql = "UPDATE "+ValidationHelpers.TABLE_PURCHASE+" SET stockQuantity = stockQuantity + "+ quantity + " Where Id="+ bookId;

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);
			
			if (cnt == 0) {
				// should not happen
				throw new ValidationException("Warning no books updated with ID " + bookId);
			}
			
			// Change status to "received"
			sql = "UPDATE "+ValidationHelpers.TABLE_PURCHASE+" SET status = '"+ Purchase.STATUS_RECEIVED + "' Where Id="+ purId;

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			cnt = statement.executeUpdate(sql);
			
			if (cnt == 0) {
				// should not happen
				throw new ValidationException("Warning no purchases updated with ID " + purId);
			}
			
			// Commit the transaction
			connection.commit();
			System.out.println("Received Purchase " + purId + " and Quantity increased by " + quantity + " for Book Id "+ bookId);


		} catch (Exception ex) {
			// abort if problem encountered
			connection.rollback();
			System.out.println("Purchase not received : " + ex.getMessage());
		}

		return;

	}

	// check the status passed from user is valid
	private String checkStatus(String status) throws ValidationException {

		Map<String, String> valid = new TreeMap<String, String>();
		valid.put("O", STATUS_ORDERED);
		valid.put("R", STATUS_RECEIVED);
		valid.put("S", STATUS_SHIPPED);

		return validateCode(status, "Status", valid);

	} // checkStatus

	/**
	 * Check the Quantity. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable integer greater than zero.
	 *
	 * @param quantity
	 *   The quantity of books purchased.
	 */
	private int checkQty(String qty) throws ValidationException {
		try {
			int qtyValue = Integer.parseInt(qty);
			if (qtyValue <= 0) {
				throw new ValidationException("Quantity must be greater than zero");
			}
			return qtyValue;
		} catch (Exception e) {
			throw new ValidationException("Quantity must be a number greater than zero");
		}
	} // checkQty

	/**
	 * Check the Price. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable double greater than equal to zero.
	 *
	 * @param price
	 *   The price of books purchased.
	 */
	private double checkPrice(String price) throws ValidationException {
		try {
			double priceValue = Double.parseDouble(price);
			if (priceValue < 0) {
				throw new ValidationException("Price must be greater than or equal zero");
			}
			return priceValue;
		} catch (Exception e) {
			throw new ValidationException("Price must be a positive number");
		} 
	} // checkPrice

	/**
	 * Display the purchase record from the result set and return the total count.
	 */
	private int displayPurchase(ResultSet result) throws SQLException {

		int cnt = 0;
		// loop through the result set printing attributes
		while (result.next()) {
			cnt++;
			int purId = result.getInt("id");
			Date date = result.getDate("orderDate");
			int bookId = result.getInt("bookId");
			int vendorId = result.getInt("vendorId");
			int staffId = result.getInt("staffId");
			int qty = result.getInt("quantity");
			String status = result.getString("status");
			int price = result.getInt("wholesalePrice");

			System.out.println(cnt+"\tID: "+purId+"\tDate: "+date+"\tBook ID: "+bookId+"\tVendor ID: "+vendorId+"\tStaff ID: "+staffId+"\tQty: "+qty+"\tStatus: "+status+"\tWholesale Price: "+price);
		}

		return cnt;

	}

}
