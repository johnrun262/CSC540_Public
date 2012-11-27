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
			checkBookId(id);
			checkBookIdForeign(id);
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
			checkBookId(id);
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
			checkBookId(id);
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
	 * Check the BookId is not a Foreign Key in Purchase, Stocks, or ItemOrders. 
	 * Throw a ValidationException if in exists.
	 *
	 * @param bookId
	 *   The id of the record for the book purchased from vendor
	 */
	private void checkBookIdForeign(String bookId) throws ValidationException {

		try {
			// Book ID must not be in itemOrders table
			String sql = "SELECT BookId FROM ItemOrder Where BookId='"+bookId+"'";

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				throw new ValidationException("Book Id must not be in ItemOrder: "+ bookId);
			}

			// Book ID must not be in Purchases table
			sql = "SELECT BookId FROM Purchase Where BookId='"+bookId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				throw new ValidationException("Book Id must not be in Purchase: "+ bookId);
			}
			
			// Book ID must not be in Stocks table
			sql = "SELECT BookId FROM Stocks Where BookId='"+bookId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				throw new ValidationException("Book Id must not be in Stocks: "+ bookId);
			}
			
			return;

		} catch (Exception e) {
			throw new ValidationException("Exception Validating Book Id: " + e.getMessage());
		}

	} // checkBookIdForeign

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
			if (priceValue <= 0) {
				throw new ValidationException("Price must be greater than or equal zero");
			}
			return priceValue;
		} catch (Exception e) {
			throw new ValidationException("Price must be a number");
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
		}
		return cnt;

	}
}
