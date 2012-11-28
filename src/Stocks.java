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
			ValidationHelpers.checkId(connection, bookId, "Book");
			// check vendor ID numeric and in database
			ValidationHelpers.checkId(connection, vendorId, "Vendor");
			// check bookId, vendor Id pair not already in stocks
			checkUnique(bookId, vendorId);

		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}


		try {
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
		String sql = "SELECT * FROM Vendor, " + TABLE + " Where vendor.id = stocks.vendorid ORDER BY bookId";

		Statement statement = createStatement();
		int cnt = displayStocks(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	} // execAll

	/**
	 * Delete the specified purchase record
	 *
	 * @param id
	 *   The book id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("Book id") String bookId, @Param("Vendor id") String vendorId) throws SQLException {

		// validate the input parameters
		try {
			// check book ID numeric and in database
			ValidationHelpers.checkId(connection, bookId, "Book");
			// check vendor ID numeric and in database
			ValidationHelpers.checkId(connection, vendorId, "Vendor");
		} catch (ValidationException e) {
			System.out.println("Validation Error: " + e.getMessage());
			return;
		}

		try {
			// Prepare the delete statement
			String sql = "DELETE FROM " + TABLE + " Where bookId='"+bookId+"' AND vendorId='"+vendorId+"'";
			PreparedStatement deleteStatement = prepareStatement(sql);

			// Execute that sucker!
			int cnt = deleteStatement.executeUpdate();
			
			System.out.println("Deleted "+ cnt + " Stocks record(s) with Book ID " + bookId + " and Vendor ID " + vendorId + " from Database"); 

		} catch (Exception e) {
			System.out.println("Exception Inserting Stocks Record: " + e.getMessage());
		}

	} // exec delete

	/**
	 * Display all vendors supplying a book ID
	 *
	 * @param bookid
	 *   The book id. Must be convertable to an integer.
	 */
	public void execList(@Param("book id") String bookId) throws SQLException {

		try {
			// check book ID numeric and in database
			ValidationHelpers.checkId(connection, bookId, "Book");
		} catch (ValidationException e) {
			System.out.println("Validation Error: " + e.getMessage());
			return; 
		}

		// Select rows in the Stocks table with Book ID
		String sql = "SELECT * FROM Vendor, " + TABLE + " WHERE vendor.id = stocks.vendorid AND stocks.bookid = "+ Integer.parseInt(bookId);

		Statement statement = createStatement();
		int cnt = displayStocks(statement.executeQuery(sql));

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
	 * Check the BookId, VendorId pair is unique in the stocks table
	 * 
	 * @param bookId
	 *   The id of the record for the book
	 * @param vendorId
	 *   The id of the record for the vendor from which a book purchased
	 */
	private void checkUnique(String bookId, String vendorId) throws ValidationException {

		try {
			// Book ID, Vendor ID pair must not be in stocks table
			String sql = "SELECT bookId, vendorId FROM Stocks Where bookId='"+bookId+"' AND vendorId='"+vendorId+"'";

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				throw new ValidationException("BookId, VendorId pair must not be in the stocks table: "+ bookId+"/"+vendorId);
			}

			return;

		} catch (Exception e) {
			throw new ValidationException("Exception Validating Book Id/Vendor Id: " + e.getMessage());
		}

	} // checkUnique

	/**
	 * Display the stocks record from the result set and return the total count.
	 */
	private int displayStocks(ResultSet result) throws SQLException {

		int cnt = 0;
		// loop through the result set printing attributes
		while (result.next()) {
			cnt++;
			int bookId = result.getInt("bookId");
			int vendorId = result.getInt("vendorId");
			String vendorName = result.getString("name");
			String vendorPhone = result.getString("phone");

			System.out.println(cnt+"\tBook ID: "+bookId+"\tVendor ID: "+vendorId+"\tVendor Name: "+vendorName+"\tVendor Phone: "+vendorPhone);
		}

		return cnt;

	}

}
