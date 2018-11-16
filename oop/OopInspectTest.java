public class OopInspectTest{

  private final String testField;

  static{
    System.loadLibrary("ooptest");
  }

  public OopInspectTest(String val){
    testField = val;
  }

  public native void printThisOopAddress();

  public static void main(String[] args) throws Exception{
    OopInspectTest test = new OopInspectTest("from main()");
    test.printThisOopAddress();

    System.out.println("PID = " + ProcessHandle.current().pid());
    System.out.println("Press any key to exit...");

    System.in.read();
  }

}
