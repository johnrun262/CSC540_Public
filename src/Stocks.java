/*****************************************************************************************
 * 
 * Stocks.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Stocks.java processes stocking of books from a vendor.
 * 
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Stocks extends AbstractCommandHandler {

	private static String TABLE = "Stocks";

	/*
	 * Contruct a handler for staff objects.
	 */
	public Stocks(Connection connection) { 
		super(connection);
	}



	/**
	 * Execute the command to create a stocks record.
	 * 
	 * @param bookId
	 *   The Id of the book record
	 * @param vendorId
	 *   The Id of the vendor record
	 * 
	 */
	public void execAdd(		
			@Param("bookId") String bookId, 
			@Param("vendorId") String vendorId) throws ValidationException, SQLException {

		// validate input parameters
		try {
			// check book ID numeric and in database
			checkBookId(bookId);
			// check vendor ID numeric
			checkVendorId(vendorId);
			// check staff ID numeric

		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}


		try {
			// TODO should not allow duplicate records
			// Prepare the insert statement
			String sql = "INSERT INTO " + TABLE + " VALUES (" + bookId + ", " + vendorId + ")";
			PreparedStatement insertStatement = prepareStatement(sql);

			// Execute that sucker!
			insertStatement.executeUpdate();
		} catch (Exception e) {
			System.out.println("Exception Inserting Stocks Record: " + e.getMessage());
		}

		System.out.println("Inserted Stocks record with BookID " + bookId + " and VendorID " + vendorId+ " into Database"); 

	} // execAdd


	/**
	 * List all purchases in the system, ordered by the book id.
	 */
	public void execAll() throws SQLException {

		// Select all rows in the staff table and sort by ID
		String sql = "SELECT * FROM " + TABLE + " ORDER BY bookId";

		Statement statement = createStatement();
		int cnt = displayPurchase(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	} // execAll

	/**
	 * Delete the specified purchase record
	 *
	 * @param id
	 *   The book id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("Book id") String id) throws SQLException {

		try {
			checkBookId(id);
		} catch (ValidationException e) {
			System.out.println("Validation Error: " + e.getMessage());
			return;
		}

		int count = deleteRow(TABLE, Integer.parseInt(id));

		System.out.println("Deleted "+ count + " Stocks record with ID " + id + " from Database"); 

	} // exec delete

	/**
	 * Display the properties of a specific purchase record.
	 *
	 * @param id
	 *   The purchase id. Must be convertable to an integer.
	 */
	public void execList(@Param("book id") String id) throws SQLException {

		try {
			checkBookId(id);
		} catch (ValidationException e) {
			System.out.println("Validation Error: " + e.getMessage());
			return; 
		}

		// Select row in the Staff table with ID
		String sql = "SELECT * FROM " + TABLE + " WHERE id = "+ Integer.parseInt(id);

		Statement statement = createStatement();
		int cnt = displayPurchase(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Execute the command to update a stocks record is not
	 * supported.
	 * 
	 */
	public void execUpdate() throws ValidationException, SQLException {

		System.out.println("Update not supported for Stocks Table"); 

	} // execUpdate

	/**
	 * Check the BookId. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable integer greater than equal to zero and
	 * exist in the books table.
	 *
	 * @param bookId
	 *   The id of the record for the book purchased from vendor
	 */
	private void checkBookId(String bookId) throws ValidationException {
		try {
			int bookIdValue = Integer.parseInt(bookId);
			if (bookIdValue <= 0) {
				throw new ValidationException("Book Id must be a positive integer: "+bookId);
			}
		} catch (Exception e) {
			throw new ValidationException("Book Id must be a number: "+bookId); 
		}
		try {
			// Book ID must be in book table
			String sql = "SELECT id FROM Book Where id='"+bookId+"'";

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(bookId)) {
					throw new ValidationException("Book Id must be in database: "+ bookId);
				}
			} else {
				throw new ValidationException("Book Id must be in database: "+ bookId);
			}

			return;

		} catch (Exception e) {
			throw new ValidationException("Exception Validating Book Id: " + e.getMessage());
		}

	} // checkBookId

	/**
	 * Check the VendorId. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable integer greater than equal to zero and
	 * exist in the vendor table.
	 *
	 * @param vendorId
	 *   The id of the record for the vendor from which a book purchased
	 */
	private void checkVendorId(String vendorId) throws ValidationException {
		try {
			int vendorIdValue = Integer.parseInt(vendorId);
			if (vendorIdValue <= 0) {
				throw new ValidationException("Vendor Id must be a positive integer: "+vendorId);
			}
		} catch (Exception e) {
			throw new ValidationException("Vendor Id must be a number: "+vendorId);
		}
		try {
			// Vendor ID must be in vendor table
			String sql = "SELECT id FROM Vendor Where id='"+vendorId+"'";

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(vendorId)) {
					throw new ValidationException("Vendor Id must be in database: "+ vendorId);
				}
			} else {
				throw new ValidationException("Vendor Id must be in database: "+ vendorId);
			}

			return;

		} catch (Exception e) {
			throw new ValidationException("Exception Validating Vendor Id: " + e.getMessage());
		}

	} // checkVendorId

	/**
	 * Display the stocks record from the result set and return the total count.
	 */
	private int displayPurchase(ResultSet result) throws SQLException {

		int cnt = 0;
		// loop through the result set printing attributes
		while (result.next()) {
			cnt++;
			int bookId = result.getInt("bookId");
			int vendorId = result.getInt("vendorId");

			System.out.println(cnt+"\tBook ID: "+bookId+"\tVendor ID: "+vendorId);
		}

		return cnt;

	}

}
