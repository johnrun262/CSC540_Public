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



public class Book {

	private Connection connection = null;

	// this is the list of commands that can be done to a book
	private static enum BookCmds {ADD, ALL, DELETE, UPDATE, LIST, SELL};

	// Constructor
	Book(Connection connection){

		// save the connection to the database
		this.connection = connection; 

	}



	public int exec(String[] args){

		if (args.length < 2){
			usage();
			return -1;
		}

		try {
			switch (BookCmds.valueOf(args[1].toUpperCase())) {
			case ADD:
				// Add a new book to the database

				return (addBook(args));

			case ALL:
				// List all books in the database

				return (allBooks(args));

			case DELETE:
				// Remove a book from the database

				return (deleteBook(args));

			case UPDATE:
				// Update a book already in the database

				return (updateBook(args));

			case LIST:
				// List information about a book already in the the database

				return (listBook(args));

			case SELL:
				// Sell a customer some number of books

				// TODO

				break;

			} // switch

		} catch (IllegalArgumentException e) {
			usage();
			return -1;
		}

		return 0;

	}

	/*
	 * Method: addBook
	 * 
	 * Execute the command to create books
	 * 
	 * Input:
	 * args[0] = "book"
	 * args[1] = "add" 
	 * args[2] = <title>
	 * args[3] = <author>
	 * args[4] = <retail price>
	 *
	 * Returns:
	 * -1 = book not inserted
	 */
	private int addBook(String[] args) {

		Statement statement = null;
		int newID = 4001;

		// do we have enough parameters to continue?
		if (args.length < 5) {
			System.out.println("Command Missing Parameters - usage: Book Add <title> <author> <retail price>");
			return -1;
		}

		try {
			// Get the last ID assigned and add one to it to create a new ID for this book
			// TODO somehow lock others out of insert to prevent duplicate ID
			String sql = "SELECT MAX(id) AS max FROM Book";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				newID = result.getInt("max");
				newID++;
			}

			// Create and execute the INSERT SQL statement
			// TODO name can be spread over multiple args
			// TODO title can be spread over multiple args
			sql = "INSERT INTO Book VALUES ("+ newID +", "+args[4]+", 0, '"+args[2]+"','"+args[3]+"')";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Inserted "+ cnt + " Book with ID " + newID + " into Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // addBook

	/*
	 * Method: allBooks
	 * 
	 * Execute the command to dump books
	 * 
	 * Input:
	 * args[0] = "book"
	 * args[1] = "all" 
	 *
	 * Returns:
	 * -1 = error processing request
	 */
	private int allBooks(String[] args) {
		
		Statement statement = null;
		int cnt = 0;
		
		try {
			// Select all rows in the book table and sort by ID
			String sql = "SELECT * FROM Book ORDER BY id";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
			while (result.next()) {
				cnt++;
				int id = result.getInt("id");
				String title = result.getString("title");
				String author = result.getString("author");
				int retailPrice = result.getInt("retailPrice");
				int stockQuantity = result.getInt("stockQuantity");
				System.out.println(cnt+"\tID: "+id+"\tTitle: "+title+"\tAuthor: "+author+"\tPrice: $"+retailPrice+"\tQty: "+stockQuantity);
			}

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	 

	} // allBooks

	/*
	 * Method: deleteBook
	 * 
	 * Execute the command to delete a Book by ID
	 * 
	 * Input:
	 * args[0] = "book"
	 * args[1] = "delete" 
	 * args[2] = <id>
	 *
	 * Returns:
	 * -1 = book not deleted
	 */
	private int deleteBook(String[] args) {

		Statement statement = null;
		
		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Book Delete <id>");
			return -1;
		}

		try { 

			// Create and execute the DELETE SQL statement
			// TODO validate id numeric
			String sql = "DELETE FROM Book WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Deleted "+ cnt + " Book with ID " + args[2] + " from Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // deleteBook

	/*
	 * Method: listBook
	 * 
	 * Execute the command to list info about a Book given ID
	 * 
	 * Input:
	 * args[0] = "Book"
	 * args[1] = "list"
	 * args[2] = <id> 
	 *
	 * Returns:
	 * -1 = error retrieving Book
	 */
	private int listBook(String[] args) {
		
		Statement statement = null;
		int cnt = 0;

		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Book List <id>");
			return -1;
		}

		try {
			// Select row in the Book table with ID
			// TODO is ID numeric?
			String sql = "SELECT * FROM Book WHERE id = "+ args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
			while (result.next()) {
				cnt++;
				int id = result.getInt("id");
				String title = result.getString("title");
				String author = result.getString("author");
				int retailPrice = result.getInt("retailPrice");
				int stockQuantity = result.getInt("stockQuantity");
				System.out.println(cnt+"\tID: "+id+"\tTitle: "+title+"\tAuthor: "+author+"\tPrice: $"+retailPrice+"\tQty: "+stockQuantity);
			}

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // listBook

	/*
	 * Method: updateBook
	 * 
	 * Execute the command to update Book with ID with the given values
	 * 
	 * Input:
	 * args[0] = "book"
	 * args[1] = "update"
	 * args[2] = <id> 
	 * args[3] = <title>
	 * args[4] = <author>
	 * args[5] = <retail price>
	 * args[6] = <quantiy>
	 *
	 * Returns:
	 * -1 = book not updated
	 */
	private int updateBook(String[] args) {

		Statement statement = null;
		
		// do we have enough parameters to continue?
		if (args.length < 7) {
			System.out.println("Command Missing Parameters - usage: Book Update <id> <title> <author> <retail price> <quantity>");
			return -1;
		}

		try {

			// Create and execute the UPDATE SQL statement
			// TODO don't require update of all attributes (ex quantity my stay constant)
			// TODO title may span multiple args
			// TODO author may span multiple args
			String sql = "UPDATE Book SET title='" + args[3]+"', author='"+args[4]+"', retailPrice="+args[5]+
					", stockQuantity="+args[6]+" WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Book was updated
			System.out.println("Updated "+ cnt + " Book with ID " + args[2] + " in Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // updateBook
	
	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (BookCmds t : BookCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
