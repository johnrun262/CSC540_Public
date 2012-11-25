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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Purchase {

	private Connection connection = null;

	private static enum PurchaseCmds {ADD, ALL, DELETE, LIST, UPDATE};

	// Constructor
	Purchase(Connection connection){

		this.connection = connection; 

	}



	public int exec(String[] args){

		if (args.length < 2){
			usage();
			return -1;
		}

		try {
			switch (PurchaseCmds.valueOf(args[1].toUpperCase())) {
			case ADD:
				// create a new purchase from a vendor

				return(addPurchase(args));

			case ALL:
				// dump all purchases

				return(allPurchases(args));

			case DELETE:
				// delete a purchase made from a vendor

				return(deletePurchase(args));

			case LIST:
				// list purchases made from a vendor

				return(listPurchase(args));

			case UPDATE:
				// Update a purchase made from a vendor

				return(updatePurchase(args));

			} // switch

		} catch (IllegalArgumentException e) {
			usage();
			return -1;
		}

		return 0;

	}

	/*
	 * Method: addPurchase
	 * 
	 * Execute the command to create a book purchase from a vendor. The purchases  
	 * is inserted with an "ordered" status and todays date
	 * 
	 * Input:
	 * args[0] = "purchase"
	 * args[1] = "add" 
	 * args[2] = <bookid>
	 * args[3] = <vendorid>
	 * args[4] = <staffid>
	 * args[5] = <quantity>
	 * args[6] = <wholesale price>
	 *
	 * Returns:
	 * -1 = purchase not created
	 */
	private int addPurchase(String[] args) {

		Statement statement = null;
		int newID = 6001;

		// do we have enough parameters to continue? note that ssn is optional
		if (args.length < 7) {
			System.out.println("Command Missing Parameters - usage: Purchase Add <book id> <vendor id> <staff id> <quantity> <wholesale price>");
			return -1;
		}

		String bookId = args[2];
		String vendorId = args[3];
		String staffId = args[4];
		String qty = args[5];
		String price = args[6];

		// validate input parameters
		try {

			// check book ID numeric
			try {
				Integer.parseInt(bookId);
			} catch (Exception e) {
				System.out.println("Book Id must be numeric");
				return -1;
			}
			
			// check vendor ID numeric
			try {
				Integer.parseInt(vendorId);
			} catch (Exception e) {
				System.out.println("Vendor Id must be numeric");
				return -1;
			}
			
			// check staff ID numeric
			try {
				Integer.parseInt(staffId);
			} catch (Exception e) {
				System.out.println("Staff Id must be numeric");
				return -1;
			}
			
			// check price numeric and > 0
			if (Integer.parseInt(price) <= 0) {
				System.out.println("Wholesale price must be greater than zero");
				return -1;
			}

			// check quantity numeric and > 0
			if (Integer.parseInt(qty) <= 0) {
				System.out.println("Order quantity must be greater than zero");
				return -1;
			}

		} catch (Exception e) {
			System.out.println("Invalid Format Parameter");
			return -1;
		}

		try {
			// Book ID must be in book table
			String sql = "SELECT id FROM Book Where id='"+bookId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(bookId)) {
					System.out.println("Book Id must be in database: "+ bookId);
					return -1;
				}
			} else {
				System.out.println("Book Id must be in database: "+ bookId);
				return -1;
			}

			// Vendor ID must be in vendor table
			sql = "SELECT id FROM Vendor Where id='"+vendorId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(vendorId)) {
					System.out.println("Vendor Id must be in database: "+ vendorId);
					return -1;
				}
			} else {
				System.out.println("Vendor Id must be in database: "+ vendorId);
				return -1;
			}

			// Staff ID must be in staff table
			sql = "SELECT id FROM Staff Where id='"+staffId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(staffId)) {
					System.out.println("Staff Id must be in database: "+ staffId);
					return -1;
				}
			} else {
				System.out.println("Staff Id must be in database: "+ staffId);
				return -1;
			}

			// Get the last ID assigned and add one to it to create a new ID for this vendor
			// TODO somehow lock others out of insert to prevent duplicate ID
			sql = "SELECT MAX(id) AS max FROM Purchase";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				newID = result.getInt("max");
				newID++;
			}

			Date todaysDate = new java.util.Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			String date = formatter.format(todaysDate);

			// Create and execute the INSERT SQL statement
			// TODO address can be spread over multiple args - may be working with quotes on cmd line
			sql = "INSERT INTO Purchase VALUES ("+newID+", '"+date+"','"+bookId+"', '"+vendorId+"', '"+staffId+"','"+qty+"', 'ordered', '"+price+"')";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Customer was inserted and the ID
			System.out.println("Inserted "+ cnt + " Purchase with ID " + newID + " into Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // addPurchase

	/*
	 * Method: allPurchases
	 * 
	 * Execute the command to dump Customers
	 * 
	 * Input:
	 * args[0] = "purchase"
	 * args[1] = "all" 
	 *
	 * Returns:
	 * -1 = error processing request
	 */
	private int allPurchases(String[] args) {

		Statement statement = null;
		int cnt = 0;

		try {
			// Select all rows in the Customer table and sort by ID
			String sql = "SELECT * FROM Purchase ORDER BY id";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

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

			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // allPurchases


	/*
	 * Method: deletePurchase
	 * 
	 * Execute the command to delete a Purchase by ID
	 * 
	 * Input:
	 * args[0] = "purchase"
	 * args[1] = "delete" 
	 * args[2] = <id>
	 *
	 * Returns:
	 * -1 = purchase not deleted
	 */
	private int deletePurchase(String[] args) {

		Statement statement = null;

		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Purchase Delete <id>");
			return -1;
		}

		String purchaseId = args[2];
		
		// check purchase ID numeric
		try {
			Integer.parseInt(purchaseId);
		} catch (Exception e) {
			System.out.println("Purchase Id must be numeric");
			return -1;
		}
		
		try { 

			// Create and execute the DELETE SQL statement
			// TODO validate id numeric
			String sql = "DELETE FROM Purchase WHERE id="+purchaseId;

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Vendor was inserted and the ID
			System.out.println("Deleted "+ cnt + " Purchase(s) with ID " + purchaseId + " from Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // deletePurchase

	/*
	 * Method: listPurchase
	 * 
	 * Execute the command to list info about a Purchase given ID
	 * 
	 * Input:
	 * args[0] = "purchase"
	 * args[1] = "list"
	 * args[2] = <id> 
	 *
	 * Returns:
	 * -1 = error retrieving customer
	 */
	private int listPurchase(String[] args) {

		Statement statement = null;
		int cnt = 0;

		// do we have enough parameters to continue?
		if (args.length < 3) {
			System.out.println("Command Missing Parameters - usage: Purchase List <id>");
			return -1;
		}

		String purchaseId = args[2];
		
		// check purchase ID numeric
		try {
			Integer.parseInt(purchaseId);
		} catch (Exception e) {
			System.out.println("Purchase Id must be numeric");
			return -1;
		}
		
		try {
			// Select row in the purchase table with ID
			// TODO is ID numeric?
			String sql = "SELECT * FROM Purchase WHERE id = "+ purchaseId;

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			// loop through the result set printing attributes
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
			
			System.out.println(cnt+" Row(s) Returned");

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}	

	} // listPurchase

	/*
	 * Method: updatePurchase
	 * 
	 * Execute the command to update purchase with ID with the given values
	 * 
	 * Input:
	 * args[0] = "vendor"
	 * args[1] = "update"
	 * args[2] = <id> 
	 * args[3] = <bookid>
	 * args[4] = <vendorid>
	 * args[5] = <staffid>
	 * args[6] = <quantity>
	 * args[7] = <wholesale price>
	 * args[8] = <order date>
	 * args[9] = <status>
	 *
	 * Returns:
	 * -1 = purchase not updated
	 */
	private int updatePurchase(String[] args) {

		Statement statement = null;
		
		// do we have enough parameters to continue? note that ssn is optional
		if (args.length < 10) {
			System.out.println("Command Missing Parameters - usage: Purchase Update <purchase id> <book id> <vendor id> <staff id> <quantity> <wholesale price> <order date> <status>");
			return -1;
		}

		String purId = args[2];
		String bookId = args[3];
		String vendorId = args[4];
		String staffId = args[5];
		String qty = args[6];
		String price = args[7];
		String date = args[8];
		String status = args[9];

		// validate input parameters
		try {
			
			// check purchase ID numeric
			try {
				Integer.parseInt(purId);
			} catch (Exception e) {
				System.out.println("Purchase Id must be numeric");
				return -1;
			}
			
			// check book ID numeric
			try {
				Integer.parseInt(bookId);
			} catch (Exception e) {
				System.out.println("Book Id must be numeric");
				return -1;
			}
			
			// check vendor ID numeric
			try {
				Integer.parseInt(vendorId);
			} catch (Exception e) {
				System.out.println("Vendor Id must be numeric");
				return -1;
			}
			
			// check staff ID numeric
			try {
				Integer.parseInt(staffId);
			} catch (Exception e) {
				System.out.println("Staff Id must be numeric");
				return -1;
			}
			
			// validate order date
			try {
				SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
				format.parse(date);
				// TODO check year is say >1900
			} catch (Exception e) {
				System.out.println("Invalid Format Order Date: expecting dd-MMM-yyyy (ex 12-dec-1960) found "+date);
				return -1;
			}
			
			// check price numeric and > 0
			if (Integer.parseInt(price) <= 0) {
				System.out.println("Wholesale price must be greater than zero");
				return -1;
			}

			// check quantity numeric and > 0
			if (Integer.parseInt(qty) <= 0) {
				System.out.println("Order quantity must be greater than zero");
				return -1;
			}

			// Status must be ordered, received or shipped
			if ((status = checkStatus(status)) == null) {
				return -1;
			}
			
		} catch (Exception e) {
			System.out.println("Invalid Format Parameter");
			return -1;
		}

		try {
			// Book ID must be in book table
			String sql = "SELECT id FROM Book Where id='"+bookId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			ResultSet result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(bookId)) {
					System.out.println("Book Id must be in database: "+ bookId);
					return -1;
				}
			} else {
				System.out.println("Book Id must be in database: "+ bookId);
				return -1;
			}

			// Vendor ID must be in vendor table
			sql = "SELECT id FROM Vendor Where id='"+vendorId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(vendorId)) {
					System.out.println("Vendor Id must be in database: "+ vendorId);
					return -1;
				}
			} else {
				System.out.println("Vendor Id must be in database: "+ vendorId);
				return -1;
			}

			// Staff ID must be in staff table
			sql = "SELECT id FROM Staff Where id='"+staffId+"'";

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			result = statement.executeQuery(sql);

			if (result.next()) {
				int id = result.getInt("id");
				if (id != Integer.parseInt(staffId)) {
					System.out.println("Staff Id must be in database: "+ staffId);
					return -1;
				}
			} else {
				System.out.println("Staff Id must be in database: "+ staffId);
				return -1;
			}

			// Create and execute the UPDATE SQL statement
			// TODO don't require update of all attributes
			sql = "UPDATE Purchase SET id='" +purId+"', orderDate='"+date+"', bookId='"+bookId+"', vendorId='"+vendorId+"', staffId='"+staffId+"', quantity='"+qty+"', status='"+status+"', wholesalePrice='"+price+"' "+
					"WHERE id="+args[2];

			statement = connection.createStatement();
			statement.setQueryTimeout(10);
			int cnt = statement.executeUpdate(sql);

			// Tell the user the Purchase was updated and the ID
			System.out.println("Updated "+ cnt + " Purchase(s) with ID " + args[2] + " in Database"); 

			return 0;

		} catch (Exception e) {
			System.out.println("Exception Processing Command: " + e.getMessage());
			return -1;
		}

	} // updatePurchase

	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (PurchaseCmds t : PurchaseCmds.values()) {
			System.out.println(t.toString());
		}
	}
	
	// check the status passed from user is valid
	private String checkStatus(String status) {

		try {
			if (status.toUpperCase().startsWith("O")) {
				return "ordered";
			}

			if (status.toUpperCase().startsWith("R")) {
				return "received";
			}

			if (status.toUpperCase().startsWith("S")) {
				return "shipped";
			}
					
		} catch (Exception e) {
		}

		System.out.println("Invalid Status - Valid values are: (O)rdered, (R)eceived, (S)hipped");

		return null;
				
	} // checkStatus
	
}
