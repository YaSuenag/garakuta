import java.lang.ref.*;
import java.net.*;
import java.nio.file.*;


public class Main{
  public static void main(String[] args) throws Exception{
    URL clspath = Paths.get("test").toUri().toURL();
    ClassLoader loader = new URLClassLoader(new URL[]{clspath});
    Class c = loader.loadClass("Test");
    String cname = c.getName();
    Cleaner.create()
           .register(c, () -> System.out.println("from Cleaner: " + cname));
    loader = null;
    c = null;
    System.gc();
    Thread.sleep(1000);
  }
}
