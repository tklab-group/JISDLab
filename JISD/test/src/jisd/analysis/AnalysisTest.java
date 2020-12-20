package jisd.analysis;

import org.junit.jupiter.api.Test;

public class AnalysisTest {
  @Test
  public void basicTest() {
    String[] args = new String[2];
    args[0] = "data/";
    args[1] = "../sample/";
    var className = "jisd/debug/Debugger";
    var c = ClassLoader.getSystemResourceAsStream(className + ".class");
    System.out.println(c);
    System.out.println("classpath : " + System.getProperty("java.class.path"));
    LysMain.main(args);
  }

  @Test
  public void jarFileTest() {
    String[] args = new String[2];
    args[0] = "data/";
    args[1] = "build/libs/jisd-0.0.2.jar";
    LysMain.main(args);
  }

  @Test
  public void multipleFilesTest() {
    String[] args = new String[4];
    args[0] = "data/";
    args[1] = "test/src/jisd/analysis/jisd.jar";
    args[2] = "bin";
    args[3] = "lib/lombok-1.18.16.jar";
    LysMain.main(args);
  }
}
