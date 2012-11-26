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
import java.io.FileReader;
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
	
	private Calendar billDate = null;
	private Calendar startDate = null;
	private int custId = 0;


	// Constructor
	public Billing(Connection connection){
	  super(connection);
	  try {
	    FileReader reader = new FileReader(new File("templates/customer_bill.txt"));
	    BufferedReader br = new BufferedReader(reader);

	    String line = br.readLine();
	    StringBuffer buff = new StringBuffer();
	    while(line != null){
	      buff.append(line);
	      buff.append('\n');
	      line = br.readLine();
	    }
	    br.close();
	    
	    customerBillTemplate = buff.toString();
	  } catch (Exception e) {
	    e.printStackTrace();
	    exitProgram("Could not load template\n" + e.getLocalizedMessage());
	  }
	}



	public int exec(String[] args){

		if (args.length < 3){
			usage();
			return -1;
		}

		try {
			switch (BillingCmds.valueOf(args[1].toUpperCase())) {
			case CUSTOMER:
			     HashMap<String, String> subs = new HashMap<String, String>();
			     custId = Integer.parseInt(args[2]);
			     fillCustomerInfo(subs);
				fillOrdersSection(args, subs);
				fillTotal(args, subs);
				String bill = customerBillTemplate;
				
				// Fill out template with values and print
				for (String key : subs.keySet()) {
				  bill = bill.replaceAll("\\{"+key+"\\}", subs.get(key));
                    }
				
				System.out.println(bill);
				//Maybe optionally write it to file??  We can just pipe standard output to file anyway.
				
				break;

			case VENDOR:
				// Creating a Vendor Payment

//               PreparedStatement purchasesStmt = prepareStatement("SELECT " +
//               "purchase.id, " +
//               "vendor.name AS Vendor, " +
//               "book.title, " +
//               "book.author AS Author, " +
//               "staff.name AS Staff, " +
//               "quantity, " +
//               "status, " +
//               "wholesalePrice, " +
//               "purchase.orderDate " +
//               "FROM Purchase, Vendor, Book, Staff " +
//               "WHERE purchase.bookId = book.id " +
//                 "AND purchase.vendorId = vendor.id " +
//                 "AND purchase.staffID = staff.id " +
//                 "AND vendor.id = 3001" +
//                 "AND purchase.orderDate >= ? AND purchase.orderDate <= ?;");

				break;

			} // switch

		} catch (Exception e) {
			usage();
			e.printStackTrace();
			exitProgram("Caught exception \n" + e.getLocalizedMessage());
		}

		return 0;
		
	}


	private void fillTotal(String[] args, HashMap<String, String> subs) throws SQLException {
	  PreparedStatement totalStmt = prepareStatement("SELECT " +
	      "SUM(salePrice * quantity) AS subtotal, " +
	      "SUM(salePrice * quantity)*0.06 as tax, " +
	      "SUM(salePrice * quantity)*0.06 + " +
	      "SUM(salePrice * quantity) as total FROM Book, ItemOrder " +
	      "WHERE bookId = id " +
	      "AND orderId IN " +
	        "(SELECT DISTINCT id FROM Orders WHERE orderDate > ? AND CustomerId = ?)");    
	  totalStmt.setDate(1, new Date(startDate.getTimeInMillis()));
	  totalStmt.setInt(2, custId);
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



  private void fillOrdersSection(String[] args, HashMap<String, String> subs) throws ParseException,
  SQLException {
    
    SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
    billDate = Calendar.getInstance();
    startDate = Calendar.getInstance();
    if(args.length > 3){  // We must have included a Date for billing
      billDate.setTime(format.parse(args[3]));
    } else {
      billDate.setTime(new java.util.Date()); // now
    }

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

    subs.put("billDate", format.format(billDate.getTime()));
    PreparedStatement purchasesStmt = prepareStatement("SELECT title, author, quantity, salePrice FROM Book, ItemOrder WHERE bookId = id AND orderId IN (SELECT DISTINCT id FROM Orders WHERE orderDate > ? AND CustomerId = ?)");

    purchasesStmt.setDate(1, new Date(startDate.getTimeInMillis()));
    purchasesStmt.setInt(2, custId);
    ResultSet purchases = purchasesStmt.executeQuery();
    StringBuffer purchaseBuff = new StringBuffer();
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



  private void fillCustomerInfo(HashMap<String, String> subs) throws SQLException {
    PreparedStatement customerInfoStmt = prepareStatement("SELECT name, address, phone FROM Customer WHERE id = ?");
    customerInfoStmt.setInt(1, custId);
    customerInfoStmt.execute();
    subs.put("invoiceNumber", custId + "-" + System.currentTimeMillis());  //pseudo unique bill identifier
    ResultSet results = customerInfoStmt.getResultSet();
    if(results.next()){
      subs.put("customerName", results.getString("name"));
      subs.put("customerPhone", results.getString("phone"));
      subs.put("customerAddress", results.getString("address"));
    }
  }

  private static void usage() {
    System.out.println("Subcommand Required. Legal values:");
    for (BillingCmds t : BillingCmds.values()) {
      System.out.println(t.toString());
    }
  }

}
