package _static;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static util.Print.out;

public class StaticTest {
  @Test
  public void basicTest() {
    Class s = new Class("src", "demo.HelloWorld");
    Assertions.assertEquals(s.path(), "src\\demo\\HelloWorld.java");
    out(s.path());
    out(s.absPath());
    out(s.src());
  }

  @Test
  public void jsonFileReadTest() {
    var s = new StaticFile();
    Assertions.assertNotNull(s.getMd());
    Assertions.assertNotNull(s.getPs());
    out(s.getPs().toString());
  }
}
