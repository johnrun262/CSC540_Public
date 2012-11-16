
public class BooksCmd {

  /**
   * @param args An array of all the command line arguments passed to BooksCmd.
   */
  public static void main(String[] args) {
    int idx = 0;
    for (String string : args) {
      System.out.println("Argument "+ idx++ +": " + string);
    }
    
  }

}
