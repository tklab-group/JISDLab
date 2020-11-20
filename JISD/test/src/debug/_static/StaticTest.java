package debug._static;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static util.Print.p;

public class StaticTest {
  @Test
  public void basicTest() {
    Static s = new Static("src", "demo.HelloWorld");
    Assertions.assertEquals(s.path(), "src\\demo\\HelloWorld.java");
    p(s.path());
    p(s.absPath());
    p(s.src());
  }
}
