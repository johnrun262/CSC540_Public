import java.sql.Connection;
import java.sql.DriverManager;


public class BooksCmd {

  private static final String jdbcURL = "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl";

  // Put your oracle ID and password here
  private static final String user = ""; 
  private static final String password = "";

  public static void main(String[] args) {
       
       // Test
       for (int i = 0; i < args.length; i++){
            String s = i + " " + args[i];
            System.out.println(s);
       }

       try {
            // Loading the driver. This creates an instance of the driver
            // and calls the registerDriver method to make Oracle Thin
            // driver, at ora.csc.ncsu.edu, available to clients.

            Class.forName("oracle.jdbc.driver.OracleDriver");

            Connection connection = null;
         
            try {
                 // Get a connection instance from the first driver in the 
                 // DriverManager list that recognizes the URL jdbcURL
                 connection = DriverManager.getConnection(jdbcURL, user, password);
                 
                 if (args[0].equals("report")){

                      Report rpt = new Report(connection);
                      int rc = rpt.Query(args);
                      if (rc == -1){
                           System.out.println("Unknown report request!");
                      }
                 }
                 else{
                      System.out.println("Unknown request!");
                 }
            
            } finally {
                 close(connection);
            }
            
       } catch(Throwable oops) {
            oops.printStackTrace();
       }
  }
  
  static void close(Connection connection) {
       if(connection != null) {
            try { 
                 connection.close(); 
            } catch(Throwable whatever) {}
       }
  }

}
