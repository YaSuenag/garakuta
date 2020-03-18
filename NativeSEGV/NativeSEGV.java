
public class NativeSEGV{

  private static native void doSEGV();
  private static native void doSEGVInLibC();

  static{
    System.loadLibrary("segv");
  }

  public static void main(String[] args){
    if ((args.length == 1) && args[0].equals("-l")) {
      doSEGVInLibC();
    }
    else{
      doSEGV();
    }
  }

}

