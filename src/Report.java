/*****************************************************************************************
 * 
 * Report.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Report.java creates the reports that:
 * 	Report the purchase history for a given customer and for a certain time period 
 * 	Return information on all the customers a given salesperson assisted during a certain time period
 * 	Return information on all the vendors a particular store has contract with.
 * 	Return information on Books-A-Thousand staff grouped by their role.
 * 
 * 
Example CLI:

BooksCmd report purchases 2001 24-dec-2011 22-oct-2012
BooksCmd report assistance 1001 24-dec-2011 22-oct-2012
BooksCmd report contracts
BooksCmd report roles Salesperson

-- Staff (id, name, gender, dob, jobTitle, department, salary, phone, address)
-- Customer (id, gender, dob, status, phone, ssn, name, address)
-- Vendor (id, phone, name, address)
-- Book (id, retailPrice, stockQuantity, title, author)
-- Orders (id, staffId, customerId, status, orderDate)
-- ItemOrder (orderId, bookId, salePrice, quantity)
-- Purchase (id, orderDate, bookId, vendorId, staffId, quantity, status, wholesalePrice) 
-- Stocks (bookId, vendorId)

-- Report the purchase history for a given customer and for a certain time period (day/month/year)
-- The constants used for customerId, and orderDate range will be replaced by user entered values.

SELECT co.customerId, co.orderDate, bk.title, co.salePrice, co.quantity FROM Book bk, (SELECT DISTINCT * FROM Orders NATURAL JOIN ItemOrder WHERE orderId=id AND customerId=2001) co WHERE bk.id=co.bookId AND (co.orderDate >= '24-dec-2011' AND co.orderDate <= '22-oct-2012');

-- Return information on all the customers a given salesperson assisted during a certain time period
-- The constants used for staffId, and orderDate range will be replaced by user entered values.

SELECT id, name, address, gender, dob, status, phone, ssn FROM Customer, (SELECT DISTINCT customerId cid FROM Orders WHERE staffId=1001 AND (orderDate >= '24-dec-2011' AND orderDate <= '22-oct-2012')) WHERE id=cid;

-- Return information on all the vendors a particular store has contract with.
SELECT DISTINCT * FROM Vendor;

-- Return information on Books-A-Thousand staff grouped by their role.
SELECT * FROM Staff WHERE jobTitle='Salesperson';
SELECT * FROM Staff WHERE jobTitle='Procurement';
SELECT * FROM Staff WHERE jobTitle='Warehouse staff';
SELECT * FROM Staff WHERE jobTitle='Accounting';
SELECT * FROM Staff WHERE jobTitle='Management';

 */


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Report extends AbstractCommandHandler {

	// Constructor
	public Report(Connection connection){
		super(connection);
	}

	/*
	 * Method: purchaseHistory
	 * 
	 * Report the purchase history for a given customer and for a certain time period (day/month/year)
	 * The constants used for customerId, and orderDate range will be replaced by user entered values.
	 * 
	 * Input:
	 * @param customerId
	 * 	Customer ID
	 * @param beginDate
	 * 	Beginning date of date range
	 * @param endDate
	 * 	Ending date of date range
	 * 
	 * Output:
	 * Print report
	 */

	public void execPurchases(@Param("Customer Id") String customerId, 
			@Param(value="Begin Date", optional=true) String beginDate, 
			@Param(value="End Date", optional=true) String endDate) throws SQLException {

		try {
			// execute method in Sales that displays orders
			Sale sale = new Sale(connection);
			sale.execCustomer(customerId, beginDate, endDate);
		} catch (Exception e) {
			System.out.println("Error Listing Customer Purchases"+ e.getMessage());
		}

		return;
	} // execPurchaseHistory

	/*
	 * Method: execSalesAssist
	 * 
	 * Return information on all the customers a given salesperson assisted during a certain time period.
	 * 
	 * Input:
	 * @param staffId
	 * 	Staff ID
	 * @param  beginDate
	 * 	Beginning date of date range
	 * @param endDate
	 *  Ending date of date range
	 * 
	 * Output:
	 * Print report
	 */

	public void execStaff(@Param("Staff Id") String staffId, 
			@Param(value="Begin Date", optional=true) String beginDate, 
			@Param(value="End Date", optional=true) String endDate) throws SQLException {

		int staffIdValue;

		// Validate the inputs
		try {
			staffIdValue = ValidationHelpers.checkId(connection, staffId, ValidationHelpers.TABLE_STAFF);
			// says check DoB but validates date correct format
			if (beginDate != null) ValidationHelpers.checkDateOfBirth(beginDate);
			if (endDate != null) ValidationHelpers.checkDateOfBirth(endDate);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		// execute method in Staf that displays Staff Members
		System.out.println("Staff Member: ");
		Staff staff = new Staff(connection);
		staff.execList(staffId);

		// get list from orders table of customers served by staff member
		System.out.println("Customers Served: ");

		// build the where clause to select customer orders within date range (if supplied)
		String where = "staffId=" + staffIdValue +" ";
		if (endDate != null) {
			where = where + "AND orderDate <= '"+ endDate + "' ";	
		}
		if (beginDate != null) {
			where = where + "AND orderDate >= '"+ beginDate + "' ";	
		}

		String sql = "SELECT DISTINCT customerId FROM Orders WHERE "+where+" Order by customerId";

		try {
			// Create a statement instance that will be sending
			// your SQL statements to the DBMS
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(sql);

			// get a list of customers served by the staff member and then
			// loop through list printing details of customer
			while (result.next()) {

				int customerId = result.getInt("customerId");

				// execute method in Customers that displays customers
				Customer cust = new Customer(connection);
				cust.execList(Integer.toString(customerId));
			}

		} catch(Exception ex) {
			System.out.println("Error Creating Customers Served History: " + ex.getMessage());
		}

		return;

	} // execStaff

	/*
	 * Method: execVendorContracts
	 * 
	 * Return information on all the vendors a particular store has contract with.
	 * 
	 * Input:
	 * None
	 * 
	 * Output:
	 * Print report
	 */

	public void execVendors() throws SQLException {

		String s1 = "SELECT DISTINCT * FROM Vendor";

		String q = s1;

		// Test
		System.out.println(q);

		try {

			// Create a statement instance that will be sending
			// your SQL statements to the DBMS
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(q);

			System.out.println(); // skip a line
			while (result.next()) {

				int id = result.getInt("id");
				String pho = result.getString("phone");
				String nam = result.getString("name");
				String add = result.getString("address");

				System.out.println(id + "  " + pho + " " + nam + "  " + add);
			}

		} catch(Exception ex) {
			System.out.println("Error Creating Vendor History: " + ex.getMessage());
		}

		return;

	} // execVendorContracts

	/*
	 * Method: staffRoles
	 * 
	 * Return information on Books-A-Thousand staff grouped by their role.
	 * 
	 * Input:
	 * @param  jobTitle
	 * 	Job title of the staff returned
	 * 
	 * Output:
	 * Print report
	 */

	public void execRoles(@Param(value="Job Title", optional=true) String jobTitle) throws SQLException {

		String where = "";
		
		// the user supplied a job title then select staff with titles like it
		if (jobTitle != null) {
			where = " Where jobTitle LIKE '%"+jobTitle+"%' ";
		}
		
		// build the SQL statement to select a list of staff by job title
		String sql = "SELECT id AS staffId, jobTitle FROM Staff "+ where +" Order by jobTitle";

		try {
			// Create a statement instance that will be sending
			// your SQL statements to the DBMS
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);

			String prevJobTitle = null;
			
			// Loop through the rows printing a row for each staff member
			while (result.next()) {

				int staffId = result.getInt("staffId");
				jobTitle = result.getString("jobTitle");
				
				if (!jobTitle.equals(prevJobTitle)) {
					System.out.println("Staff Members with JobTitle: "+ jobTitle);
					System.out.println();
					prevJobTitle = jobTitle;
				}
				
				// execute method in Staff that displays staff
				Staff staff = new Staff(connection);
				staff.execList(Integer.toString(staffId));
			}

		} catch(Exception ex) {
			System.out.println("Error Roles List: " + ex.getMessage());
		}

		return;

	} // execStaffRoles

}
