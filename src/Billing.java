/*****************************************************************************************
 * 
 * Billing.java
 *  
 * Aaron Averill
 * John Lloyd
 * Matthew Marum
 * Rob Parsons
 * 
 * CSC 540
 * Fall 2012
 * 
 * Billing.java creates payments to a vendor and bills for a customer.
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;



public class Billing extends AbstractCommandHandler{

	private static enum BillingCmds {CUSTOMER, VENDOR};

	private String customerBillTemplate = null;
	private String vendorPaymentTemplate = null;

	private Calendar billDate = null;
	private Calendar startDate = null;
	private SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");


	// Constructor
	public Billing(Connection connection){
		super(connection);
		try {
			File billTemplate = new File("templates/customer_bill.txt");
			StringBuffer buff = readFile(billTemplate);
			customerBillTemplate = buff.toString();

			File vendorTemplate = new File("templates/vendor_payment.txt");
			buff = readFile(vendorTemplate);
			vendorPaymentTemplate = buff.toString();


		} catch (Exception e) {
			e.printStackTrace();
			exitProgram("Could not load template\n" + e.getLocalizedMessage());
		}
	}



	private StringBuffer readFile(File billTemplate) throws FileNotFoundException, IOException {
		FileReader reader = new FileReader(billTemplate);
		BufferedReader br = new BufferedReader(reader);

		String line = br.readLine();
		StringBuffer buff = new StringBuffer();
		while(line != null){
			buff.append(line);
			buff.append('\n');
			line = br.readLine();
		}
		br.close();
		return buff;
	}


	public void execCustomer(@Param("Customer Id") String custId,
			@Param(value="Start Date", optional=true) String argStartDate) throws ValidationException, SQLException {

		int custIdValue;

		// validate input parameters
		try {
			// check customer ID numeric and in database
			custIdValue = ValidationHelpers.checkId(connection, custId, ValidationHelpers.TABLE_CUSTOMER);
		} catch (ValidationException ex) {
			System.out.println("Validation Error: " + ex.getMessage());
			return;
		}

		try {
			HashMap<String, String> subs = new HashMap<String, String>();

			fillCustomerInfo(custIdValue, subs);
			fillOrdersSection(custIdValue, argStartDate, subs);
			fillBillTotal(custIdValue, subs);

			String bill = customerBillTemplate;
			// Fill out template with values and print
			for (String key : subs.keySet()) {
				bill = bill.replaceAll("\\{"+key+"\\}", subs.get(key));
			}

			System.out.println("====");
			System.out.println(bill);
			System.out.println("====");
			//Maybe optionally write it to file??  We can just pipe standard output to file anyway.

		} catch (Exception e) {
			usage();
			System.out.println("Caught exception \n" + e.getLocalizedMessage());
		}

		return;

	}


	public int execVendor(String[] args){

		/*		  if (args.length < 3){
		    usage();
		    return -1;
		  }

		  try {
		    HashMap<String, String> subs = new HashMap<String, String>();
		    switch (BillingCmds.valueOf(args[1].toUpperCase())) {
		    case CUSTOMER:
		      identifier = Integer.parseInt(args[2]);
		      fillCustomerInfo(subs);
		      fillOrdersSection(args, subs);
		      fillBillTotal(args, subs);

		      String bill = customerBillTemplate;
		      // Fill out template with values and print
		      for (String key : subs.keySet()) {
		        bill = bill.replaceAll("\\{"+key+"\\}", subs.get(key));
		      }

		      System.out.println("====");
		      System.out.println(bill);
		      System.out.println("====");
		      //Maybe optionally write it to file??  We can just pipe standard output to file anyway.

		      break;

		    case VENDOR:
		      // Creating a Vendor Payment
		      identifier = Integer.parseInt(args[2]);
		      fillVendorInfo(subs);
		      fillPurchasesSection(args,subs);
		      fillPaymentTotal(args,subs);

	           String payment = vendorPaymentTemplate;

	           // Fill out template with values and print
	           for (String key : subs.keySet()) {
	             payment = payment.replaceAll("\\{"+key+"\\}", subs.get(key));
	           }
	           System.out.println("====");
	           System.out.println(payment);
	           System.out.println("====");
		      break;

		    } // switch

		  } catch (Exception e) {
		    usage();
		    e.printStackTrace();
		    exitProgram("Caught exception \n" + e.getLocalizedMessage());
		  }*/

		return 0;

	}

	private void fillBillTotal(int id, HashMap<String, String> subs) throws SQLException {
		PreparedStatement totalStmt = prepareStatement("SELECT " +
				"SUM(salePrice * quantity) AS subtotal, " +
				"SUM(salePrice * quantity)*0.06 as tax, " +
				"SUM(salePrice * quantity)*0.06 + " +
				"SUM(salePrice * quantity) as total FROM Book, ItemOrder " +
				"WHERE bookId = id " +
				"AND orderId IN " +
				"(SELECT DISTINCT id FROM Orders WHERE orderDate > ? AND CustomerId = ? AND status <> 'paid')");    
		totalStmt.setDate(1, new Date(startDate.getTimeInMillis()));
		totalStmt.setInt(2, id);
		ResultSet set = totalStmt.executeQuery();
		if(set.next()){
			subs.put("subtotal", "\\$"+new DecimalFormat("0.00").format(set.getDouble("subtotal")));
			subs.put("tax", "\\$"+new DecimalFormat("0.00").format(set.getDouble("tax")));
			subs.put("total", "\\$"+new DecimalFormat("0.00").format(set.getDouble("total")));
		} else {  // Still can produce a bill for no sales.
			subs.put("subtotal", "\\$0.00");
			subs.put("tax", "\\$0.00");
			subs.put("total", "\\$0.00");
		}


	}

	private void fillPaymentTotal(int id, HashMap<String, String> subs) throws SQLException {
		PreparedStatement totalStmt = prepareStatement("SELECT SUM(wholesalePrice * quantity) AS total FROM Book b, Purchase p WHERE bookId = b.id AND p.id IN (SELECT DISTINCT id FROM Purchase WHERE orderDate > ? AND VendorId = ?)");    
		totalStmt.setDate(1, new Date(startDate.getTimeInMillis()));
		totalStmt.setInt(2, id);
		ResultSet set = totalStmt.executeQuery();
		if(set.next()){
			subs.put("total", "\\$"+new DecimalFormat("0.00").format(set.getDouble("total")));
		} else {  // Still can produce a bill for no sales.
			subs.put("total", "\\$0.00");
		}


	}

	private void fillPurchasesSection(int id, String argStartDate, HashMap<String, String> subs) throws ParseException,
	SQLException {

		setupDates(argStartDate);
		subs.put("billDate", format.format(billDate.getTime()));
		PreparedStatement purchasesStmt = prepareStatement("SELECT title, author, quantity, wholesalePrice FROM Book b, Purchase p WHERE bookId = b.id AND p.id IN (SELECT DISTINCT id FROM Purchase WHERE orderDate > ? AND VendorId = ?)");

		purchasesStmt.setDate(1, new Date(startDate.getTimeInMillis()));
		purchasesStmt.setInt(2, id);
		ResultSet purchases = purchasesStmt.executeQuery();
		StringBuffer purchaseBuff = new StringBuffer();
		purchaseBuff.append("#\tTitle\t\tAuthor\t\tPrice\n");
		while (purchases.next()) {
			purchaseBuff.append(purchases.getString("quantity"));
			purchaseBuff.append('\t');
			purchaseBuff.append(purchases.getString("title"));
			purchaseBuff.append('\t');
			purchaseBuff.append(purchases.getString("author"));
			purchaseBuff.append('\t');
			purchaseBuff.append("\\$"+new DecimalFormat("0.00").format(purchases.getDouble("wholesalePrice")));
			purchaseBuff.append('\n');

		}
		subs.put("purchaseList", purchaseBuff.toString());
	}

	private void fillOrdersSection(int id, String argStartDate, HashMap<String, String> subs) throws ParseException,
	SQLException {

		SimpleDateFormat format = setupDates(argStartDate);
		subs.put("billDate", format.format(billDate.getTime()));
		PreparedStatement purchasesStmt = prepareStatement(
				"SELECT title, author, quantity, salePrice "+
						"FROM Book, ItemOrder "+
						" WHERE bookId = id AND orderId IN " +
						" (SELECT DISTINCT id FROM Orders WHERE orderDate > ? AND CustomerId = ? AND status <> 'paid')");

		purchasesStmt.setDate(1, new Date(startDate.getTimeInMillis()));
		purchasesStmt.setInt(2, id);
		ResultSet purchases = purchasesStmt.executeQuery();
		StringBuffer purchaseBuff = new StringBuffer();
		purchaseBuff.append("#\tTitle\t\tAuthor\t\tPrice\n");
		while (purchases.next()) {
			String book = purchases.getString("title");
			String author = purchases.getString("author");
			double sale = purchases.getDouble("salePrice");
			int qty = purchases.getInt("quantity");
			purchaseBuff.append(
					qty+
					"\t"+book+
					"\t"+author+
					"\t\\$"+new DecimalFormat("0.00").format(sale)+ "\n");
		}
		subs.put("orderList", purchaseBuff.toString());
	}



	private SimpleDateFormat setupDates(String argStartDate) throws ParseException {

		billDate = Calendar.getInstance();
		startDate = Calendar.getInstance();
		billDate.setTime(new java.util.Date()); // now

		try {
			startDate.setTime(format.parse(argStartDate));
		} catch (Exception e) {

			System.out.println("No date specified, using last 30 days.");

			int month = billDate.get(Calendar.MONTH);
			if(month == 0){  // We are looking for purchases during last month
				month = Calendar.DECEMBER;
				startDate.setTime(billDate.getTime());
				startDate.set(Calendar.MONTH, Calendar.DECEMBER);
				startDate.set(Calendar.YEAR, billDate.get(Calendar.YEAR)-1);
			} else {
				month --;
				startDate.setTime(billDate.getTime());
				startDate.set(Calendar.MONTH, month);
			}
		}
		System.out.println("Generating billing since " +format.format(startDate.getTime()));
		return format;
	}

	private void fillVendorInfo(int id, HashMap<String, String> subs) throws SQLException {
		PreparedStatement customerInfoStmt = prepareStatement("SELECT name, address, phone FROM Vendor WHERE id = ?");
		customerInfoStmt.setInt(1, id);
		customerInfoStmt.execute();
		subs.put("invoiceNumber", id + "-" + System.currentTimeMillis());  //pseudo unique bill identifier
		ResultSet results = customerInfoStmt.getResultSet();
		if(results.next()){
			subs.put("vendorName", results.getString("name"));
			subs.put("vendorPhone", results.getString("phone"));
			subs.put("vendorAddress", results.getString("address"));
		}
		if(subs.get("vendorName") == null){
			System.out.println("Vendor does not exist.\n Vendor Id: "+id);
		}
	}

	private void fillCustomerInfo(int id, HashMap<String, String> subs) throws SQLException {
		PreparedStatement customerInfoStmt = prepareStatement("SELECT name, address, phone FROM Customer WHERE id = ?");
		customerInfoStmt.setInt(1, id);
		customerInfoStmt.execute();
		subs.put("invoiceNumber", id + "-" + System.currentTimeMillis());  //pseudo unique bill identifier
		ResultSet results = customerInfoStmt.getResultSet();
		if(results.next()){
			subs.put("customerName", results.getString("name"));
			subs.put("customerPhone", results.getString("phone"));
			subs.put("customerAddress", results.getString("address"));
		}
		if(subs.get("customerName") == null){
			System.out.println("Customer does not exist.\n Customer Id: "+id);
			return;
		}
	}

	private static void usage() {
		System.out.println("Subcommand Required. Legal values:");
		for (BillingCmds t : BillingCmds.values()) {
			System.out.println(t.toString());
		}
	}

}
