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
			@Param("Begin Date") String beginDate, 
			@Param("End Date") String endDate) throws SQLException {

		// Create a hash map
		//Map<String, String> args = new HashMap<String, String>();
		//m.put(args1);

		String s1 = "SELECT co.customerId, co.orderDate, bk.title, co.salePrice, co.quantity FROM Book bk, (SELECT DISTINCT * FROM Orders NATURAL JOIN ItemOrder WHERE orderId=id AND customerId=";
		// s2 = Customer ID like 2001
		String s3 = ") co WHERE bk.id=co.bookId AND (co.orderDate >= '";
		// s4 = Beginning date of date range like 24-dec-2011
		String s5 = "' AND co.orderDate <= '";
		// s6 = Ending data of date range like 22-oct-2012
		String s7 = "')";

		String q = s1+customerId+s3+beginDate+s5+endDate+s7;

		// Test
		System.out.println(q);

		try {

			// Create a statement instance that will be sending
			// your SQL statements to the DBMS
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(q);

			System.out.println(); // skip a line
			while (result.next()) {

				int cid = result.getInt("customerId");
				String ord = result.getString("orderDate");
				String tit = result.getString("title");
				String sal = result.getString("salePrice");
				//int ret = result.getInt("retailPrice");
				int qua = result.getInt("quantity");

				System.out.println(cid + " " + ord + " " + tit + " " + sal + " " + qua);
				//System.out.println(tit);
			}

		} catch(Exception ex) {
			System.out.println("Error Creating Purchase History: " + ex.getMessage());
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

	public void execSales(@Param("Staff Id") String staffId, 
			@Param("Begin Date") String beginDate, 
			@Param("End Date") String endDate) throws SQLException {

		String s1 = "SELECT id, name, address, gender, dob, status, phone, ssn FROM Customer, (SELECT DISTINCT customerId cid FROM Orders WHERE staffId=";
		// s2 = Staff ID like 1001
		String s3 = " AND (orderDate >= '";
		// s4 = Beginning date of date range like 24-dec-2011
		String s5 = "' AND orderDate <= '";
		// s6 = Ending data of date range like 22-oct-2012
		String s7 = "')) WHERE id=cid";

		String q = s1+staffId+s3+beginDate+s5+endDate+s7;

		// Test
		System.out.println(q);

		try {
			// Create a statement instance that will be sending
			// your SQL statements to the DBMS
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(q);

			System.out.println(); // skip a line
			while (result.next()) {

				//String ord = result.getString("orderDate");
				//int sid = result.getInt("staffId");
				String snm = result.getString("name");
				//int cid = result.getInt("cid");
				//String cnm = result.getString("cname");

				//System.out.println(ord + " " + sid + "  " + snm + "  " +  cid + "  " + cnm);
				System.out.println(snm);
			}

		} catch(Exception ex) {
			System.out.println("Error Creating Sales History: " + ex.getMessage());
		}

		return;
	} // execSalesAssist

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

	public void execRoles(@Param("Job Title") String jobTitle) throws SQLException {

		String s1 = "SELECT * FROM Staff WHERE jobTitle LIKE '%";
		// s2 = Staff role
		String s3 = "%'";

		String q = s1+jobTitle+s3;

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
				String nam = result.getString("name");
				String gen = result.getString("gender");
				String dat = result.getString("dob");
				String job = result.getString("jobTitle");
				String dep = result.getString("department");
				int sal = result.getInt("salary");
				String pho = result.getString("phone");
				String add = result.getString("address");

				System.out.println(id + "  " + nam + " " + gen + " " + dat + "  " + job + " " + dep + " " + sal + " " + pho + " " + add);
			}

		} catch(Exception ex) {
			System.out.println("Error Creating Vendor History: " + ex.getMessage());
		}

		return;
		
	} // execStaffRoles

}
