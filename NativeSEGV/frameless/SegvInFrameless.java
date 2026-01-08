public class SegvInFrameless{

  private static native void doSEGV();

  static{
    System.loadLibrary("segv");
  }

  public static void main(String[] args){
    doSEGV();
  }

}
