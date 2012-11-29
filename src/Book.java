/*****************************************************************************************
 * 
 * Book.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Book.java maintains information about books.
 * 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class Book extends AbstractCommandHandler {

	public static String TABLE = "Book";

	/*
	 * Contruct a handler for book objects.
	 */
	public Book(Connection connection) { 
		super(connection);
	}

	/**
	 * Execute the command to create a book.
	 * 
	 * @param title
	 *   The book title
	 * @param author
	 *   The book authors
	 * @param retailPrice
	 *   The retail price. Must be convertable to a double.
	 */
	public void execAdd(
			@Param("title") String title, 
			@Param("author") String author, 
			@Param("retail price") String retailPrice) throws SQLException {

		// validate Book parameters
		try {
			checkPrice(retailPrice);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("title", title);
		params.put("author", author);
		params.put("retailPrice", Double.parseDouble(retailPrice));

		int newID = insertRow(TABLE, "id", 4001, params);

		System.out.println("Inserted Book with ID " + newID + " into Database"); 

	}

	/**
	 * List all books in the system, ordered by the book id.
	 */
	public void execAll() throws SQLException {

		// Select all rows in the book table and sort by ID
		String sql = "SELECT * FROM " + TABLE + " ORDER BY id";

		Statement statement = createStatement();
		int cnt = displayBooks(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Delete the specified book
	 *
	 * @param id
	 *   The book id. Must be convertable to an integer.
	 */
	public void execDelete(@Param("book id") String id) throws SQLException {

		// validate Book ID parameter
		try {
			ValidationHelpers.checkId(connection, id, TABLE);
			ValidationHelpers.checkIdNotForeign(connection, id, "Purchase", "bookId");
			ValidationHelpers.checkIdNotForeign(connection, id, "ItemOrder", "bookId");
			ValidationHelpers.checkIdNotForeign(connection, id, "Stocks", "bookId");
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		int count = deleteRow(TABLE, Integer.parseInt(id));

		System.out.println("Deleted "+ count + " Book with ID " + id + " from Database"); 

	}

	/**
	 * Display the properties of a specific book.
	 *
	 * @param id
	 *   The book id. Must be convertable to an integer.
	 */
	public void execList(@Param("book id") String id) throws SQLException {

		// validate Book ID parameter
		try {
			ValidationHelpers.checkId(connection, id, TABLE);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// Select row in the Book table with ID
		String sql = "SELECT * FROM " + TABLE + " WHERE id = "+ Integer.parseInt(id);

		Statement statement = createStatement();
		int cnt = displayBooks(statement.executeQuery(sql));

		System.out.println(cnt+" Row(s) Returned");

	}

	/**
	 * Update a Book with ID with the given values
	 *
	 * @param id
	 *   The book id. Must be convertable to an integer.
	 * @param title
	 *   The book title
	 * @param author
	 *   The book authors
	 * @param retailPrice
	 *   The retail price. Must be convertable to a double.
	 * @param quantity
	 *   The quantity in stock. Must be convertable to an integer.
	 */
	public void execUpdate(
			@Param("book id") String id, 
			@Param("title") String title, 
			@Param("author") String author,
			@Param("retail price") String retailPrice,
			@Param("quantity") String quantity) throws SQLException {

		// validate Book parameters
		try {
			ValidationHelpers.checkId(connection, id, TABLE);
			checkPrice(retailPrice);
			checkQty(quantity);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("title", title);
		params.put("author", author);
		params.put("retailPrice", Double.parseDouble(retailPrice));
		params.put("stockQuantity", Integer.parseInt(quantity));

		updateRow(TABLE, "id", Integer.parseInt(id), params);

		System.out.println("Updated Book with ID " + id + " in Database"); 
	}

	/**
	 * Check the Quantity. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable integer greater than zero.
	 *
	 * @param quantity
	 *   The quantity of books in stock
	 */
	private int checkQty(String qty) throws ValidationException {
		try {
			int qtyValue = Integer.parseInt(qty);
			if (qtyValue <= 0) {
				throw new ValidationException("Quantity must be greater than zero");
			}
			return qtyValue;
		} catch (Exception e) {
			throw new ValidationException("Quantity must be a number");
		}
	} // checkQty

	/**
	 * Check the Price. Throw a ValidationException if there is an error.
	 *
	 * Must be a parseable double greater than equal to zero.
	 *
	 * @param quantity
	 *   The retail price of books in stock.
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
	 * Display the books from the result set and return the total count.
	 */
	private int displayBooks(ResultSet result) throws SQLException {

		int cnt = 0;
		// loop through the result set printing attributes
		while (result.next()) {
			cnt++;
			int id = result.getInt("id");
			String title = result.getString("title");
			String author = result.getString("author");
			Double retailPrice = result.getDouble("retailPrice");
			int stockQuantity = result.getInt("stockQuantity");
			System.out.println(
					cnt+
					"\tID: "+id+
					"\tTitle: "+title+
					"\tAuthor: "+author+
					"\tPrice: $"+new DecimalFormat("0.00").format(retailPrice)+
					"\tQty: "+stockQuantity
					);
			System.out.println();

		}
		return cnt;

	}
}
