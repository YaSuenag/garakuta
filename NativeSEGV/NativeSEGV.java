import java.io.IOException;
import java.util.*;
import java.util.regex.*;


public class NativeSEGV{

  private static native void doSEGV();
  private static native void doSEGVInLibC();

  static{
    var pattern = Pattern.compile("^file:(.+)NativeSEGV\\.jar!.+$");
    var fileElement = NativeSEGV.class
                                .getResource("NativeSEGV.class")
                                .getFile();
    var matcher = pattern.matcher(fileElement);
    matcher.matches();
    var libpath = matcher.group(1);
    libpath += System.getProperty("os.name").equals("Linux") ? "libsegv.so" : "segv.dll";

    System.load(libpath);
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

