import java.io.IOException;
import java.util.*;


public class NativeSEGV{

  private static native void doSEGV();
  private static native void doSEGVInLibC();

  static{
    System.loadLibrary("segv");
  }

  public static void main(String[] args) throws IOException{
    Set<String> opts = new HashSet<>(Arrays.asList(args));
    if(opts.remove("-s")){
      System.out.println("Press enter key to continue...");
      System.in.read();
    }
    if(opts.remove("-l")){
      doSEGVInLibC();
    }
    if(!opts.isEmpty()){
      System.err.println("warning: unknown option: " + opts.toString());
    }

    doSEGV();
  }

}

