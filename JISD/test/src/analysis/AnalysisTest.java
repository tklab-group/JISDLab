package analysis;

import org.junit.jupiter.api.Test;

public class AnalysisTest {
  @Test
  public void basicTest() {
    String[] args = new String[2];
    args[0] = "data/";
    args[1] = "../sample/";
    var className = "debug/Debugger";
    var c = ClassLoader.getSystemResourceAsStream(className + ".class");
    System.out.println(c);
    System.out.println("classpath : " + System.getProperty("java.class.path"));
    LysMain.main(args);
  }
}
