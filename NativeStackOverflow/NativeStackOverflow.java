
public class NativeStackOverflow{

  private static native void doOverflow();

  static{
    System.loadLibrary("overflow");
  }

  public static void main(String[] args){
    doOverflow();
  }

}

